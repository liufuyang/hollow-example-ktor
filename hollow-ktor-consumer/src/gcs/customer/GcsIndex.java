package gcs.customer;


import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import gcs.FileBasedDeserializers;
import gcs.GcsConstants;
import io.ktor.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static gcs.Util.downloadFile;


public class GcsIndex {

    private static final Logger LOG = LoggerFactory.getLogger(GcsIndex.class);

    private final String namespace;
    private final String bucketName;
    private final Storage storage;

    public GcsIndex(final ApplicationConfig config) {
        this(config, StorageOptions.getDefaultInstance().getService());
    }

    public GcsIndex(final ApplicationConfig config, final Storage storage) {
        this.bucketName =  config.property(GcsConstants.CONFIG_GCS_BUCKET).getString();
        this.namespace = config.property(GcsConstants.CONFIG_NAMESPACE).getString();
        this.storage = storage;
        LOG.info("Starting a GCS index in {}/{}", bucketName, namespace);
    }

    String getSnapshotIndexObjectName() {
        return String.format("%s/%s", namespace, GcsConstants.SNAPSHOT_INDEX_FILE);
    }

    public List<Long> getIndexedSnapshots() {
        try {
            return FileBasedDeserializers.readIndexFile(
                    downloadFile(storage, bucketName, getSnapshotIndexObjectName()));
        } catch (RuntimeException e) {
            LOG.warn("Failed to read index file in {}: {}", bucketName, e.getMessage());
        }
        return Collections.emptyList();
    }

}
