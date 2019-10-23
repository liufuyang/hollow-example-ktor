package gcs;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.core.memory.encoding.HashCodes;
import com.netflix.hollow.core.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private Util() {
    }

    public static String getGcsObjectName(final String blobNamespace,
                                          final String fileType,
                                          final long lookupVersion) {
        return String.format("%s%s-%s", getGcsObjectPrefix(blobNamespace, fileType),
                Integer.toHexString(HashCodes.hashLong(lookupVersion)),
                lookupVersion);
    }

    public static String getGcsObjectPrefix(final String blobNamespace, final String fileType) {
        return String.format("%s/%s/", blobNamespace, fileType);
    }

    public static File downloadFile(final Storage storage,
                                    final String bucketName,
                                    final String objectName) throws RuntimeException {
        final File tempFile =
                new File(TMP_DIR, objectName.replace('/', '-'));
        final byte[] bytes = storage.readAllBytes(BlobId.of(bucketName, objectName));

        try (
                ByteArrayInputStream input = new ByteArrayInputStream(bytes);
                FileOutputStream outputFile = new FileOutputStream(tempFile);
                DataOutputStream outputData = new DataOutputStream(outputFile)
        ) {
            IOUtils.copyBytes(input, new DataOutputStream[]{ outputData }, input.available());
        } catch (IOException e) {
            throw new RuntimeException("Unable to download file: " + objectName, e);
        }

        return tempFile;
    }

    public static long parseSnapshotVersion(final BlobId blobId) {
        try {
            return Long.parseLong(blobId.getName().substring(blobId.getName().lastIndexOf("-") + 1));
        } catch (NumberFormatException e) {
            LOG.warn("Failed to parse version of snapshot '{}' in bucket {}", blobId.getName(),
                    blobId.getBucket());
        }

        return HollowConsumer.AnnouncementWatcher.NO_ANNOUNCEMENT_AVAILABLE;
    }
}
