package com.windhoverlabs.yamcs.applications.commandoptions;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_Event;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import org.yamcs.protobuf.Event.EventSeverity;

public class CommandOptionsController {
  public static final Logger log =
      Logger.getLogger(CommandOptionsController.class.getPackageName());

  private final TableView<CMDR_Event> tableView = new TableView<CMDR_Event>();

  TableColumn<CMDR_Event, String> severityCol = new TableColumn<CMDR_Event, String>("Severity");
  TableColumn<CMDR_Event, String> annotationCol = new TableColumn<CMDR_Event, String>();
  TableColumn<CMDR_Event, String> generationTimeCol =
      new TableColumn<CMDR_Event, String>("Generation Time");
  TableColumn<CMDR_Event, String> receptionTimeCol =
      new TableColumn<CMDR_Event, String>("Reception Time");
  TableColumn<CMDR_Event, String> messageCol = new TableColumn<CMDR_Event, String>("Message");
  TableColumn<CMDR_Event, String> typeCol = new TableColumn<CMDR_Event, String>("Type");
  TableColumn<CMDR_Event, String> sourceCol = new TableColumn<CMDR_Event, String>("Source");
  TableColumn<CMDR_Event, String> instanceCol = new TableColumn<CMDR_Event, String>("Instance");

  private ObservableList<CMDR_Event> data =
      FXCollections.observableArrayList(new ArrayList<CMDR_Event>());
  private static final int dataSize = 10_023;

  // TODO:Eventually these will be in spinner nodes. These are the event filters.
  private String currentServer = "sitl";
  private String currentInstance = "yamcs-cfs";

  YamcsAware yamcsListener = null;

  @FXML private GridPane gridPane;

  @FXML private ToggleButton scrollLockButton;

  public Node getRootPane() {
    return gridPane;
  }

  @FXML
  public void initialize() {
    tableView.setId("eventsTable");
    tableView.getStylesheets().add(CommandOptionsApp.getCSSPath());
    messageCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getMessage());
        });

    generationTimeCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getGenerationTime().toString());
        });
    receptionTimeCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getReceptionTime().toString());
        });
    severityCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getSeverity().toString());
        });
    typeCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getType().toString());
        });
    sourceCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getSource().toString());
        });
    instanceCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(YamcsObjectManager.getDefaultInstance().getName());
        });
    tableView
        .getColumns()
        .addAll(
            messageCol,
            generationTimeCol,
            receptionTimeCol,
            severityCol,
            typeCol,
            sourceCol,
            instanceCol);

    gridPane.add(tableView, 0, 1);
  }

  public CommandOptionsController() {
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
