package org.phoebus.applications.commander.connections;

import static org.phoebus.applications.commander.connections.ConnectionsManagerInstance.logger;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.phoebus.framework.selection.SelectionService;
import org.phoebus.framework.spi.AppResourceDescriptor;
import org.phoebus.framework.util.ResourceParser;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.application.ApplicationLauncherService;
import org.phoebus.ui.application.ContextMenuService;
import org.phoebus.ui.application.PhoebusApplication;
import org.phoebus.ui.dialog.DialogHelper;
import org.phoebus.ui.javafx.ImageCache;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.phoebus.ui.spi.ContextMenuEntry;

/**
 * Controller for the commander app
 *
 * @lgomez
 */
@SuppressWarnings("nls")
public class ConnectionsManagerController {

    public ConnectionsManagerController()
    {
    }

    @FXML
    public void initialize() {
    }

    /** Call when no longer needed */
    public void shutdown()
    {
       System.out.println("shutdown");
    }
}
