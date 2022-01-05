package com.windhoverlabs.commander.applications.eventlog;

import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.MenuEntry;

import javafx.scene.image.Image;

@SuppressWarnings("nls")
public class EventLogMenuEntry implements MenuEntry {
  @Override
  public String getName() {
    return EventLogApp.Name;
  }

  @Override
  public String getMenuPath() {
    return Messages.MenuPath;
  }

  @Override
  public Image getIcon() {
    return ImageCache.getImage(EventLogApp.class, "/icons/filebrowser.png");
  }

  @Override
  public Void call() throws Exception {
    ApplicationService.createInstance(EventLogApp.Name);
    return null;
  }
}
