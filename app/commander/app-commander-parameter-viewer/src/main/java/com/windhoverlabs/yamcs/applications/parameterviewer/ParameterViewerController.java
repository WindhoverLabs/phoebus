package com.windhoverlabs.yamcs.applications.parameterviewer;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_Event;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.epics.vtype.TimeProvider;
import org.epics.vtype.VBoolean;
import org.epics.vtype.VDouble;
import org.epics.vtype.VEnum;
import org.epics.vtype.VFloat;
import org.epics.vtype.VInt;
import org.epics.vtype.VLong;
import org.epics.vtype.VString;
import org.epics.vtype.VUInt;
import org.epics.vtype.VULong;
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

  private HashMap<String, Disposable> oldSubs = new HashMap<String, Disposable>();

  private String currentPVName;

  public Node getRootPane() {
    return mainSplit;
  }

  @FXML
  public void initialize() {
    tableView.setId("paramViewTable");

    viewColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.1));
    pvColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.9));
    viewColumn.minWidthProperty().bind(tableView.widthProperty().multiply(0.1));
    pvColumn.minWidthProperty().bind(tableView.widthProperty().multiply(0.9));
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
                PV pv = null;
                boolean newPV = false;
                if (item.viewProperty().get()) {
                  newPV = viewableSet.add(item.paramProperty().get());
                  try {
                    pv = PVPool.getPV(item.paramProperty().get());
                  } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                  if (pv != null && newPV) {
                    oldSubs.put(
                        item.paramProperty().get(), pvSubscription(pv, item.paramProperty().get()));
                  }
                } else {
                  viewableSet.remove(item.paramProperty().get());
                  try {
                    pv = PVPool.getPV(item.paramProperty().get());
                  } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                  if (pv != null && !newPV) {
                    //						pvSubscription(pv);
                    oldSubs.get(item.paramProperty().get()).dispose();
                    paramsView.removeParam(item.paramProperty().get());
                  }
                }
                paramsView.getParameters().clear();
                paramsView.updateParams(viewableSet);
              }
            }
          }
        });

    tableView.setItems(proposalList);
    tableView.setEditable(true);
    gridPane.add(tableView, 0, 1);
    createParamTab();
    mainSplit.setOrientation(Orientation.VERTICAL);
    mainSplit.setDividerPositions(0.8);
  }

  private @NonNull Disposable pvSubscription(PV pv, String PvName) {
    return pv.onValueEvent()
        .subscribe(
            param -> {
              Platform.runLater(
                  new Runnable() {

                    @Override
                    public void run() {
                      String paramStr = "" + PvName;
                      paramStr +=
                          "\nValue:"
                              + org.phoebus.ui.vtype.FormatOptionHandler.format(
                                  param, org.phoebus.ui.vtype.FormatOption.DEFAULT, -1, true);
                      if (param instanceof VInt) {
                        paramStr += "\nType:Int";
                        paramStr += "\nGeneration Time:" + ((VInt) param).getTime();
                      } else if (param instanceof VDouble) {
                        paramStr += "\nType:VDouble";
                        paramStr += "\nGeneration Time:" + ((VDouble) param).getTime();
                      } else if (param instanceof VString) {
                        paramStr += "\nType:VString";
                        paramStr += "\nGeneration Time:" + ((VString) param).getTime();
                      } else if (param instanceof VBoolean) {
                        paramStr += "\nType:Boolean";
                        paramStr += "\nGeneration Time:" + ((VBoolean) param).getTime();
                      } else if (param instanceof VEnum) {
                        paramStr += "\nType:Enum";
                        paramStr += "\nGeneration Time:" + ((VEnum) param).getTime();
                      } else if (param instanceof VFloat) {
                        paramStr += "\nType:Float";
                        paramStr += "\nGeneration Time:" + ((VFloat) param).getTime();
                      } else if (param instanceof VLong) {
                        paramStr += "\nType:Long";
                        paramStr += "\nGeneration Time:" + ((VLong) param).getTime();
                      } else if (param instanceof VUInt) {
                        paramStr += "\nType:UInt";
                        paramStr += "\nGeneration Time:" + ((VUInt) param).getTime();
                      } else if (param instanceof VULong) {
                        paramStr += "\nType:UInt";
                        paramStr += "\nGeneration Time:" + ((VULong) param).getTime();
                      } else {
                        paramStr += "\nType:UNKNOWN" + "(" + param.toString() + ")";
                        paramStr += "\nGeneration Time:" + ((TimeProvider) param).getTime();
                      }
                      paramsView.getCurrentParam().set(paramStr);
                      paramsView.updateParamValue(paramStr, PvName);
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
