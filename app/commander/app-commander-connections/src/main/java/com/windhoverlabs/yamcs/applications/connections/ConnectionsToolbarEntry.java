package com.windhoverlabs.yamcs.applications.connections;

import javafx.scene.image.Image;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.ToolbarEntry;

public class ConnectionsToolbarEntry implements ToolbarEntry {
  @Override
  public String getName() {
    return ConnectionsManagerApp.Name;
  }

  @Override
  public Image getIcon() {
    return ImageCache.getImage(ConnectionsManagerApp.class, "/icons/connections.png");
  }

  @Override
  public Void call() throws Exception {
    ApplicationService.createInstance(ConnectionsManagerApp.Name);
    return null;
  }
}
