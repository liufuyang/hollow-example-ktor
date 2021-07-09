package gcs.repo;

import com.netflix.hollow.api.producer.HollowProducerListener;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProducerListener implements HollowProducerListener {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingProducerListener.class);

    @Override
    public void onProducerInit(final long elapsed, final TimeUnit unit) {
        LOG.debug("Producer init {} {}", elapsed, unit);
    }

    @Override
    public void onProducerRestoreStart(final long restoreVersion) {
        LOG.debug("Producer restore start {}", restoreVersion);
    }

    @Override
    public void onProducerRestoreComplete(final RestoreStatus status,
                                          final long elapsed,
                                          final TimeUnit unit) {
        LOG.debug("Producer restore complete {} {} {}", status.getStatus(), elapsed, unit);
    }

    @Override
    public void onNewDeltaChain(final long version) {
        LOG.debug("Starting new delta chain {}", version);
    }

    @Override
    public void onCycleStart(final long version) {
        LOG.debug("Producer cycle started {}", version);
    }

    @Override
    public void onCycleComplete(final ProducerStatus status,
                                final long elapsed,
                                final TimeUnit unit) {
        if (Status.FAIL.equals(status.getStatus())) {
            LOG.error("Producer cycle failed.", status.getCause());
        } else {
            LOG.info("Producer cycle completed to version {} with status {}, {}{}.",
                    status.getVersion(), status.getStatus(), elapsed, unit);
        }
    }

    @Override
    public void onNoDeltaAvailable(final long version) {
        LOG.debug("No delta available for version {}", version);
    }

    @Override
    public void onPopulateStart(final long version) {
        LOG.debug("Populating version {}", version);
    }

    @Override
    public void onPopulateComplete(final ProducerStatus status,
                                   final long elapsed,
                                   final TimeUnit unit) {
        LOG.debug("Finished populating with status {} took {} {}. {}", status.getStatus(), elapsed,
                unit,
                status.getCause());
    }

    @Override
    public void onPublishStart(final long version) {
        LOG.debug("Starting to publish version {}", version);
    }

    @Override
    public void onPublishComplete(final ProducerStatus status,
                                  final long elapsed,
                                  final TimeUnit unit) {
        LOG.debug("Finished publishing {} took {} {}", status.getStatus(), elapsed, unit);
    }

    @Override
    public void onArtifactPublish(final PublishStatus publishStatus,
                                  final long elapsed,
                                  final TimeUnit unit) {
        LOG.debug("Producer published artifact {}. Took {} {}", publishStatus.getStatus(), elapsed,
                unit);
    }

    @Override
    public void onIntegrityCheckStart(final long version) {
        LOG.debug("Checking integrity for {}", version);
    }

    @Override
    public void onIntegrityCheckComplete(final ProducerStatus status,
                                         final long elapsed,
                                         final TimeUnit unit) {
        LOG.debug("Integrity check completed {} {} {}. {}", status.getStatus(), elapsed, unit,
                status.getCause());
    }

    @Override
    public void onValidationStart(final long version) {
        LOG.debug("Validation started for {}", version);
    }

    @Override
    public void onValidationComplete(final ProducerStatus status,
                                     final long elapsed,
                                     final TimeUnit unit) {
        LOG.debug("Validation completed {} {} {}. {}", status.getStatus(), elapsed, unit,
                status.getCause());
    }

    @Override
    public void onAnnouncementStart(final long version) {
        LOG.debug("Producer announcement start");
    }

    @Override
    public void onAnnouncementComplete(final ProducerStatus status,
                                       final long elapsed,
                                       final TimeUnit unit) {
        LOG.debug("Producer announcement complete {} {} {}", status.getStatus(), elapsed, unit);
    }
}
