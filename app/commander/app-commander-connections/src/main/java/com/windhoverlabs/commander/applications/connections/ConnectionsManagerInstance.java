package com.windhoverlabs.commander.applications.connections;

import javafx.collections.FXCollections;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.phoebus.framework.nls.NLS;
import org.phoebus.framework.persistence.Memento;
import org.phoebus.framework.persistence.XMLUtil;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;

import com.windhoverlabs.commander.core.YamcsServerContext;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * 
 * @author lgomez
 *
 */
@SuppressWarnings("nls")
public class ConnectionsManagerInstance implements AppInstance {
	/** Logger for all file browser code */
	public static final Logger logger = Logger.getLogger(ConnectionsManagerInstance.class.getPackageName());

	/** Memento tags */
	private static final String DIRECTORY = "directory", SHOW_COLUMN = "show_col", WIDTH = "col_width";

	static ConnectionsManagerInstance INSTANCE = null;

	private final AppDescriptor app;

	private ConnectionsManagerController controller;
	
	private Tree serverTree = new Tree(restoreServers());

	private DockItem tab = null;

	public ConnectionsManagerInstance(AppDescriptor app) {
		this.app = app;

		Node content;
//		try {
//			content = loadGUI();
			content = serverTree.getTreeView();
//		} catch (IOException ex) {
//			logger.log(Level.WARNING, "Cannot load UI", ex);
//			content = new Label("Cannot load UI");
//		}

		tab = new DockItem(this, content);
		DockPane.getActiveDockPane().addTab(tab);
		
		
        tab.addCloseCheck(() ->
        {
            INSTANCE = null;
            return CompletableFuture.completedFuture(true);
        });

	}

	private Node loadGUI() throws IOException {
		final FXMLLoader fxmlLoader;
		Node content;
		final URL fxml = getClass().getResource("ConnectionsManager.fxml");
		final ResourceBundle bundle = NLS.getMessages(ConnectionsManagerInstance.class);
		fxmlLoader = new FXMLLoader(fxml, bundle);
		content = (Node) fxmlLoader.load();
		controller = fxmlLoader.getController();

		return content;
	}

	@Override
	public AppDescriptor getAppDescriptor() {
		return app;
	}

	@Override
	public void restore(final Memento memento) {
		System.out.println("restore");
	}

	@Override
	public void save(final Memento memento) {
		String stringToSave = "Hello";
		
		XMLUtil.createTextElement(null, SHOW_COLUMN, DIRECTORY);
	}
	

	public void raise() {
		tab.select();
	}

    private ObservableList<YamcsServer> restoreServers() {
//        YamcsServer server1 = new YamcsServer("Server1");
//        YamcsServer server2 = new YamcsServer("Server2");
//
//        YamcsInstance instance1 = new YamcsInstance("instance1");
//        YamcsInstance instance2 = new YamcsInstance("instance2");
//
//        server1.getItems().add(instance1);
//        server2.getItems().add(instance2);
//
//        return FXCollections.observableArrayList(server1, server2, instance1, instance2);

        return FXCollections.observableArrayList();

    }
}
