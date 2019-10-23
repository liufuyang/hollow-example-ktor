package gcs;

public class GcsConstants {

    public static final String CONFIG_GCS_BUCKET = "carbon.gcs.bucket";
    public static final String CONFIG_NAMESPACE = "carbon.gcs.namespace";
    public static final String DEFAULT_NAMESPACE = "carbon";

    public static final String DEFAULT_DIR = System.getProperty("java.io.tmpdir");
    public static final String PINNED_FILE = "pinned.version";
    public static final String SNAPSHOT_INDEX_FILE = "snapshot.index";
    public static final String CONFIG_CYCLE_TIME = "carbon.consumer.interval";
    public static final int DEFAULT_CYCLE_TIME_SECONDS = 30;

    private GcsConstants() {
    }
}
