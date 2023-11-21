package com.windhoverlabs.yamcs.applications.commandhistory;

import javafx.scene.image.Image;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.MenuEntry;

@SuppressWarnings("nls")
public class CommandHistoryMenuEntry implements MenuEntry {
  @Override
  public String getName() {
    return CommandHistoryApp.Name;
  }

  @Override
  public String getMenuPath() {
    return Messages.MenuPath;
  }

  @Override
  public Image getIcon() {
    return ImageCache.getImage(CommandHistoryApp.class, "/icons/filebrowser.png");
  }

  @Override
  public Void call() throws Exception {
    ApplicationService.createInstance(CommandHistoryApp.Name);
    return null;
  }
}
