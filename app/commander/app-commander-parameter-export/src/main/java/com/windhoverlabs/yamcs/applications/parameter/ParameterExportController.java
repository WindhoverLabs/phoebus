package com.windhoverlabs.yamcs.applications.parameter;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_Event;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import java.util.ArrayList;
import java.util.HashSet;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.csstudio.trends.databrowser3.Activator;
import org.csstudio.trends.databrowser3.Messages;
import org.phoebus.framework.autocomplete.PVProposalService;
import org.phoebus.framework.autocomplete.Proposal;
import org.phoebus.framework.autocomplete.ProposalService;

public class ParameterExportController {
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
  }

  public static final Logger log =
      Logger.getLogger(ParameterExportController.class.getPackageName());

  private final TableView<ExportPV> tableView = new TableView<ExportPV>();

  TableColumn<ExportPV, Boolean> exportColumn = new TableColumn<ExportPV, Boolean>("export");
  TableColumn<ExportPV, String> pvColumn = new TableColumn<ExportPV, String>("pv");

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

  private ObservableList<ExportPV> proposalList = FXCollections.observableArrayList();
  private HashSet<String> exportSet = new HashSet<String>();

  private ExportView paramExportView = new ExportView();

  private Tab exportTab;
  @FXML private TabPane exportTabPane;
  @FXML private SplitPane mainSplit;

  public Node getRootPane() {
    return mainSplit;
  }

  @FXML
  public void initialize() {
    tableView.setId("paramExportTable");

    pvColumn.setCellValueFactory(new PropertyValueFactory<>("pv"));
    pvColumn.setCellValueFactory(cellData -> cellData.getValue().pvProperty());
    exportColumn.setCellValueFactory(cellData -> cellData.getValue().exportProperty());
    exportColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

    this.proposalList =
        FXCollections.observableArrayList(
            new Callback<ExportPV, Observable[]>() {

              @Override
              public Observable[] call(ExportPV param) {
                //                System.out.println("boolean:" + param.exportProperty());
                return new Observable[] {param.exportProperty()};
              }
            });

    this.proposalList.addListener(
        new ListChangeListener<ExportPV>() {

          @Override
          public void onChanged(ListChangeListener.Change<? extends ExportPV> c) {
            while (c.next()) {
              if (c.wasUpdated()) {
                ExportPV item = proposalList.get(c.getFrom());
                if (item.exportProperty().get()) {
                  exportSet.add(item.pvProperty().get());
                } else {
                  exportSet.remove(item.pvProperty().get());
                }
              }
            }
          }
        });
    tableView.getColumns().addAll(exportColumn, pvColumn);

    yamcsListener =
        new YamcsAware() {
          public void changeDefaultInstance() {
            //            YamcsObjectManager.getDefaultInstance()
            //                .getEvents()
            //                .addListener(
            //                    new ListChangeListener<Object>() {
            //                      @Override
            //                      public void onChanged(Change<?> c) {
            //                        Platform.runLater(
            //                            () -> {
            //                              if (!scrollLockButton.isSelected()) {
            //                                tableView.scrollTo(tableView.getItems().size() - 1);
            //                              }
            //                            });
            //                      }
            //                    });
            //            tableView.setItems(YamcsObjectManager.getDefaultInstance().getEvents());
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
                          Platform.runLater(
                              () -> {
                                //                                if
                                // (!scrollLockButton.isSelected()) {
                                //
                                // tableView.scrollTo(tableView.getItems().size() - 1);
                                //                                }
                              });
                        }
                      });
              //
              // tableView.setItems(YamcsObjectManager.getDefaultInstance().getEvents());
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
                          Platform.runLater(
                              () -> {
                                //                                if
                                // (!scrollLockButton.isSelected()) {
                                //
                                // tableView.scrollTo(tableView.getItems().size() - 1);
                                //                                }
                              });
                        }
                      });
              //
              // tableView.setItems(YamcsObjectManager.getDefaultInstance().getEvents());
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
          //          proposalService.lookup(
          //              pvTextField.getText(),
          //              (name, priority, proposals) -> {
          //                System.out.println("pvs:");
          //                for (Proposal p : proposals) {
          //                  System.out.println(p.getValue());
          //                }
          //              });

          //          proposalService.lookup(currentInstance, null);
        });
    //    pvTextField.setOnAction(
    //        (action) -> {
    //        });
    tableView.setItems(proposalList);
    tableView.setEditable(true);
    gridPane.add(tableView, 0, 1);
    createExportTab();
    mainSplit.setOrientation(Orientation.VERTICAL);
    mainSplit.setDividerPositions(0.8);
    //    mainSplit.
    //    mainSplit = new SplitPane(gridPane, exportTabPane);
    //    gridPane.add(paramExportView, 0, 2);
  }

  private void handleLookupResult(
      final javafx.scene.control.TextInputControl field,
      final String text,
      final String name,
      final int priority,
      final List<Proposal> proposals) {
    synchronized (proposalList) {
      proposalList.clear();
      exportSet.forEach(
          item -> {
            proposalList.add(new ExportPV(item, true));
          });
      for (Proposal p : proposals) {
        System.out.println(p.getValue());
        proposalList.add(new ExportPV(p.getValue(), false));
      }
    }
    //      final List<AutocompleteItem> items = new ArrayList<>();
    //
    //      synchronized (results)
    //      {
    //          // Merge proposals
    //          results.add(new Result(name, priority, proposals));
    //
    //          // Create menu items: Header for each result,
    //          // then list proposals
    //          for (Result result : results)
    //          {
    //              // Pressing 'Enter' on header simply forwards the enter to the text field
    //              items.add(new AutocompleteItem(result.header, () -> invokeAction(field)));
    //              for (Proposal proposal : result.proposals)
    //                  items.add(createItem(field, text, proposal));
    //          }
    //      }
    //
    //      // Update and show menu on UI thread
    //      if (menu_items.getAndSet(items) == null)
    //          Platform.runLater(() ->
    //          {
    //              final List<AutocompleteItem> current_items = menu_items.getAndSet(null);
    //              menu.setItems(current_items);
    //              if (! menu.isShowing())
    //                  showMenuForField(field);
    //          });
    // else: already pending, will use the updated 'menu_items'
  }

  public ParameterExportController() {}

  public void unInit() {
    YamcsObjectManager.removeListener(yamcsListener);
  }

  private void createExportTab() {
    exportTab = new Tab(Messages.Export, paramExportView);
    exportTab.setClosable(false);
    exportTab.setGraphic(Activator.getIcon("export"));
    exportTabPane.getTabs().add(exportTab);
  }
}
