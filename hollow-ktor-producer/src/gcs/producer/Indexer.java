package gcs.producer;

import java.util.List;

public interface Indexer {

    boolean addSnapshotVersion(final long version);

    List<Long> getSnapshotVersions();
}