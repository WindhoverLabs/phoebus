package com.windhoverlabs.yamcs.applications.commandoptions;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class CommandOptionsApp implements AppDescriptor {

  public static final String Name = "Command Options";

  public static final String DisplayName = Messages.DisplayName;

  public static final Logger log = Logger.getLogger(CommandOptionsApp.class.getPackageName());

  @Preference public static String css_path;

  static {
    AnnotatedPreferences.initialize(CommandOptionsApp.class, "/eventviewer_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  public static String getCSSPath() {
    String path = css_path.trim();
    if (path.isEmpty()) {
      return CommandOptionsInstance.class.getResource("/events_style.css").toExternalForm();
    } else {
      return Path.of(path).toUri().toString();
    }
  }

  @Override
  public AppInstance create() {

    if (CommandOptionsInstance.INSTANCE == null) {
      try {
        CommandOptionsInstance.INSTANCE = new CommandOptionsInstance(this);
      } catch (Exception ex) {
        Logger.getLogger(CommandOptionsApp.class.getPackageName())
            .log(Level.WARNING, "Cannot create Error Log", ex);
        return null;
      }
    } else {
      CommandOptionsInstance.INSTANCE.raise();
    }
    return CommandOptionsInstance.INSTANCE;
  }
}
