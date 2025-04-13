package com.windhoverlabs.yamcs.applications.parameterviewer;

import javafx.scene.image.Image;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.MenuEntry;

@SuppressWarnings("nls")
public class ParameterViewerMenuEntry implements MenuEntry {
  @Override
  public String getName() {
    return ParameterViewer.Name;
  }

  @Override
  public String getMenuPath() {
    return Messages.MenuPath;
  }

  @Override
  public Image getIcon() {
    return ImageCache.getImage(ParameterViewer.class, "/icons/filebrowser.png");
  }

  @Override
  public Void call() throws Exception {
    ApplicationService.createInstance(ParameterViewer.Name);
    return null;
  }
}
