package com.windhoverlabs.yamcs.applications.xtce;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_Event;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.csstudio.trends.databrowser3.Activator;
import org.csstudio.trends.databrowser3.Messages;
import org.phoebus.framework.autocomplete.PVProposalService;
import org.phoebus.framework.autocomplete.Proposal;
import org.phoebus.framework.autocomplete.ProposalService;

public class XTCEExportController {
  class ExportPV {
    private final SimpleBooleanProperty export = new SimpleBooleanProperty();
    private final SimpleStringProperty pv = new SimpleStringProperty();

    ExportPV(String pv, boolean export) {
      this.pv.set(pv);
      this.export.set(export);
    }

    public final SimpleStringProperty pvProperty() {
      return pv;
    }

    public final SimpleBooleanProperty exportProperty() {
      return export;
    }

    public void toggleExport() {
      if (export.get()) {
        export.set(false);
      } else {
        export.set(true);
      }
    }
  }

  public static final Logger log = Logger.getLogger(XTCEExportController.class.getPackageName());

  private ObservableList<CMDR_Event> data =
      FXCollections.observableArrayList(new ArrayList<CMDR_Event>());
  private static final int dataSize = 10_023;

  private ProposalService proposalService = PVProposalService.INSTANCE;

  // TODO:Eventually these will be in spinner nodes. These are the event filters.
  private String currentServer = "sitl";
  private String currentInstance = "yamcs-cfs";

  YamcsAware yamcsListener = null;

  @FXML private GridPane gridPane;

  @FXML private TextField pvTextField;

  private LinkedHashSet<String> exportSet = new LinkedHashSet<String>();

  private ExportView paramExportView = new ExportView();

  public ExportView getParamExportView() {
    return paramExportView;
  }

  private Tab exportTab;
  @FXML private Pane mainPane;

  public Node getRootPane() {
    return mainPane;
  }

  @FXML
  public void initialize() {
    mainPane.setId("mainExportXTCEPane");
    ;

    yamcsListener =
        new YamcsAware() {
          public void changeDefaultInstance() {
            //            tableView.refresh();
          }

          public void onYamcsConnected() {
            if (YamcsObjectManager.getDefaultInstance() != null) {
              YamcsObjectManager.getDefaultInstance()
                  .getEvents()
                  .addListener(
                      new ListChangeListener<Object>() {
                        @Override
                        public void onChanged(Change<?> c) {
                          Platform.runLater(() -> {});
                        }
                      });
              //              tableView.refresh();
            }
          }

          public void onInstancesReady(YamcsServer s) {
            if (s.getDefaultInstance() != null) {
              s.getDefaultInstance()
                  .getEvents()
                  .addListener(
                      new ListChangeListener<Object>() {
                        @Override
                        public void onChanged(Change<?> c) {
                          Platform.runLater(() -> {});
                        }
                      });
              //              tableView.refresh();
            }
          }
        };

    YamcsObjectManager.addYamcsListener(yamcsListener);
    //    gridPane.add(tableView, 0, 1);
    createExportTab();
    //    mainPane.setOrientation(Orientation.VERTICAL);
    //    mainPane.setDividerPositions(0.8);
  }

  private void handleLookupResult(
      final javafx.scene.control.TextInputControl field,
      final String text,
      final String name,
      final int priority,
      final List<Proposal> proposals) {}

  public XTCEExportController() {}

  public void unInit() {
    YamcsObjectManager.removeListener(yamcsListener);
  }

  private void createExportTab() {
    exportTab = new Tab(Messages.Export, paramExportView);
    exportTab.setClosable(false);
    exportTab.setGraphic(Activator.getIcon("export"));
  }
}
