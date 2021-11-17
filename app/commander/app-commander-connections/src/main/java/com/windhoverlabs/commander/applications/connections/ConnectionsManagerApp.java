package com.windhoverlabs.commander.applications.connections;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;

@SuppressWarnings("nls")
public class ConnectionsManagerApp implements AppDescriptor {

    public static final String Name = "Connections";

    public static final String DisplayName = Messages.DisplayName;

    /** Initial root directory for newly opened file browser */
    @Preference public static File default_root;

    /** Show hidden files (File.isHidden)? */
    @Preference public static boolean show_hidden;

    static
    {
    	AnnotatedPreferences.initialize(ConnectionsManagerApp.class, "/connections_preferences.properties");
    }

    @Override
    public String getName() {
        return Name;
    }
	@Override
	public AppInstance create() {
    	
        if (ConnectionsManagerInstance.INSTANCE == null)
        {
            try
            {
            	ConnectionsManagerInstance.INSTANCE = new ConnectionsManagerInstance(this);
            }
            catch (Exception ex)
            {
                Logger.getLogger(ConnectionsManagerApp.class.getPackageName())
                      .log(Level.WARNING, "Cannot create Error Log", ex);
                return null;
            }
        }
        else
        	ConnectionsManagerInstance.INSTANCE.raise();
        
        return ConnectionsManagerInstance.INSTANCE;
	}
}
