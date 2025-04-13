package com.windhoverlabs.yamcs.applications.links;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class LinksViewerApp implements AppDescriptor {

  public static final String Name = "Links";

  public static final String DisplayName = Messages.DisplayName;

  public static final Logger log = Logger.getLogger(LinksViewerApp.class.getPackageName());

  @Preference public static String css_path;

  static {
    AnnotatedPreferences.initialize(LinksViewerApp.class, "/eventviewer_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  public static String getCSSPath() {
    String path = css_path.trim();
    if (path.isEmpty()) {
      return LinksViewerInstance.class.getResource("/events_style.css").toExternalForm();
    } else {
      return Path.of(path).toUri().toString();
    }
  }

  @Override
  public AppInstance create() {

    if (LinksViewerInstance.INSTANCE == null) {
      try {
        LinksViewerInstance.INSTANCE = new LinksViewerInstance(this);
      } catch (Exception ex) {
        Logger.getLogger(LinksViewerApp.class.getPackageName())
            .log(Level.WARNING, "Cannot create Error Log", ex);
        return null;
      }
    } else {
      LinksViewerInstance.INSTANCE.raise();
    }
    return LinksViewerInstance.INSTANCE;
  }
}
