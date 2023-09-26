package com.windhoverlabs.yamcs.applications.commandhistory;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class CommandHistoryApp implements AppDescriptor {

  public static final String Name = "CommandHistory";

  public static final String DisplayName = Messages.DisplayName;

  public static final Logger log = Logger.getLogger(CommandHistoryApp.class.getPackageName());

  @Preference public static String css_path;

  static {
    AnnotatedPreferences.initialize(CommandHistoryApp.class, "/eventviewer_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  @Override
  public AppInstance create() {

    if (CommandHistoryInstance.INSTANCE == null) {
      try {
        CommandHistoryInstance.INSTANCE = new CommandHistoryInstance(this);
      } catch (Exception ex) {
        Logger.getLogger(CommandHistoryApp.class.getPackageName())
            .log(Level.WARNING, "Cannot create Error Log", ex);
        return null;
      }
    } else {
      CommandHistoryInstance.INSTANCE.raise();
    }
    return CommandHistoryInstance.INSTANCE;
  }
}
