package gcs;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class FileBasedSerializers {

    private FileBasedSerializers() {
    }

    /**
     * Creates a sorted list of unique snapshot versions. Versions are written in clear, one per line.
     *
     * @param snapshots The list of snapshots
     * @return byte[] The serialized snapshot index.
     */
    public static byte[] serializeSnapshotIndex(final List<Long> snapshots) {
        return snapshots.stream()
                .sorted()
                .distinct()
                .map(Object::toString)
                .collect(Collectors.joining("\n"))
                .getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Transforms a version into bytes.
     *
     * @param version snapshot version
     * @return the serialized version
     */
    public static byte[] serializeAnnouncmentFile(final long version) {
        return String.valueOf(version).getBytes(StandardCharsets.UTF_8);
    }
}