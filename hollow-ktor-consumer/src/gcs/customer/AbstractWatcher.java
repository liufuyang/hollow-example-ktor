package gcs.customer;

import com.netflix.hollow.api.consumer.HollowConsumer;
import gcs.GcsConstants;
import io.ktor.config.ApplicationConfig;
import io.ktor.config.ApplicationConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractWatcher implements HollowConsumer.AnnouncementWatcher {

    private static final long INITIAL_WAIT = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractWatcher.class);
    protected final ApplicationConfig config;
    protected final List<HollowConsumer> consumers = Collections.synchronizedList(new ArrayList<>());
  protected long currentVersion = HollowConsumer.AnnouncementWatcher.NO_ANNOUNCEMENT_AVAILABLE;

    public AbstractWatcher(final ApplicationConfig config, final boolean watchForAnnouncements) {
        this.config = config;
        if (watchForAnnouncements) {
            final long cycleTime = Optional.ofNullable(config.propertyOrNull(GcsConstants.CONFIG_CYCLE_TIME))
                        .map(ApplicationConfigValue::getString)
                        .map(Integer::valueOf).orElse(GcsConstants.DEFAULT_CYCLE_TIME_SECONDS);
            LOG.debug("Started watching for announcements every {} second", cycleTime);
            // TODO: ignored future below shouldn't be ignored
            final ScheduledFuture<?> ignored = Executors.newSingleThreadScheduledExecutor()
                    .scheduleAtFixedRate(
                            this::refreshCurrentVersion, INITIAL_WAIT, cycleTime, TimeUnit.SECONDS);
        }
    }

    public abstract boolean isPinned();

    protected abstract void refreshCurrentVersion();
}
