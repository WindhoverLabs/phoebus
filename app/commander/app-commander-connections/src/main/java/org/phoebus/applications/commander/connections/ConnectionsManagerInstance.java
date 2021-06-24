package org.phoebus.applications.commander.connections;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.phoebus.framework.nls.NLS;
import org.phoebus.framework.persistence.Memento;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;

@SuppressWarnings("nls")
public class ConnectionsManagerInstance implements AppInstance
{
    /** Logger for all file browser code */
    public static final Logger logger = Logger.getLogger(ConnectionsManagerInstance.class.getPackageName());

    /** Memento tags */
    private static final String DIRECTORY = "directory",
                                SHOW_COLUMN = "show_col",
                                WIDTH = "col_width";

    private final AppDescriptor app;

    private ConnectionsManagerController controller;

    public ConnectionsManagerInstance(AppDescriptor app)
    {
        this.app = app;

        final FXMLLoader fxmlLoader;

        Node content;
        try
        {
            final URL fxml = getClass().getResource("Main.fxml");
            final ResourceBundle bundle = NLS.getMessages(ConnectionsManagerInstance.class);
            fxmlLoader = new FXMLLoader(fxml, bundle);
            content = (Node) fxmlLoader.load();
            controller = fxmlLoader.getController();
        }
        catch (IOException ex)
        {
            logger.log(Level.WARNING, "Cannot load UI", ex);
            content = new Label("Cannot load UI");
        }

        final DockItem tab = new DockItem(this, content);
        DockPane.getActiveDockPane().addTab(tab);
                 
        tab.addClosedNotification(controller::shutdown);
    }

    @Override
    public AppDescriptor getAppDescriptor()
    {
        return app;
    }

    @Override
    public void restore(final Memento memento)
    {
    	System.out.println("restore");
    }

    @Override
    public void save(final Memento memento)
    {
    	System.out.println("save");
    }
}
