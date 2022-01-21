package com.windhoverlabs.commander.applications.events;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class EventViewerApp implements AppDescriptor {

  public static final String Name = "Events";

  public static final String DisplayName = Messages.DisplayName;

  /** Initial root directory for newly opened file browser */
  @Preference public static File default_root;

  /** Show hidden files (File.isHidden)? */
  @Preference public static boolean show_hidden;

  static {
    AnnotatedPreferences.initialize(EventViewerApp.class, "/eventlog_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  @Override
  public AppInstance create() {

    if (EventViewerInstance.INSTANCE == null) {
      try {
        EventViewerInstance.INSTANCE = new EventViewerInstance(this);
      } catch (Exception ex) {
        Logger.getLogger(EventViewerApp.class.getPackageName())
            .log(Level.WARNING, "Cannot create Error Log", ex);
        return null;
      }
    } else {
      EventViewerInstance.INSTANCE.raise();
    }
    return EventViewerInstance.INSTANCE;
  }
}
