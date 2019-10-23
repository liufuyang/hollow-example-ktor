package gcs.producer;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import gcs.FileBasedDeserializers;
import gcs.FileBasedSerializers;
import gcs.GcsConstants;
import gcs.Util;
import io.ktor.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GcsIndexer implements Indexer {

    private static final Logger LOG = LoggerFactory.getLogger(GcsIndexer.class);

    private final String bucketName;
    private final String namespace;
    private final Storage storage;

    public GcsIndexer(final ApplicationConfig config) {
        this(config, StorageOptions.getDefaultInstance().getService());
    }

    public GcsIndexer(final ApplicationConfig config, final Storage storage) {
        this.bucketName = config.property(GcsConstants.CONFIG_GCS_BUCKET).getString();
        this.namespace = config.property(GcsConstants.CONFIG_NAMESPACE).getString();
        this.storage = storage;
        LOG.info("Starting a GCS indexer in {}/{}", bucketName, namespace);
    }

    @Override
    public boolean addSnapshotVersion(final long version) {
        List<Long> snapshots = getSnapshotVersions();
        if (!snapshots.contains(version)) {
            snapshots = new ArrayList<>(snapshots);
            snapshots.add(version);
            LOG.debug("Adding version {} to snapshot index {}", version, getIndexFilePath(true));
        }
        return writeIndexContents(snapshots);
    }

    @Override
    public List<Long> getSnapshotVersions() {
        try {
            final String indexFilePath = getIndexFilePath(false);
            final Blob indexBlob =
                    storage.get(BlobId.of(bucketName, indexFilePath),
                            Storage.BlobGetOption.fields(Storage.BlobField.METADATA));
            if (Objects.isNull(indexBlob)) {
                LOG.debug("No snapshot index found. Creating a empty one.");
                storage.create(BlobInfo.newBuilder(bucketName, indexFilePath).build());
            }
            return FileBasedDeserializers.readIndexFile(
                    Util.downloadFile(storage, bucketName, indexFilePath));
        } catch (RuntimeException e) {
            LOG.warn("Failed to read snapshot index at {}. Defaulting to empty list",
                    getIndexFilePath(true));
        }

        return Collections.emptyList();
    }

    private boolean writeIndexContents(final List<Long> snapshots) {
        /// build a binary representation of the list -- gap encoded variable-length integers
        final byte[] idxBytes = FileBasedSerializers.serializeSnapshotIndex(snapshots);
        final BlobInfo blobInfo =
                BlobInfo.newBuilder(bucketName, getIndexFilePath(false))
                        .setCacheControl("no-cache")
                        .setStorageClass(StorageClass.MULTI_REGIONAL)
                        .setMetadata(ImmutableMap.of("Content-Length", String.valueOf(idxBytes.length)))
                        .build();

        try {
            // upload the new file content.
            storage.create(blobInfo, idxBytes);

            return true;
        } catch (StorageException e) {
            LOG.warn("Failed to upload snapshot index to {}: {}", getIndexFilePath(true), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    String getIndexFilePath(final boolean withBucket) {
        if (withBucket) {
            return String.format("%s/%s/%s", bucketName, namespace, GcsConstants.SNAPSHOT_INDEX_FILE);
        }

        return String.format("%s/%s", namespace, GcsConstants.SNAPSHOT_INDEX_FILE);
    }
}