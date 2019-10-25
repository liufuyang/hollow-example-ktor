package gcs.producer;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.netflix.hollow.api.producer.HollowProducer;
import gcs.GcsConstants;
import io.ktor.config.ApplicationConfig;
import io.ktor.config.ApplicationConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.netflix.hollow.api.producer.HollowProducer.Blob.Type.*;

public class GcsBlobCleaner extends HollowProducer.BlobStorageCleaner {

  private static final Logger LOG = LoggerFactory.getLogger(GcsBlobCleaner.class);
  private static final int DEFAULT_MAX_OBJECTS_TO_KEEP = 100;

  private final String bucketName;
  private final String namespace;
  private final Storage storage;
  private final Indexer indexer;
  private int maxObjectsToKeep;

  public GcsBlobCleaner(final ApplicationConfig config, final Indexer indexer) {
    this(
        config,
        indexer,
        StorageOptions.getDefaultInstance().getService(),
        Optional.ofNullable(config.propertyOrNull(GcsConstants.CONFIG_BLOBS_TO_KEEP))
            .map(ApplicationConfigValue::getString)
            .map(Integer::valueOf)
            .orElse(GcsConstants.DEFAULT_BLOBS_TO_KEEP));
  }

  @SuppressWarnings("WeakerAccess")
  public GcsBlobCleaner(
      final ApplicationConfig config,
      final Indexer indexer,
      final Storage storage,
      final int maxObjectsToKeep) {
    this.bucketName = config.property(GcsConstants.CONFIG_GCS_BUCKET).getString();
    this.namespace = config.property(GcsConstants.CONFIG_NAMESPACE).getString();
    this.storage = storage;
    this.maxObjectsToKeep = maxObjectsToKeep;
    this.indexer = indexer;
  }

  @Override
  public void cleanSnapshots() {
    final List<Long> removed =
        removeOlderObjects(SNAPSHOT.prefix).stream()
            .map(blob -> blob.getMetadata().get(GcsConstants.METADATA_TO))
            .map(Long::parseLong)
            .collect(Collectors.toList());
    // Clean the removed snapshots from the index
    if (!removed.isEmpty()) {
      indexer.purge(removed);
    }
    LOG.debug("Removed {} {} from {}", removed, SNAPSHOT.prefix, bucketName);
  }

  @Override
  public void cleanDeltas() {
    final long removed = removeOlderObjects(DELTA.prefix).size();
    LOG.debug("Removed {} {} from {}", removed, DELTA.prefix, bucketName);
  }

  @Override
  public void cleanReverseDeltas() {
    final long removed = removeOlderObjects(REVERSE_DELTA.prefix).size();
    LOG.debug("Removed {} {} from {}", removed, REVERSE_DELTA.prefix, bucketName);
  }

  private int getMaxObjectsToKeep() {
    return maxObjectsToKeep < 0 ? DEFAULT_MAX_OBJECTS_TO_KEEP : maxObjectsToKeep;
  }

  @SuppressWarnings("unused")
  public void setMaxObjectsToKeep(int maxObjectsToKeep) {
    this.maxObjectsToKeep = maxObjectsToKeep;
  }

  private List<Blob> removeOlderObjects(final String directory) {
    final List<Blob> blobList = getSortedListOfBlobs(getBucketBlobs(directory), directory);
    LOG.debug(
        "Keeping {} number of items out of {} in bucket {}",
        Math.min(blobList.size(), getMaxObjectsToKeep()),
        blobList.size(),
        bucketName);

    return blobList.stream()
        .skip(getMaxObjectsToKeep())
        .filter(this::removeObject)
        .collect(Collectors.toList());
  }

  private boolean removeObject(final Blob blob) {
    LOG.debug("Removing Blob {}", blob.getBlobId().getName());
    return storage.delete(blob.getBlobId());
  }

  private Page<Blob> getBucketBlobs(final String directory) {
    return storage.list(
        bucketName,
        Storage.BlobListOption.currentDirectory(),
        Storage.BlobListOption.prefix(String.format("%s/%s/", namespace, directory)));
  }

  private List<Blob> getSortedListOfBlobs(final Page<Blob> page, final String directory) {
    List<Blob> blobs = new ArrayList<>();
    page.iterateAll()
        .forEach(
            b -> {
              if (!b.isDirectory()) {
                blobs.add(b);
              }
            });
    // Sort the list descending.
    blobs.sort(
        Comparator.<Blob>comparingLong(
                blob -> Long.parseLong(blob.getMetadata().get(GcsConstants.METADATA_TO)))
            .reversed());
    LOG.debug("Found {} blobs in bucket {} with directory {}", blobs.size(), bucketName, directory);

    return blobs;
  }
}
