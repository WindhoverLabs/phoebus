package com.windhoverlabs.yamcs.applications.packets;

import javafx.scene.image.Image;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.MenuEntry;

@SuppressWarnings("nls")
public class PacketsViewerMenuEntry implements MenuEntry {
  @Override
  public String getName() {
    return PacketsViewerApp.Name;
  }

  @Override
  public String getMenuPath() {
    return Messages.MenuPath;
  }

  @Override
  public Image getIcon() {
    return ImageCache.getImage(PacketsViewerApp.class, "/icons/filebrowser.png");
  }

  @Override
  public Void call() throws Exception {
    ApplicationService.createInstance(PacketsViewerApp.Name);
    return null;
  }
}
