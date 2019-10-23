package gcs.customer;

import com.google.cloud.storage.*;
import com.netflix.hollow.api.consumer.HollowConsumer;
import gcs.GcsConstants;
import gcs.GcsPinnableAnnounceReader;
import io.ktor.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class GcsWatcher extends AbstractWatcher {

    private static final Logger LOG = LoggerFactory.getLogger(GcsWatcher.class);

    private final Storage storage;
    private final String bucketName;
    private final String namespace;


    public GcsWatcher(final ApplicationConfig config) {
        this(config, StorageOptions.getDefaultInstance().getService());
    }

    public GcsWatcher(final ApplicationConfig config,
                      final Storage storage) {
        this(config, storage, true);
    }

    public GcsWatcher(final ApplicationConfig config,
                      final Storage storage,
                      final boolean watchForAnnouncements) {
        super(config, watchForAnnouncements);
        final String bucketName = config.property(GcsConstants.CONFIG_GCS_BUCKET).getString();
        this.bucketName = bucketName;
        this.namespace = config.property(GcsConstants.CONFIG_NAMESPACE).getString();
        this.storage = storage;

        refreshCurrentVersion();
        LOG.info("Started GCS watcher in {}/{}", bucketName, namespace);
    }

    @Override
    public long getLatestVersion() {
        return currentVersion;
    }

    @Override
    protected void refreshCurrentVersion() {
        long announcedVersion = readLatestAnnouncedVersion();
        if (currentVersion != announcedVersion) {
            LOG.debug("New version for {}/{}: {}", bucketName, namespace, announcedVersion);
            currentVersion = announcedVersion;
            synchronized (consumers) {
                consumers.forEach(HollowConsumer::triggerAsyncRefresh);
            }
        }
    }

    @Override
    public void subscribeToUpdates(HollowConsumer consumer) {
        consumers.add(consumer);
    }

    long readLatestAnnouncedVersion() {
        return GcsPinnableAnnounceReader.readAnnouncedVersion(storage, bucketName, namespace);
    }

    @Override
    public boolean isPinned() {
        final BlobId pinnedId =
                BlobId.of(bucketName, String.format("%s/%s", namespace, GcsConstants.PINNED_FILE));
        try {
            final Blob pinned =
                    storage.get(pinnedId, Storage.BlobGetOption.fields(Storage.BlobField.METADATA));

            return Objects.nonNull(pinned);
        } catch (StorageException e) {
            LOG.warn("Failed to check if {} is pinned. Defaulting to unpinned. {}", namespace,
                    e.getMessage());

            return false;
        }
    }
}
