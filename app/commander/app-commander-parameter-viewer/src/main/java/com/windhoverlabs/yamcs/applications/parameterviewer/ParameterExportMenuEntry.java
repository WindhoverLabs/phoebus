package com.windhoverlabs.yamcs.applications.parameterviewer;

import javafx.scene.image.Image;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.MenuEntry;

@SuppressWarnings("nls")
public class ParameterExportMenuEntry implements MenuEntry {
  @Override
  public String getName() {
    return ParameterExport.Name;
  }

  @Override
  public String getMenuPath() {
    return Messages.MenuPath;
  }

  @Override
  public Image getIcon() {
    return ImageCache.getImage(ParameterExport.class, "/icons/filebrowser.png");
  }

  @Override
  public Void call() throws Exception {
    ApplicationService.createInstance(ParameterExport.Name);
    return null;
  }
}
