package com.windhoverlabs.pv.yamcs;

import com.windhoverlabs.yamcs.core.YamcsServer;
import java.time.Instant;
import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.protobuf.ProcessorInfo;

/**
 * Marks a component as being aware of the global YamcsObjectManager state. This state includes the
 * connected instance and/or processor.
 */
public interface YamcsAware {
  public enum YamcsAwareMethod {
    onYamcsDisconnected
  };

  default void onYamcsObjectManagerInit() {
    changeDefaultInstance();
  }

  // TODO:It might be worth considering passing the YamcsServer/YamcsInstance object to the
  // implementors of this function
  default void onInstancesReady(YamcsServer s) {}

  default void onYamcsConnecting() {}

  // Called when we fail to connect to the server.
  default void onYamcsConnectionFailed(Throwable t) {}

  /** Called when we the global connection to yamcs was successfully established */
  // TODO:It might worth considering passing the YamcsServer/YamcsInstance object to the
  // implementors of this function
  default void onYamcsConnected() {}

  /** Called when the yamcs is connection went lost */
  default void onYamcsDisconnected() {}

  /** The activated instance has changed. */
  default void changeInstance(String instance) {}

  default void changeDefaultInstance() {}

  /**
   * The globally activated processor has changed. This is always called on the UI thread.
   *
   * <p>Note that this method is not called if only the instance has changed.
   */
  default void changeProcessor(String instance, String processor) {}

  default void changeProcessorInfo(ProcessorInfo processor) {}

  /**
   * Actual mission time as reported by Yamcs Server. If no time is defined (for example when not
   * connected), listeners will receive null
   */
  default void updateTime(Instant time) {}

  default void updateClearance(boolean enabled, SignificanceLevelType level) {}
  
  default void updateLink(String link) 
  {
	  
  }
}
