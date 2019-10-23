package gcs;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.producer.fs.HollowFilesystemAnnouncer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class GcsPinnableAnnounceReader {

    static final String PINNED_FILE = "pinned.version";
    private static final Logger LOG = LoggerFactory.getLogger(GcsPinnableAnnounceReader.class);

    private GcsPinnableAnnounceReader() {
    }

    public static long readAnnouncedVersion(final Storage storage,
                                            final String bucketName,
                                            final String namespace) {
        return readAnnouncedVersionBytes(storage, bucketName, namespace)
                .map(FileBasedDeserializers::deserializeAnnouncementFile)
                .orElse(HollowConsumer.AnnouncementWatcher.NO_ANNOUNCEMENT_AVAILABLE);
    }

    private static Optional<byte[]> readAnnouncedVersionBytes(final Storage storage,
                                                              final String bucketName,
                                                              final String namespace) {
        try {
            final BlobId pinnedBlobId =
                    BlobId.of(bucketName, String.format("%s/%s", namespace, PINNED_FILE));
            final BlobId announcedBlobId =
                    BlobId.of(bucketName,
                            String.format("%s/%s", namespace, HollowFilesystemAnnouncer.ANNOUNCEMENT_FILENAME));
            Blob blobUsed = storage.get(pinnedBlobId);
            if (Objects.isNull(blobUsed)) {
                LOG.debug("No pinned version available.");
                blobUsed = storage.get(announcedBlobId);
            }

            if (Objects.nonNull(blobUsed)) {
                return Optional.of(storage.readAllBytes(blobUsed.getBlobId()));
            }
        } catch (StorageException | IllegalArgumentException e) {
            LOG.warn("Failed to read announced version from GCS", e.getMessage());
        }

        return Optional.empty();
    }

}
