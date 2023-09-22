package com.windhoverlabs.yamcs.applications.events;

import com.windhoverlabs.yamcs.core.CMDR_Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.yamcs.protobuf.Event.EventSeverity;

public class NewEventDialog extends Dialog<CMDR_Event> {
  protected TextArea message = new TextArea();
  protected ComboBox<String> severity = new ComboBox<String>();
  protected TextField user = new TextField();
  protected String pathToCSS = "";
  private ObservableList<String> severityOptions = FXCollections.observableArrayList();

  //  Callback<ListView<String>, ListCell<String>> cellFactory = new Callback<ListView<String>,
  // ListCell<String>>();

  private ButtonType saveButtonType;

  protected GridPane layout = new GridPane();

  protected NewEventDialog() {}

  public NewEventDialog(Callback<CMDR_Event, Boolean> testConnectionCallback, String newCSSPath) {
    message.setId("message");
    severity.setId("severity");
    severityOptions.add(EventSeverity.INFO.toString());
    severityOptions.add(EventSeverity.WATCH.toString());
    severityOptions.add(EventSeverity.WARNING.toString());
    severityOptions.add(EventSeverity.DISTRESS.toString());
    severityOptions.add(EventSeverity.CRITICAL.toString());
    severityOptions.add(EventSeverity.SEVERE.toString());
    severity.setItems(severityOptions);

    saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
    this.setTitle("Create Event");
    pathToCSS = newCSSPath;

    layout.setColumnIndex(layout, null);
    layout.setHgap(5);
    layout.setVgap(5);
    setResizable(true);
    layout.setPrefWidth(600);

    addServerUrlField();
    addSeverityField();

    getDialogPane().setContent(layout);
    addButtons(testConnectionCallback);
  }

  protected void addButtons(Callback<CMDR_Event, Boolean> testConnectionCallback) {
    getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);

    //    saveButton.addEventFilter(
    //        ActionEvent.ACTION,
    //        event -> {
    //          if (!validateConnect()) {
    //            event.consume();
    //          }
    //        });
  }

  protected void addServerUrlField() {
    message.setPromptText("168.2.5.100");
    layout.add(new Label("Address:"), 0, 1);
    message.setTooltip(new Tooltip("Name of the server url to connect to."));
    GridPane.setHgrow(message, Priority.ALWAYS);
    layout.add(message, 1, 1);
  }

  protected void addSeverityField() {
    severity.setPromptText(EventSeverity.INFO.toString());
    layout.add(new Label("Severity:"), 0, 2);
    severity.setTooltip(new Tooltip("Port number to connect to."));
    GridPane.setHgrow(severity, Priority.ALWAYS);
    layout.add(severity, 1, 2);
  }
}
