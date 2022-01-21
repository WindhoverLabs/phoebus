package com.windhoverlabs.commander.applications.eventlog;

import javafx.scene.image.Image;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.MenuEntry;

@SuppressWarnings("nls")
public class EventLogMenuEntry implements MenuEntry {
  @Override
  public String getName() {
    return EventViewerApp.Name;
  }

  @Override
  public String getMenuPath() {
    return Messages.MenuPath;
  }

  @Override
  public Image getIcon() {
    return ImageCache.getImage(EventViewerApp.class, "/icons/filebrowser.png");
  }

  @Override
  public Void call() throws Exception {
    ApplicationService.createInstance(EventViewerApp.Name);
    return null;
  }
}
