package gcs.repo;

import com.netflix.hollow.api.producer.HollowProducer;
import com.netflix.hollow.api.producer.enforcer.SingleProducerEnforcer;
import gcs.customer.GcsBolbRetriever;
import gcs.customer.GcsWatcher;
import gcs.producer.Indexer;

@SuppressWarnings("unused")
public interface Producer {

    void initAndRestore();

//    void pinVersion(final long version);

//    void unpin();

//    boolean isPinned();

    boolean forceSnapshot();

    Indexer getIndexer();

    HollowProducer.Announcer getAnnouncer();

    HollowProducer.Publisher getPublisher();

    GcsBolbRetriever getRetriever();

    GcsWatcher getWatcher();

//    SingleProducerEnforcer getEnforcer();

    void stop();

    void start();

    boolean isRunning();

    void restore();
}
