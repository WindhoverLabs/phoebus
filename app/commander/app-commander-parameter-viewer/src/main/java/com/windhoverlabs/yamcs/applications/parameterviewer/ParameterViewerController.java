package com.windhoverlabs.yamcs.applications.parameterviewer;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_Event;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.Observable;
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
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.epics.vtype.VBoolean;
import org.epics.vtype.VInt;
import org.epics.vtype.VNumber;
import org.epics.vtype.VString;
import org.phoebus.framework.autocomplete.PVProposalService;
import org.phoebus.framework.autocomplete.Proposal;
import org.phoebus.framework.autocomplete.ProposalService;
import org.phoebus.pv.PV;
import org.phoebus.pv.PVPool;

public class ParameterViewerController {
  class ViewablePV {
    private final SimpleBooleanProperty view = new SimpleBooleanProperty();
    private final SimpleStringProperty param = new SimpleStringProperty();

    ViewablePV(String pv, boolean export) {
      this.param.set(pv);
      this.view.set(export);
    }

    public final SimpleStringProperty paramProperty() {
      return param;
    }

    public final SimpleBooleanProperty viewProperty() {
      return view;
    }

    public void toggleView() {
      if (view.get()) {
        view.set(false);
      } else {
        view.set(true);
      }
    }
  }

  public static final Logger log =
      Logger.getLogger(ParameterViewerController.class.getPackageName());

  private final TableView<ViewablePV> tableView = new TableView<ViewablePV>();

  TableColumn<ViewablePV, Boolean> viewColumn = new TableColumn<ViewablePV, Boolean>("view");

  TableColumn<ViewablePV, String> pvColumn = new TableColumn<ViewablePV, String>("pv");

  private ObservableList<CMDR_Event> data =
      FXCollections.observableArrayList(new ArrayList<CMDR_Event>());
  private static final int dataSize = 10_023;

  private ProposalService proposalService = PVProposalService.INSTANCE;

  YamcsAware yamcsListener = null;

  @FXML private GridPane gridPane;

  @FXML private TextField pvTextField;

  private ObservableList<ViewablePV> proposalList = FXCollections.observableArrayList();
  private LinkedHashSet<String> viewableSet = new LinkedHashSet<String>();

  private ParameterViewerView paramsView = new ParameterViewerView();

  public ParameterViewerView getParamExportView() {
    return paramsView;
  }

  private Tab exportTab;
  @FXML private TabPane exportTabPane;
  @FXML private SplitPane mainSplit;

  private String oldPVName = null;

  private @NonNull Disposable oldSub;

  private String currentPVName;

  public Node getRootPane() {
    return mainSplit;
  }

  @FXML
  public void initialize() {
    tableView.setId("paramViewTable");

    viewColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.5));
    pvColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.5));
    viewColumn.minWidthProperty().bind(tableView.widthProperty().multiply(0.5));
    pvColumn.minWidthProperty().bind(tableView.widthProperty().multiply(0.5));
    viewColumn.setCellValueFactory(cellData -> cellData.getValue().viewProperty());
    viewColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

    pvColumn.setCellValueFactory(
        (pv) -> {
          return pv.getValue().param;
        });

    tableView.getColumns().addAll(pvColumn, viewColumn);

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

    tableView.setOnKeyPressed(
        e -> {
          if (e.getCode() == KeyCode.ENTER) {
            var selection = tableView.getSelectionModel().getSelectedItem();
            if (selection != null) {
              selection.toggleView();
            }
          }
        });

    this.proposalList =
        FXCollections.observableArrayList(
            new Callback<ViewablePV, Observable[]>() {

              @Override
              public Observable[] call(ViewablePV param) {
                return new Observable[] {param.viewProperty()};
              }
            });

    this.proposalList.addListener(
        new ListChangeListener<ViewablePV>() {

          @Override
          public void onChanged(ListChangeListener.Change<? extends ViewablePV> c) {
            while (c.next()) {
              if (c.wasUpdated()) {
                ViewablePV item = proposalList.get(c.getFrom());
                if (item.viewProperty().get()) {
                  viewableSet.add(item.paramProperty().get());
                } else {
                  viewableSet.remove(item.paramProperty().get());
                }
                paramsView.getParameters().clear();
                paramsView.updateParams(viewableSet);
              }
            }
          }
        });

    tableView.setItems(proposalList);
    tableView
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(
            (obs, oldSelection, newSelection) -> {
              if (newSelection.intValue() == -1) {
                return;
              }
              PV pv = null;
              PV oldPV = null;

              try {
                System.out.println("newSelection-->" + newSelection);
                System.out.println("oldSelection-->" + oldSelection);
                currentPVName = proposalList.get((int) newSelection).paramProperty().get();
                pv = PVPool.getPV(proposalList.get((int) newSelection).paramProperty().get());
                if (oldPVName != null) {
                  // TODO: Have to think about this one....
                  oldSub.dispose();
                  oldPV = PVPool.getPV(oldPVName);
                  if (oldPV != null) {
                    PVPool.releasePV(oldPV);
                  }
                  oldPVName = proposalList.get((int) newSelection).paramProperty().get();
                }
              } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }

              if (pv != null) {
                oldSub = pvSubscription(pv);
              }
              //              value_flow = pv.onValueEvent()
              //                      .throttleLatest(Preferences.update_throttle_ms,
              // TimeUnit.MILLISECONDS)
              //                      .subscribe(this::valueChanged);
              //              pv.onValueEvent().
              oldPVName = proposalList.get((int) newSelection).paramProperty().get();
            });
    tableView.setEditable(true);
    gridPane.add(tableView, 0, 1);
    createParamTab();
    mainSplit.setOrientation(Orientation.VERTICAL);
    mainSplit.setDividerPositions(0.8);
  }

  private @NonNull Disposable pvSubscription(PV pv) {
    return pv.onValueEvent()
        .subscribe(
            param -> {
              Platform.runLater(
                  new Runnable() {

                    @Override
                    public void run() {
                      String paramStr = "PV name:" + currentPVName;
                      paramStr +=
                          "\nValue:"
                              + org.phoebus.ui.vtype.FormatOptionHandler.format(
                                  param, org.phoebus.ui.vtype.FormatOption.DEFAULT, -1, true);

                      //                                    System.out.println(
                      //                                        "VType Format:"
                      //                                            +
                      // org.phoebus.ui.vtype.FormatOptionHandler.format(
                      //                                                param,
                      //
                      // org.phoebus.ui.vtype.FormatOption.DEFAULT,
                      //                                                -1,
                      //                                                true));
                      //                            	  paramStr += "PV Name:";
                      //                            	  paramStr += "PV Name:" +
                      // VType.class;
                      //                            	  switch(param.getClass())
                      //                            	  {
                      //
                      //                            	  }
                      //                                Object pType =

                      // VType.typeOf(param);
                      //                                    System.out.println("class
                      // name:" + param.getClass());
                      if (param instanceof VInt) {
                        paramStr += "\nType:VInt";
                      } else if (param instanceof VNumber) {
                        paramStr += "\nType:VNumber";
                      } else if (param instanceof VString) {
                        paramStr += "\nType:VString";
                      } else if (param instanceof VBoolean) {
                        paramStr += "\nType:VBoolean";
                      }
                      if (param instanceof com.windhoverlabs.data.yamcs.YamcsVType) {
                        paramStr += "\nType:VNumber";
                        System.out.println("YamcsVType***");
                      }

                      if (param instanceof com.windhoverlabs.data.yamcs.YamcsVType) {
                        System.out.println("com.windhoverlabs.data.yamcs.YamcsVType");
                      }

                      //                                TODO:Would be nice to get mdb
                      // data
                      // from YAMCS and display it(offsets, xtce type, etc)
                      //                                if(param instanceof
                      // com.windhoverlabs.pv.yamcs.YamcsPV)
                      //                                {
                      //
                      //                                }
                      //                        else if (param instanceof
                      // VFloat) {
                      //                                  paramStr += "Type: VFloat";
                      //                                } else if (param instanceof
                      // VULong) {
                      //                                  paramStr += "Type: VULong";
                      //                                } else if (param instanceof
                      // VLong)
                      // {
                      //                                  paramStr += "\nType: VLong";
                      //                                } else if (param instanceof
                      // VUInt)
                      // {
                      //                                  paramStr += "Type: VUInt";
                      //                                } else if (param instanceof
                      // VInt)
                      // {
                      //                                  paramStr += "Type: VInt";
                      //                                } else if (param instanceof
                      // VEnum)
                      // {
                      //                                  paramStr += "Type: VEnum";
                      //                                } else if (param instanceof
                      // VBoolean) {
                      //                                  paramStr += "Type:
                      // VBoolean";
                      //                                } else if (param instanceof
                      // VString) {
                      //                                  paramStr += "Type: VString";
                      //                                } else {
                      //                                  paramStr += "Type: Unknown";
                      //                                }
                      //                                if (pType instanceof VDouble )
                      //                                {
                      //                                    paramStr += "\nType:
                      // VDouble";
                      //                                }
                      paramsView.getCurrentParam().set(paramStr);
                      paramsView.updateParamValue(paramStr, currentPVName);
                      //                                param.toVType(obs);
                      //                                VType.typeOf(obs);
                    }
                  });
            });
  }

  private void handleLookupResult(
      final javafx.scene.control.TextInputControl field,
      final String text,
      final String name,
      final int priority,
      final List<Proposal> proposals) {
    synchronized (proposalList) {
      proposalList.clear();
      //      for (Proposal p : proposals) {
      //        proposalList.add(p.getValue());
      //      }

      viewableSet.forEach(
          item -> {
            proposalList.add(new ViewablePV(item, true));
          });
      for (Proposal p : proposals) {
        proposalList.add(new ViewablePV(p.getValue(), false));
      }
    }
  }

  public ParameterViewerController() {}

  public void unInit() {
    YamcsObjectManager.removeListener(yamcsListener);
  }

  private void createParamTab() {
    exportTab = new Tab(Messages.ParameterTabTitle, paramsView);
    exportTab.setClosable(false);
    //    exportTab.setGraphic(Activator.getIcon("export"));
    exportTabPane.getTabs().add(exportTab);
  }
}
