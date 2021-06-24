package org.windhoverlabs.applications.commander.map;

import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.MenuEntry;

import javafx.scene.image.Image;

@SuppressWarnings("nls")
public class MissionMapMenuEntry implements MenuEntry
{
    @Override
    public String getName() {
        return MissionMapApp.DisplayName;
    }

    @Override
    public String getMenuPath()
    {
        return Messages.MenuPath;
    }

    @Override
    public Image getIcon()
    {
        return ImageCache.getImage(MissionMapApp.class, "/icons/filebrowser.png");
    }

    @Override
    public Void call() throws Exception
    {
        ApplicationService.createInstance(MissionMapApp.Name);
        return null;
    }
}
