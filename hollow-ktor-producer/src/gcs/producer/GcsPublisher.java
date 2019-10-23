package gcs.producer;

import static com.netflix.hollow.api.producer.HollowProducer.Blob.Type.SNAPSHOT;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableMap;
import com.netflix.hollow.api.producer.HollowProducer;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import gcs.GcsConstants;
import gcs.Util;
import io.ktor.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GcsPublisher implements HollowProducer.Publisher {

    private static final Logger LOG = LoggerFactory.getLogger(GcsPublisher.class);

    private final String bucketName;
    private final String namespace;
    private final Storage storage;
    private final Indexer indexer;

    public GcsPublisher(final ApplicationConfig config,
                        final Indexer indexer) {
        this(config, indexer, StorageOptions.getDefaultInstance().getService());
    }

    public GcsPublisher(final ApplicationConfig config,
                        final Indexer indexer,
                        final Storage storage) {
        final String bucketName = config.property(GcsConstants.CONFIG_GCS_BUCKET).getString();
        this.storage = storage;
        this.bucketName = bucketName;
        this.namespace = config.property(GcsConstants.CONFIG_NAMESPACE).getString();
        this.indexer = indexer;
        LOG.info("Starting GCS publisher in {}/{}", bucketName, namespace);
    }

    @Override
    public void publish(final HollowProducer.Blob blob) {
        if (Objects.isNull(storage)) {
            LOG.error("Could not publish delta. GSC not initialized.");
            return;
        }

        final long blobVersion =
                blob.getType().equals(HollowProducer.Blob.Type.SNAPSHOT) ? blob.getToVersion()
                        : blob.getFromVersion();
        final String objectName =
                Util.getGcsObjectName(namespace, blob.getType().prefix, blobVersion);
        final BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
                .setMetadata(ImmutableMap
                        .of("from_state", String.valueOf(blob.getFromVersion()),
                                "to_state", String.valueOf(blob.getToVersion())))
                .setCacheControl("no-cache")
                .setStorageClass(StorageClass.MULTI_REGIONAL)
                .build();

        uploadBlob(blob, blobInfo);
        if (Objects.nonNull(indexer) && SNAPSHOT.equals(blob.getType())) {
            indexer.addSnapshotVersion(blob.getToVersion());
        }
    }

    private void uploadBlob(final HollowProducer.Blob blob, final BlobInfo blobInfo) {
        try {
            final byte[] bytes = Files.readAllBytes(blob.getPath());
            storage.create(blobInfo, bytes);
        } catch (IOException e) {
            LOG.warn("Failed to upload file to GCS: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
