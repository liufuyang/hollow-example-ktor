package gcs.producer;

import com.google.cloud.storage.*;
import com.netflix.hollow.api.producer.HollowProducer;
import com.netflix.hollow.api.producer.fs.HollowFilesystemAnnouncer;
import gcs.FileBasedSerializers;
import gcs.GcsConstants;
import gcs.GcsPinnableAnnounceReader;
import io.ktor.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class GcsAnnouncer implements HollowProducer.Announcer {

    private static final Logger LOG = LoggerFactory.getLogger(GcsAnnouncer.class);

    private final String bucketName;
    private final String namespace;
    private final Storage storage;

    public GcsAnnouncer(final ApplicationConfig config) {
        this(config, StorageOptions.getDefaultInstance().getService());
    }

    public GcsAnnouncer(final ApplicationConfig config, final Storage storage) {
        final String bucketName = config.property(GcsConstants.CONFIG_GCS_BUCKET).getString();
        this.bucketName = bucketName;
        this.namespace = config.property(GcsConstants.CONFIG_NAMESPACE).getString();
        this.storage = storage;
        LOG.info("Starting a GCS announcer in {}/{}", bucketName, namespace);
    }

    @Override
    public void announce(long version) {
        if (Objects.isNull(storage)) {
            LOG.warn("Could not announce version. Storage not initialized.");
            return;
        }

        createBlob(version, HollowFilesystemAnnouncer.ANNOUNCEMENT_FILENAME);
    }



    private boolean createBlob(final long version, final String blobName) {
        final BlobInfo blobInfo =
                BlobInfo.newBuilder(bucketName, String.format("%s/%s", namespace, blobName))
                        .setCacheControl("no-cache")
                        .setStorageClass(StorageClass.MULTI_REGIONAL)
                        .build();
        try {
            storage.create(blobInfo, FileBasedSerializers.serializeAnnouncmentFile(version));
            return true;
        } catch (StorageException e) {
            LOG.warn("Failed to store blob {}/{}:{}", bucketName, namespace, blobName, e.getMessage());
            return false;
        }
    }

    public boolean isPinned() {
        try {
            final Blob pinnedBlob =
                    storage
                            .get(BlobId.of(bucketName, String.format("%s/%s", namespace, GcsConstants.PINNED_FILE)));

            return Objects.nonNull(pinnedBlob);
        } catch (StorageException e) {
            LOG.warn("Failed to check if {} is pinned. Defaulting to unpinned. {}", namespace,
                    e.getMessage());
            return false;
        }
    }

    public long getAnnouncedVersion() {
        return GcsPinnableAnnounceReader.readAnnouncedVersion(storage, bucketName, namespace);
    }
}
