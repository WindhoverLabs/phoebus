package com.windhoverlabs.yamcs.applications.connections;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.statusbar.StatusBar;

@SuppressWarnings("nls")
public class ConnectionsManagerApp implements AppDescriptor {

  public static final String Name = "Connections";

  public static final String DisplayName = Messages.DisplayName;

  private StatusBarConnectionsIndicator statusIndicator = new StatusBarConnectionsIndicator();

  /** Initial root directory for newly opened file browser */
  @Preference public static File default_root;

  /** Show hidden files (File.isHidden)? */
  @Preference public static boolean show_hidden;

  static Logger logger;

  static {
    AnnotatedPreferences.initialize(
        ConnectionsManagerApp.class, "/connections_preferences.properties");
    logger = Logger.getLogger(ConnectionsManagerInstance.class.getPackageName());
  }

  @Override
  public String getName() {
    return Name;
  }

  @Override
  public AppInstance create() {

    if (ConnectionsManagerInstance.INSTANCE == null) {
      try {
        ConnectionsManagerInstance.INSTANCE = new ConnectionsManagerInstance(this);
      } catch (Exception ex) {
        Logger.getLogger(ConnectionsManagerApp.class.getPackageName())
            .log(Level.WARNING, "Cannot create Error Log", ex);
        return null;
      }
    } else ConnectionsManagerInstance.INSTANCE.raise();

    return ConnectionsManagerInstance.INSTANCE;
  }

  @Override
  public void start() {
    Platform.runLater(
        () -> StatusBar.getInstance().addItem(statusIndicator.getConnectionsStatus()));
  }

  @Override
  public void stop() {
    try {
      ConnectionsManagerInstance.createYamcsConnectionMemento();
      ConnectionsManagerInstance.INSTANCE = null;
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
