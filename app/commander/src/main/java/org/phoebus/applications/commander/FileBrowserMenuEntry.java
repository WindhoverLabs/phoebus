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
        return CommanderApp.DisplayName;
    }

    @Override
    public String getMenuPath()
    {
        return Messages.MenuPath;
    }

    @Override
    public Image getIcon()
    {
        return ImageCache.getImage(CommanderApp.class, "/icons/filebrowser.png");
    }

    @Override
    public Void call() throws Exception
    {
        ApplicationService.createInstance(CommanderApp.Name);
        return null;
    }
}
