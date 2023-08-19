package com.windhoverlabs.yamcs.applications.parameter;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_Event;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
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
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import org.phoebus.framework.autocomplete.PVProposalService;
import org.phoebus.framework.autocomplete.Proposal;
import org.phoebus.framework.autocomplete.ProposalService;
// import org.phoebus.ui.autocomplete.AutocompleteItem;
// import org.phoebus.ui.autocomplete.TextInputControl;
// import org.phoebus.ui.autocomplete.AutocompleteMenu.Result;
import org.yamcs.protobuf.Event.EventSeverity;

public class ParameterExportController {
  class ExportPV {
    private final SimpleBooleanProperty export = new SimpleBooleanProperty();
    private final SimpleStringProperty pv = new SimpleStringProperty();

    ExportPV(String pv, boolean export) {
      this.pv.set(pv);
      this.export.set(export);
      ;
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

  @FXML private Button exportButton;

  @FXML private TextField pvTextField;

  private ObservableList<ExportPV> exportList = FXCollections.observableArrayList();

  public Node getRootPane() {
    return gridPane;
  }

  @FXML
  public void initialize() {
    tableView.setId("paramExportTable");
    //    exportToggle.setCellValueFactory(
    //        (event) -> {
    //          return new SimpleStringProperty(event.getValue().getMessage());
    //        });

    pvColumn.setCellValueFactory(new PropertyValueFactory<>("pv"));
    pvColumn.setCellValueFactory(cellData -> cellData.getValue().pvProperty());
    //    exportColumn.setCellValueFactory(new PropertyValueFactory<>("export"));
    exportColumn.setCellValueFactory(cellData -> cellData.getValue().exportProperty());
    exportColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

    //    exportColumn.setCellFactory(
    //        column -> {
    //          return new TableCell<ExportPV, String>() {
    //            @Override
    //            protected void updateItem(String item, boolean empty) {
    //              super.updateItem(item, empty); // This is mandatory
    //              setTextFill(Color.BLACK);
    //              if (item == null || empty) { // If the cell is empty
    //                setText(null);
    //                setStyle("");
    //              } else { // If the cell is not empty
    //                // We get here all the info of the event of this row
    //                //                CMDR_Event event =
    // getTableView().getItems().get(getIndex());
    //                CMDR_Event event = null;
    //                switch (event.getSeverity()) {
    //                  case CRITICAL:
    //                    this.getStyleClass().add("critical");
    //                    break;
    //                  case DISTRESS:
    //                    this.getStyleClass().add("distress");
    //                    break;
    //                  case ERROR:
    //                    this.getStyleClass().add("error");
    //                    break;
    //                  case INFO:
    //                    this.getStyleClass().add("info");
    //                    break;
    //                  case SEVERE:
    //                    this.getStyleClass().add("severe");
    //                    break;
    //                  case WARNING:
    //                    this.getStyleClass().add("warning");
    //                    break;
    //                  case WATCH:
    //                    this.getStyleClass().add("watch");
    //                    break;
    //                  default:
    //                    setTextFill(Color.BLACK);
    //                    break;
    //                }
    //                setText(item); // Put the String data in the cell
    //              }
    //            }
    //          };
    //        });
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

    System.out.println("items part of root-->" + YamcsObjectManager.getRoot().getItems());
    //    for (YamcsServer s : YamcsObjectManager.getRoot().getItems()) {
    //      s.addListener(yamcsListener);
    //    }

    //    pvTextField.onKe
    pvTextField.setOnKeyTyped(
        e -> {
          String text = pvTextField.getText();
          System.out.println("type event:" + pvTextField.getText());

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
    tableView.setItems(exportList);
    tableView.setEditable(true);
    gridPane.add(tableView, 0, 1);
  }

  private void handleLookupResult(
      final javafx.scene.control.TextInputControl field,
      final String text,
      final String name,
      final int priority,
      final List<Proposal> proposals) {
    System.out.println("handleLookupResult");
    synchronized (exportList) {
      exportList.clear();
      for (Proposal p : proposals) {
        System.out.println(p.getValue());
        exportList.add(new ExportPV(p.getValue(), true));
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

  public ParameterExportController() {
    System.out.println("EventViewerController constructor$$$$$$$$$$$$$$");
  }

  private ObservableList<CMDR_Event> generateEvents(int numberOfEvents) {
    ObservableList<CMDR_Event> events = FXCollections.observableArrayList();
    for (int i = 0; i < numberOfEvents; i++) {
      DecimalFormat formatter = new DecimalFormat("#,###.00");
      events.add(
          new CMDR_Event(
              "Fake Events" + formatter.format(i + 1),
              Instant.now(),
              EventSeverity.INFO,
              "FAKE",
              Instant.now(),
              "FAKE_SOURCE",
              "FAKE_INSTANCE"));
    }
    return events;
  }

  public void unInit() {
    YamcsObjectManager.removeListener(yamcsListener);
  }
}
