package gcs.repo;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.producer.HollowProducer;
import com.netflix.hollow.api.producer.HollowProducer.Incremental;
import gcs.GcsConstants;
import gcs.customer.GcsBolbRetriever;
import gcs.customer.GcsWatcher;
import gcs.producer.Indexer;
import io.ktor.config.ApplicationConfig;
import io.ktor.config.ApplicationConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractProducer implements Producer {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractProducer.class);
  private static final int SINGLE_THREAD_POOL = 1;
  private static final int INITIAL_DELAY = 1;

  protected HollowProducer.Builder<?> producerBuilder;
  // Using HollowProducer.Incremental as the only producer, no need to use a HollowProducer
  protected Incremental incrementalProducer;
  protected Indexer indexer;
  protected HollowProducer.Announcer announcer;
  protected HollowProducer.Publisher publisher;
  protected GcsBolbRetriever retriever;
  protected GcsWatcher watcher;
  protected ApplicationConfig config;
  protected volatile long currentVersion =
      HollowConsumer.AnnouncementWatcher.NO_ANNOUNCEMENT_AVAILABLE;
  protected ScheduledFuture<?> cycleThread;

  public AbstractProducer(
      final HollowProducer.Announcer announcer,
      final ApplicationConfig config,
      final HollowProducer.Publisher publisher,
      final GcsBolbRetriever retriever,
      final GcsWatcher watcher) {
    this.announcer = announcer;
    this.publisher = publisher;
    this.retriever = retriever;
    this.watcher = watcher;
    this.config = config;
  }

  @Override
  public abstract boolean forceSnapshot();

  @Override
  public Indexer getIndexer() {
    return indexer;
  }

  @Override
  public HollowProducer.Announcer getAnnouncer() {
    return announcer;
  }

  public HollowProducer.Builder<?> getProducerBuilder() {
    return producerBuilder;
  }

  public void setProducerBuilder(final HollowProducer.Builder<?> producerBuilder) {
    this.producerBuilder = producerBuilder;
  }

  @Override
  public HollowProducer.Publisher getPublisher() {
    return publisher;
  }

  @Override
  public GcsBolbRetriever getRetriever() {
    return this.retriever;
  }

  @Override
  public GcsWatcher getWatcher() {
    return watcher;
  }

  @Override
  public void stop() {
    if (Objects.nonNull(cycleThread)) {
      cycleThread.cancel(false);
    }
  }

  @Override
  public void start() {
    final long cycleTime =
        Optional.ofNullable(config.propertyOrNull(GcsConstants.CONFIG_CYCLE_TIME))
            .map(ApplicationConfigValue::getString)
            .map(Integer::valueOf)
            .orElse(GcsConstants.DEFAULT_CYCLE_TIME_SECONDS);
    LOG.debug("Scheduling producer cycle in {}s and every {}s", INITIAL_DELAY, cycleTime);
    cycleThread =
        Executors.newScheduledThreadPool(SINGLE_THREAD_POOL)
            .scheduleAtFixedRate(this::runCycle, INITIAL_DELAY, cycleTime, TimeUnit.SECONDS);
  }

  @Override
  public boolean isRunning() {
    return Objects.nonNull(cycleThread) && !(cycleThread.isCancelled() || cycleThread.isDone());
  }

  @Override
  public void restore() {
    restoreIfAvailable();
  }

  protected abstract void runCycle();

  protected void restoreIfAvailable() {
    final long announcedVersion = watcher.getLatestVersion();
    if (announcedVersion == HollowConsumer.AnnouncementWatcher.NO_ANNOUNCEMENT_AVAILABLE) {
      LOG.info("No Hollow state has been announced. Restore not needed.");
    } else {
      incrementalProducer.restore(announcedVersion, retriever);
      currentVersion = announcedVersion;
      LOG.info("Restored Hollow producer to version {}", announcedVersion);
    }
  }

  protected void writeNewState(
      final List<?> updates, final List<?> deletes, final Long newVersion) {
    currentVersion = newVersion;
    LOG.debug("Running incremental cycle on producer, version now is {}", currentVersion);

    // https://github.com/Netflix/hollow/blob/master/docs/getting-started.md#incremental-production
    incrementalProducer.runIncrementalCycle(
        state -> {
          updates.forEach(state::addOrModify);
          deletes.forEach(state::delete);
        });
  }
}
