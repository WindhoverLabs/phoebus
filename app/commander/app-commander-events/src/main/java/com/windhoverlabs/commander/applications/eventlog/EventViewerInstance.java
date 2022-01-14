package com.windhoverlabs.commander.applications.eventlog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.phoebus.framework.nls.NLS;
import org.phoebus.framework.persistence.Memento;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;

/** @author lgomez */
@SuppressWarnings("nls")
public class EventViewerInstance implements AppInstance {
  private static final String YAMCS_EVENTS_MEMENTO_FILENAME = "yamcs_events_memento";

  /** Logger for all file browser code */
  public static final Logger logger = Logger.getLogger(EventViewerInstance.class.getPackageName());

  /** Memento tags */
  private static final String YAMCS_EVENTS = "yamcs_events", YAMCS_EVENT_MESSAGE = "message";

  static EventViewerInstance INSTANCE;

  private final AppDescriptor app;

  // TODO: Refactor Tree constructor since we don't need to pass the list of servers anymore.
  private static EventViewerController eventLog = new EventViewerController();

  private DockItem tab = null;

  public EventViewerInstance(AppDescriptor app) {
    this.app = app;
    Node content = null;
    ResourceBundle resourceBundle = NLS.getMessages(Messages.class);
    FXMLLoader loader = new FXMLLoader();
    loader.setResources(resourceBundle);
    loader.setLocation(this.getClass().getResource("EventView.fxml"));

    try {
      content = loader.load();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    tab = new DockItem(this, content);
    DockPane.getActiveDockPane().addTab(tab);
    tab.addCloseCheck(
        () -> {
          INSTANCE = null;
          return CompletableFuture.completedFuture(true);
        });
  }

  public static EventViewerController getEventLog() {
    return eventLog;
  }

  @Override
  public AppDescriptor getAppDescriptor() {
    return app;
  }

  @Override
  public void restore(final Memento memento) {
    // TODO: Move "new Tree(restoreServers());" here.
  }

  @Override
  public void save(final Memento memento) {
    // TODO:Implement memento pattern
    try {
    } catch (Exception e) {
      logger.warning("Error saving Events    connections...:" + e.toString());
    }
    logger.info("Saving Yamcs Events...");

    // Save yamcs connections
    try {
      createEventsMemento();
    } catch (Exception ex) {
      logger.log(Level.WARNING, "Error writing saved state to " + "", ex);
    }
  }

  private void createEventsMemento() throws Exception, FileNotFoundException {
    // TODO:Implement
  }

  public void raise() {
    tab.select();
  }

  private static ObservableList<String> restoreEvents() {
    // TODO:Implement memento pattern
    return FXCollections.observableArrayList(new ArrayList<String>());
  }
}
