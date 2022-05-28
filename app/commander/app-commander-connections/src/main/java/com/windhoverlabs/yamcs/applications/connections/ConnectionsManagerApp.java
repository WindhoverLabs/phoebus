package com.windhoverlabs.yamcs.applications.connections;

import java.io.File;
import java.util.logging.Logger;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class ConnectionsManagerApp implements AppDescriptor {

  public static final String Name = "Connections";

  public static final String DisplayName = Messages.DisplayName;

  /** Initial root directory for newly opened file browser */
  @Preference public static File default_root;

  /** Show hidden files (File.isHidden)? */
  @Preference public static boolean show_hidden;

  static Logger logger;

  static {
    AnnotatedPreferences.initialize(
        ConnectionsManagerApp.class, "/connections_preferences.properties");
  }

  @Override
  public String getName() {
    return Name;
  }

  @Override
  public AppInstance create() {
    System.out.println("*********************************************2");
    return null;
  }

  @Override
  public void stop() {
    System.out.println("*********************************************3");
  }
}
