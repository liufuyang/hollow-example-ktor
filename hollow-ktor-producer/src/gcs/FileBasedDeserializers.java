package gcs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedDeserializers {

    private static final Logger LOG = LoggerFactory.getLogger(FileBasedDeserializers.class);

    private FileBasedDeserializers() {
    }

    /**
     * Reads files serialized with
     */
    public static List<Long> readIndexFile(final File indexFile) {
        try (Stream<String> lines = Files.lines(Paths.get(indexFile.toURI()))) {
            return lines.map(Long::parseLong)
                    .sorted(Comparator.reverseOrder())
                    .distinct()
                    .collect(Collectors.toList());
        } catch (NoSuchFileException e) {
            LOG.warn("There is no snapshot index file at {}.", indexFile.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("There was an error reading the index file {}", indexFile.getAbsolutePath(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Reads data serialized with
     */
    public static long deserializeAnnouncementFile(final byte[] bytes) {
        return Long.parseLong(new String(bytes, StandardCharsets.UTF_8));
    }
}
