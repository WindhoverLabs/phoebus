package com.windhoverlabs.yamcs.applications.parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.phoebus.framework.persistence.MementoTree;
import org.phoebus.framework.persistence.XMLMementoTree;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.framework.workbench.Locations;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;

/** @author lgomez */
@SuppressWarnings("nls")
public class ParameterExportViewerInstance implements AppInstance {
  private static final String YAMCS_PARAMETER_EXPORT_MEMENTO_FILENAME =
      "yamcs_parameter_export_memento";

  /** Logger for all file browser code */
  public static final Logger logger =
      Logger.getLogger(ParameterExportViewerInstance.class.getPackageName());

  /** Memento tags */
  private static final String EXPORT_START = "yamcs_export_start", EXPORT_END = "yamcs_export_end";

  static ParameterExportViewerInstance INSTANCE;

  private FXMLLoader loader;

  private ParameterExportController parameterExportInstanceController = null;

  private final AppDescriptor app;

  private DockItem tab = null;

  public ParameterExportViewerInstance(AppDescriptor app) {
    this.app = app;
    Node content = null;
    ResourceBundle resourceBundle = NLS.getMessages(Messages.class);
    FXMLLoader loader = new FXMLLoader();
    loader.setResources(resourceBundle);
    loader.setLocation(this.getClass().getResource("ExportView.fxml"));

    try {
      content = loader.load();
      parameterExportInstanceController = loader.getController();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    tab = new DockItem(this, content);
    DockPane.getActiveDockPane().addTab(tab);
    tab.addCloseCheck(
        () -> {
          INSTANCE = null;
          parameterExportInstanceController.unInit();
          return CompletableFuture.completedFuture(true);
        });
  }

  @Override
  public AppDescriptor getAppDescriptor() {
    return app;
  }

  @Override
  public void restore(final Memento memento) {
    // TODO: Move "new Tree(restoreServers());" here.
    parameterExportInstanceController
        .getParamExportView()
        .setStart(memento.getString(EXPORT_START).orElse(""));
    memento.getString(EXPORT_END);
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
      createParameterExportMemento();
    } catch (Exception ex) {
      logger.log(Level.WARNING, "Error writing saved state to " + "", ex);
    }
  }

  private void createParameterExportMemento() throws Exception, FileNotFoundException {

    logger.info("Saving CSV Exporter state...");
    final XMLMementoTree csvExporterMemento = XMLMementoTree.create();
    MementoTree exportData =
        csvExporterMemento.createChild(YAMCS_PARAMETER_EXPORT_MEMENTO_FILENAME);

    boolean saveMemento = true;

    if (isViewerValid()) {
      exportData.setString(
          EXPORT_START, parameterExportInstanceController.getParamExportView().getStart());
      exportData.setString(
          EXPORT_END, parameterExportInstanceController.getParamExportView().getEnd());
      saveMemento = false;
    }
    if (saveMemento) {
      csvExporterMemento.write(
          new FileOutputStream(
              new File(Locations.user(), YAMCS_PARAMETER_EXPORT_MEMENTO_FILENAME)));
    } else {
      logger.info("Ignoring invalid fields. Only the last valid state will be saved.");
    }
  }

  private boolean isViewerValid() {
    return (!parameterExportInstanceController.getParamExportView().getStart().isBlank()
            && !parameterExportInstanceController.getParamExportView().getStart().isEmpty())
        && (!parameterExportInstanceController.getParamExportView().getEnd().isBlank()
            && !parameterExportInstanceController.getParamExportView().getEnd().isEmpty());
  }

  public void raise() {
    tab.select();
  }

  public ParameterExportController getController() {
    return parameterExportInstanceController;
  }

  private static ObservableList<String> restoreEvents() {
    // TODO:Implement memento pattern
    return FXCollections.observableArrayList(new ArrayList<String>());
  }
}
