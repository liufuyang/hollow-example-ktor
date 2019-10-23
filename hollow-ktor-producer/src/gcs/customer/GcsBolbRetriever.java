package gcs.customer;

import static com.netflix.hollow.api.producer.HollowProducer.Blob.Type.DELTA;
import static com.netflix.hollow.api.producer.HollowProducer.Blob.Type.REVERSE_DELTA;
import static com.netflix.hollow.api.producer.HollowProducer.Blob.Type.SNAPSHOT;
import static gcs.Util.downloadFile;
import static gcs.Util.getGcsObjectName;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.netflix.hollow.api.consumer.HollowConsumer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gcs.GcsConstants;
import gcs.Util;
import io.ktor.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GcsBolbRetriever implements HollowConsumer.BlobRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(GcsBolbRetriever.class);

    private static final String DEFAULT_VERSION_STRING =
            String.valueOf(HollowConsumer.AnnouncementWatcher.NO_ANNOUNCEMENT_AVAILABLE);

    private final Storage storage;
    private final String bucketName;
    private final String namespace;

    private final GcsIndex index;


    public GcsBolbRetriever(final ApplicationConfig config, final GcsIndex index) {
        this(config, index, StorageOptions.getDefaultInstance().getService());
    }

    @SuppressWarnings("WeakerAccess")
    public GcsBolbRetriever(final ApplicationConfig config, final GcsIndex index, final Storage storage) {

        final String bucketName = config.property(GcsConstants.CONFIG_GCS_BUCKET).getString();
        this.bucketName = bucketName;
        this.namespace = config.property(GcsConstants.CONFIG_NAMESPACE).getString();
        this.storage = storage;
        this.index = index;
        LOG.info("Starting a GCS retriever in {}/{}", bucketName, namespace);
    }

    @Override
    public HollowConsumer.Blob retrieveSnapshotBlob(final long desiredVersion) {
        HollowConsumer.Blob blob = knownSnapshotBlob(desiredVersion);

        if (Objects.isNull(blob) && Objects.nonNull(index)) {
            /// There was no exact match for a snapshot leading to the desired state.
            /// We'll use the snapshot abstractIndex to find the nearest one before the desired state.
            return index.getIndexedSnapshots().stream()
                    .filter(version -> version < desiredVersion)
                    .map(this::knownSnapshotBlob)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        return blob;
    }

    @Override
    public HollowConsumer.Blob retrieveDeltaBlob(final long currentVersion) {
        return knownDeltaBlob(DELTA.prefix, currentVersion);
    }

    @Override
    public HollowConsumer.Blob retrieveReverseDeltaBlob(final long currentVersion) {
        return knownDeltaBlob(REVERSE_DELTA.prefix, currentVersion);
    }

    public List<Long> getAllStoredSnapshotVersions() {
        final List<Long> snapshots = new ArrayList<>();
        storage.list(Util.getGcsObjectPrefix(namespace, SNAPSHOT.prefix))
                .iterateAll()
                .iterator()
                .forEachRemaining(blob -> snapshots.add(Util.parseSnapshotVersion(blob.getBlobId())));
        return null;
    }

    private HollowConsumer.Blob knownSnapshotBlob(final long desiredVersion) {
        HollowConsumer.Blob hollowBlob = null;
        final String objectName = getGcsObjectName(namespace, SNAPSHOT.prefix, desiredVersion);
        try {
            final Blob blob = storage.get(BlobId.of(bucketName, objectName),
                    Storage.BlobGetOption.fields(Storage.BlobField.METADATA));
            if (Objects.nonNull(blob)) {
                final long toState = Long.parseLong(blob.getMetadata()
                        .getOrDefault("to_state", DEFAULT_VERSION_STRING));
                hollowBlob = new GcsBlob(objectName, toState);
            }
        } catch (StorageException e) {
            LOG.warn("Failed to retrieve snapshot blob {}. {}", objectName, e.getMessage());
        } catch (NumberFormatException e) {
            LOG.warn("Failed to parse version of stored snapshot {}: {}", objectName, e.getMessage());
        }

        return hollowBlob;
    }

    private HollowConsumer.Blob knownDeltaBlob(final String fileType, final long fromVersion) {
        HollowConsumer.Blob hollowBlob = null;
        final String objectName = getGcsObjectName(namespace, fileType, fromVersion);
        try {
            final Blob blob = storage.get(BlobId.of(bucketName, objectName),
                    Storage.BlobGetOption.fields(Storage.BlobField.METADATA));
            if (Objects.nonNull(blob)) {
                final long fromState =
                        Long.parseLong(blob.getMetadata().getOrDefault("from_state", DEFAULT_VERSION_STRING));
                final long toState =
                        Long.parseLong(blob.getMetadata().getOrDefault("to_state", DEFAULT_VERSION_STRING));
                hollowBlob = new GcsBlob(objectName, fromState, toState);
            }
        } catch (StorageException se) {
            LOG.warn("Failed to retrieve delta blob {}. {}", objectName, se.getMessage());
        } catch (NumberFormatException e) {
            LOG.warn("Failed to parse metadata on GCS object {}", objectName);
        }

        return hollowBlob;
    }


    private class GcsBlob extends HollowConsumer.Blob {

        private final String objectName;

        GcsBlob(final String objectName, final long toVersion) {
            super(toVersion);
            this.objectName = objectName;
        }

        GcsBlob(final String objectName, final long fromVersion, final long toVersion) {
            super(fromVersion, toVersion);
            this.objectName = objectName;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            final File tempFile = downloadFile(storage, bucketName, objectName);

            return new BufferedInputStream(new FileInputStream(tempFile)) {
                @SuppressWarnings("ResultOfMethodCallIgnored")
                @Override
                public void close() throws IOException {
                    super.close();
                    tempFile.delete();
                }
            };
        }
    }
}
