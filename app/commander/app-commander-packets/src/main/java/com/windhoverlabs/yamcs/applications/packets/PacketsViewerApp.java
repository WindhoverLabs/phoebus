package com.windhoverlabs.yamcs.applications.packets;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class PacketsViewerApp implements AppDescriptor {

  public static final String Name = "Packets";

  public static final String DisplayName = Messages.DisplayName;

  public static final Logger log = Logger.getLogger(PacketsViewerApp.class.getPackageName());

  @Preference public static String css_path;

  static {
    AnnotatedPreferences.initialize(PacketsViewerApp.class, "/eventviewer_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  public static String getCSSPath() {
    String path = css_path.trim();
    if (path.isEmpty()) {
      return PacketsViewerInstance.class.getResource("/events_style.css").toExternalForm();
    } else {
      return Path.of(path).toUri().toString();
    }
  }

  @Override
  public AppInstance create() {

    if (PacketsViewerInstance.INSTANCE == null) {
      try {
        PacketsViewerInstance.INSTANCE = new PacketsViewerInstance(this);
      } catch (Exception ex) {
        Logger.getLogger(PacketsViewerApp.class.getPackageName())
            .log(Level.WARNING, "Cannot create Error Log", ex);
        return null;
      }
    } else {
      PacketsViewerInstance.INSTANCE.raise();
    }
    return PacketsViewerInstance.INSTANCE;
  }
}
