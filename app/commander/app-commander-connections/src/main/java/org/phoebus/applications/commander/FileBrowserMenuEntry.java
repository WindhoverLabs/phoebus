package org.phoebus.applications.commander;

import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.MenuEntry;

import javafx.scene.image.Image;

@SuppressWarnings("nls")
public class FileBrowserMenuEntry implements MenuEntry
{
    @Override
    public String getName() {
        return ConnectionsManagerApp.DisplayName;
    }

    @Override
    public String getMenuPath()
    {
        return Messages.MenuPath;
    }

    @Override
    public Image getIcon()
    {
        return ImageCache.getImage(ConnectionsManagerApp.class, "/icons/filebrowser.png");
    }

    @Override
    public Void call() throws Exception
    {
        ApplicationService.createInstance(ConnectionsManagerApp.Name);
        return null;
    }
}
