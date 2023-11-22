package com.windhoverlabs.yamcs.applications.parameterviewer;

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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import org.phoebus.framework.autocomplete.PVProposalService;
import org.phoebus.framework.autocomplete.Proposal;
import org.phoebus.framework.autocomplete.ProposalService;
import org.phoebus.pv.PV;
import org.phoebus.pv.PVPool;

public class ParameterViewerController {
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

  public static final Logger log =
      Logger.getLogger(ParameterViewerController.class.getPackageName());

  private final TableView<String> tableView = new TableView<String>();

  TableColumn<String, String> pvColumn = new TableColumn<String, String>("pv");

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

  private ObservableList<String> proposalList = FXCollections.observableArrayList();
  private LinkedHashSet<String> exportSet = new LinkedHashSet<String>();

  private ParameterViewerView paramExportView = new ParameterViewerView();

  public ParameterViewerView getParamExportView() {
    return paramExportView;
  }

  private Tab exportTab;
  @FXML private TabPane exportTabPane;
  @FXML private SplitPane mainSplit;

  public Node getRootPane() {
    return mainSplit;
  }

  @FXML
  public void initialize() {
    tableView.setId("paramExportTable");

    //    pvColumn.setCellValueFactory(new PropertyValueFactory<>("pv"));
    //    pvColumn.setCellValueFactory(cellData -> cellData.getValue().pvProperty());

    pvColumn.setCellValueFactory(
        (pv) -> {
          return new SimpleStringProperty(pv.getValue().toString());
        });

    pvColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(1));
    pvColumn.minWidthProperty().bind(tableView.widthProperty().multiply(1));

    tableView.getColumns().addAll(pvColumn);

    yamcsListener =
        new YamcsAware() {
          public void changeDefaultInstance() {
            tableView.refresh();
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
              tableView.refresh();
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
              tableView.refresh();
            }
          }
        };

    YamcsObjectManager.addYamcsListener(yamcsListener);
    pvTextField.setOnKeyTyped(
        e -> {
          String text = pvTextField.getText();

          proposalService.lookup(
              text,
              (name, priority, proposals) ->
                  handleLookupResult(pvTextField, text, name, priority, proposals));
          if (e.getCode() == KeyCode.ENTER) {
            proposalService.addToHistory(text);
          }
        });
    tableView.setItems(proposalList);
    tableView
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(
            (obs, oldSelection, newSelection) -> {
              System.out.println("Clicked on PV:" + proposalList.get((int) newSelection));
              PV pv = null;
              PV oldPV = null;
              try {
                pv = PVPool.getPV(proposalList.get((int) newSelection));
                if (oldSelection != null) {
                  //                	TODO: Have to think about this one....
                  //                  oldPV = PVPool.getPV(proposalList.get((int) oldSelection));
                  //                  //                	oldPV.
                  //                  PVPool.releasePV(oldPV);
                }
              } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
              pv.onValueEvent()
                  .subscribe(
                      param -> {
                        //                        System.out.println("Current val:" +
                        // param.toString());
                        Platform.runLater(
                            new Runnable() {

                              @Override
                              public void run() {
                                String paramStr = "";
                                //                            	  paramStr += "PV Name:";
                                //                            	  paramStr += "PV Name:" +
                                // VType.class;
                                //                            	  switch(param.getClass())
                                //                            	  {
                                //
                                //                            	  }
                                paramExportView.getCurrentParam().set(param.toString());
                              }
                            });
                      });
              //              value_flow = pv.onValueEvent()
              //                      .throttleLatest(Preferences.update_throttle_ms,
              // TimeUnit.MILLISECONDS)
              //                      .subscribe(this::valueChanged);
              //              pv.onValueEvent().
            });
    tableView.setEditable(true);
    gridPane.add(tableView, 0, 1);
    createParamTab();
    mainSplit.setOrientation(Orientation.VERTICAL);
    mainSplit.setDividerPositions(0.8);
  }

  private void handleLookupResult(
      final javafx.scene.control.TextInputControl field,
      final String text,
      final String name,
      final int priority,
      final List<Proposal> proposals) {
    synchronized (proposalList) {
      proposalList.clear();
      for (Proposal p : proposals) {
        proposalList.add(p.getValue());
      }
    }
  }

  public ParameterViewerController() {}

  public void unInit() {
    YamcsObjectManager.removeListener(yamcsListener);
  }

  private void createParamTab() {
    exportTab = new Tab(Messages.ParameterTabTitle, paramExportView);
    exportTab.setClosable(false);
    //    exportTab.setGraphic(Activator.getIcon("export"));
    exportTabPane.getTabs().add(exportTab);
  }
}
