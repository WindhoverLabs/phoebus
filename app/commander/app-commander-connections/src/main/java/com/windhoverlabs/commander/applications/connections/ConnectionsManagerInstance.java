package com.windhoverlabs.commander.applications.connections;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import org.phoebus.framework.nls.NLS;
import org.phoebus.framework.persistence.Memento;
import org.phoebus.framework.persistence.XMLUtil;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;
import org.w3c.dom.*;

import com.windhoverlabs.commander.core.YamcsContext;

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

	private DockItem tab = null;

	public ConnectionsManagerInstance(AppDescriptor app) {
		this.app = app;

		Node content;
		try {
			content = loadGUI();
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Cannot load UI", ex);
			content = new Label("Cannot load UI");
		}

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
	
	
	
    public void write(final YamcsContext model, final OutputStream stream) throws Exception
    {
//        Boolean saveRestore = model.isSaveRestoreEnabled();

        final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final Element root = doc.createElement("pvtable");
        root.setAttribute("version", "3.0");
//        root.setAttribute(ENABLE_SAVE_RESTORE, saveRestore.toString());
        doc.appendChild(root);

        root.appendChild(XMLUtil.createTextElement(doc, "Data", "Hello"));

        final Element pvs = doc.createElement("YamcsConnection");
//        for (PVTableItem item : model.getItems())
//        {
//            Element pv = doc.createElement(PV);
//
//            pv.appendChild(XMLUtil.createTextElement(doc, SELECTED, Boolean.toString(item.isSelected())));
//            pv.appendChild(XMLUtil.createTextElement(doc, NAME, item.getName()));
//            pv.appendChild(XMLUtil.createTextElement(doc, TOLERANCE, Double.toString(item.getTolerance())));
//
//            if (saveRestore)
//            {
//                pv.appendChild(XMLUtil.createTextElement(doc, SAVED_TIME, item.getTime_saved()));
//
//                final SavedValue saved = item.getSavedValue().orElse(null);
//                if (saved instanceof SavedScalarValue)
//                    pv.appendChild(XMLUtil.createTextElement(doc, SAVED_VALUE, saved.toString()));
//                else if (saved instanceof SavedArrayValue)
//                {
//                    final SavedArrayValue array = (SavedArrayValue) saved;
//                    final Element el = doc.createElement(SAVED_ARRAY);
//
//                    final int N = array.size();
//                    for (int i=0; i<N; ++i)
//                        el.appendChild(XMLUtil.createTextElement(doc, ITEM, array.get(i)));
//
//                    pv.appendChild(el);
//
//                }
//
//                pv.appendChild(XMLUtil.createTextElement(doc, COMPLETION, Boolean.toString(item.isUsingCompletion())));
//            }
//
//            pvs.appendChild(pv);
//        }
//        root.appendChild(pvs);
        
        root.appendChild(doc);

        XMLUtil.writeDocument(doc, stream);
    }

	public void raise() {
		tab.select();
	}
}
