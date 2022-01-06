package com.windhoverlabs.commander.applications.eventlog;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class EventLogApp implements AppDescriptor {

  public static final String Name = "Event Log";

  public static final String DisplayName = Messages.DisplayName;

  /** Initial root directory for newly opened file browser */
  @Preference public static File default_root;

  /** Show hidden files (File.isHidden)? */
  @Preference public static boolean show_hidden;

  static {
    AnnotatedPreferences.initialize(EventLogApp.class, "/eventlog_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  @Override
  public AppInstance create() {

    if (EventLogInstance.INSTANCE == null) {
      try {
        EventLogInstance.INSTANCE = new EventLogInstance(this);
      } catch (Exception ex) {
        Logger.getLogger(EventLogApp.class.getPackageName())
            .log(Level.WARNING, "Cannot create Error Log", ex);
        return null;
      }
    } else {
      EventLogInstance.INSTANCE.getEventLog().updateEvents();
      EventLogInstance.INSTANCE.raise();
    }
    return EventLogInstance.INSTANCE;
  }
}
