package gcs.repo;

import com.example.gcs.InfoEntity;
import com.netflix.hollow.api.producer.HollowProducer;
import com.netflix.hollow.api.producer.fs.HollowFilesystemBlobStager;
import com.netflix.hollow.core.write.HollowBlobWriter;
import com.netflix.hollow.core.write.HollowWriteStateEngine;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import gcs.GcsConstants;
import gcs.customer.GcsBolbRetriever;
import gcs.customer.GcsWatcher;
import io.ktor.config.ApplicationConfig;
import io.ktor.config.ApplicationConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryProducer extends AbstractProducer {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryProducer.class);
    private static final ExecutorService PUBLISH_EXECUTOR = Executors.newSingleThreadExecutor();

    private Map<Integer, InfoEntity> db;

    public RepositoryProducer(final HollowProducer.Announcer announcer,
                              final ApplicationConfig config,
                              final HollowProducer.Publisher publisher,
                              final GcsBolbRetriever retriever,
                              final GcsWatcher watcher) {
        super(announcer, config, publisher, retriever, watcher);
        final int statesBetweenSnapshots =
                Optional.ofNullable(config.propertyOrNull(GcsConstants.CONFIG_SNAPSHOT_CYCLE))
                        .map(ApplicationConfigValue::getString)
                        .map(Integer::valueOf)
                        .orElse(GcsConstants.DEFAULT_STATES_BETWEEN_SNAPSHOTS);

        this.producerBuilder = HollowProducer.withPublisher(publisher)
                .withAnnouncer(announcer)
                .withSnapshotPublishExecutor(PUBLISH_EXECUTOR)
                .withNumStatesBetweenSnapshots(statesBetweenSnapshots)
                .withVersionMinter(() -> currentVersion)
                .withListener(new LoggingProducerListener())
//                .withBlobStorageCleaner(publisher.getCleaner())
        ;
        LOG.debug("Starting a Hollow producer as primary: {} with {} states between snapshots",
                true, statesBetweenSnapshots);
    }


    public void addDb(Map<Integer, InfoEntity> db) {
        this.db = db;
    }

    @SuppressWarnings("unused")
    @Override
    public void initAndRestore() {
        if (Objects.isNull(this.incrementalProducer)) {
            this.incrementalProducer = producerBuilder.buildIncremental();
        } else {
            LOG.error("The producer has already been initialized.");
        }

        LOG.debug("Initializing producer for {} entities", db.size());
        incrementalProducer.initializeDataModel(InfoEntity.class);

        restoreIfAvailable();
        start();
    }

    @Override
    public void runCycle() {
        final List<InfoEntity> updatedEntities = getUpdatedEntitiesAfter(currentVersion);
        final List<InfoEntity> deletedEntities = getDeletedEntitiesAfter(currentVersion);
        if (!updatedEntities.isEmpty() || !deletedEntities.isEmpty()) {
            LOG.debug("Running producer cycle updating {} entities removing {}", updatedEntities.size(),
                    deletedEntities.size());

            final long newVersion = Math.max(getMaxUpdatedTimestamp(updatedEntities),
                    getMaxUpdatedTimestamp(deletedEntities));
            writeNewState(updatedEntities, deletedEntities, newVersion);
        }
    }

    private List<InfoEntity> getUpdatedEntitiesAfter(final long timestamp) {
        return db.values().stream().filter(e -> e.getTimeUpdated() > timestamp).collect(Collectors.toList());
    }

    private List<InfoEntity> getDeletedEntitiesAfter(final long timestamp) {
        return Collections.emptyList();
    }

    private long getMaxUpdatedTimestamp(final List<InfoEntity> entities) {
        return entities.stream()
                .map(entity -> entity.getTimeUpdated())
                .max(Comparator.naturalOrder())
                .orElse(currentVersion);
    }

    @Override
    public boolean forceSnapshot() {
        HollowProducer temporaryProducer = producerBuilder.build();
        HollowWriteStateEngine writeEngine = temporaryProducer.getWriteEngine();
        writeEngine.prepareForNextCycle();

        final HollowBlobWriter blobWriter = new HollowBlobWriter(writeEngine);
        final List<InfoEntity> entities = getUpdatedEntitiesAfter(0L);
        final HollowFilesystemBlobStager stager = new HollowFilesystemBlobStager();
        final long versionOfSnapshot = Math.max(getMaxUpdatedTimestamp(entities), currentVersion) + 1L;

        final HollowProducer.Blob blob = stager.openSnapshot(versionOfSnapshot);

        entities.forEach(temporaryProducer.getObjectMapper()::add);

        try {
            OutputStream output = new BufferedOutputStream(Files.newOutputStream(blob.getPath()));
            blobWriter.writeSnapshot(output);
            publisher.publish(blob);
            announcer.announce(versionOfSnapshot);
            incrementalProducer.restore(versionOfSnapshot, retriever);
            this.currentVersion = versionOfSnapshot;
        } catch (IOException e) {
            LOG.error("There was an error while reindexing a new snapshot.", e);
            return false;
        }

        return true;
    }
}


