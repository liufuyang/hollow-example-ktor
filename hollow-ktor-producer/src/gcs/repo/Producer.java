package gcs.repo;

import com.netflix.hollow.api.producer.HollowProducer;
import gcs.customer.GcsBolbRetriever;
import gcs.customer.GcsWatcher;

@SuppressWarnings("unused")
public interface Producer {

  void initAndRestore();

  boolean forceSnapshot();

  HollowProducer.Announcer getAnnouncer();

  HollowProducer.Publisher getPublisher();

  GcsBolbRetriever getRetriever();

  GcsWatcher getWatcher();

  void stop();

  void start();

  boolean isRunning();

  void restore();
}
