package com.windhoverlabs.yamcs.applications.commandoptions;

import javafx.scene.image.Image;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.ToolbarEntry;

public class CommandOptionsToolbarEntry implements ToolbarEntry {
  @Override
  public String getName() {
    return CommandOptionsApp.Name;
  }

  @Override
  public Image getIcon() {
    return ImageCache.getImage(CommandOptionsApp.class, "/icons/connections.png");
  }

  @Override
  public Void call() throws Exception {
    ApplicationService.createInstance(CommandOptionsApp.Name);
    return null;
  }
}
