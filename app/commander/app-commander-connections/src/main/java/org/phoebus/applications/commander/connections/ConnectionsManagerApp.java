package org.phoebus.applications.commander.connections;

import java.io.File;
import java.net.URI;

import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.framework.spi.AppResourceDescriptor;

@SuppressWarnings("nls")
public class ConnectionsManagerApp implements AppResourceDescriptor {

    public static final String Name = "connections";

    public static final String DisplayName = Messages.DisplayName;

    /** Initial root directory for newly opened file browser */
    @Preference public static File default_root;

    /** Show hidden files (File.isHidden)? */
    @Preference public static boolean show_hidden;

    static
    {
    	AnnotatedPreferences.initialize(ConnectionsManagerApp.class, "/filebrowser_preferences.properties");
    }

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getDisplayName()
    {
        return DisplayName;
    }

    @Override
    public AppInstance create() {
        return createWithRoot(default_root);
    }

    @Override
    public AppInstance create(final URI resource)
    {
        return createWithRoot(new File(resource));
    }

    public AppInstance createWithRoot(final File directory)
    {
        return new ConnectionsManagerController(this, directory);
    }
}
