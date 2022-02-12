package com.windhoverlabs.pv.yamcs;

import java.time.Instant;
import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.protobuf.ProcessorInfo;

/**
 * Marks a component as being aware of the global YamcsObjectManager state. This state includes the
 * connected instance and/or processor.
 */
public interface YamcsAware {

  default void onYamcsObjectManagerInit() {
    changeDefaultInstance();
  }

  default void onYamcsConnecting() {}

  default void onYamcsConnectionFailed(Throwable t) {}

  /** Called when we the global connection to yamcs was succesfully established */
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
}
