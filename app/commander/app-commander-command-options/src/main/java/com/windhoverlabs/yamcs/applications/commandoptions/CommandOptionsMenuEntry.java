package com.windhoverlabs.yamcs.applications.commandoptions;

import javafx.scene.image.Image;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.MenuEntry;

@SuppressWarnings("nls")
public class CommandOptionsMenuEntry implements MenuEntry {
  @Override
  public String getName() {
    return CommandOptionsApp.Name;
  }

  @Override
  public String getMenuPath() {
    return Messages.MenuPath;
  }

  @Override
  public Image getIcon() {
    return ImageCache.getImage(CommandOptionsApp.class, "/icons/filebrowser.png");
  }

  @Override
  public Void call() throws Exception {
    ApplicationService.createInstance(CommandOptionsApp.Name);
    return null;
  }
}
