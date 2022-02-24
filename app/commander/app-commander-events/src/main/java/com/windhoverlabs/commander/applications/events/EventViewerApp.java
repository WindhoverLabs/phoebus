package com.windhoverlabs.commander.applications.events;

import java.nio.file.Path;
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

  public static final Logger log = Logger.getLogger(EventViewerApp.class.getPackageName());

  @Preference public static String css_path;

  static {
    AnnotatedPreferences.initialize(EventViewerApp.class, "/eventviewer_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  public static String getCSSPath() {
    String path = css_path.trim();
    if (path.isEmpty()) {
      return EventViewerInstance.class.getResource("/events_style.css").toExternalForm();
    } else {
      return Path.of(path).toUri().toString();
    }
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
