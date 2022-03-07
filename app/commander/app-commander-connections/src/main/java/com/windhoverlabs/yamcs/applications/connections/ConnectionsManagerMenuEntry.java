package com.windhoverlabs.yamcs.applications.connections;

import javafx.scene.image.Image;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.MenuEntry;

@SuppressWarnings("nls")
public class ConnectionsManagerMenuEntry implements MenuEntry {
  @Override
  public String getName() {
    return ConnectionsManagerApp.Name;
  }

  @Override
  public String getMenuPath() {
    return Messages.MenuPath;
  }

  @Override
  public Image getIcon() {
    return ImageCache.getImage(ConnectionsManagerApp.class, "/icons/filebrowser.png");
  }

  @Override
  public Void call() throws Exception {
    ApplicationService.createInstance(ConnectionsManagerApp.Name);
    return null;
  }
}
