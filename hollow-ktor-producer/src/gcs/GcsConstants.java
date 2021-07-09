package gcs;

public class GcsConstants {

  public static final String CONFIG_GCS_BUCKET = "carbon.gcs.bucket";
  public static final String CONFIG_NAMESPACE = "carbon.gcs.namespace";
  public static final String DEFAULT_NAMESPACE = "carbon";

  public static final String DEFAULT_DIR = System.getProperty("java.io.tmpdir");
  public static final String PINNED_FILE = "pinned.version";
  public static final String SNAPSHOT_INDEX_FILE = "snapshot.index";
  public static final String CONFIG_CYCLE_TIME = "carbon.interval";
  public static final int DEFAULT_CYCLE_TIME_SECONDS = 30;

  public static final String CONFIG_SNAPSHOT_CYCLE = "carbon.producer.states";
  public static final int DEFAULT_STATES_BETWEEN_SNAPSHOTS = 50;
  public static final String CONFIG_BLOBS_TO_KEEP = "carbon.producer.maxObjectsToKeep";
  public static final int DEFAULT_BLOBS_TO_KEEP = 100;
  public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

  public static final String METADATA_TO = "to_state";

  private GcsConstants() {}
}
