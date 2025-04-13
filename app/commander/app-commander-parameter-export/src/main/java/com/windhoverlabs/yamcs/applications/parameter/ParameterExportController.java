package com.windhoverlabs.yamcs.applications.parameter;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_Event;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
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

    public void toggleExport() {
      if (export.get()) {
        export.set(false);
      } else {
        export.set(true);
      }
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
  private LinkedHashSet<String> exportSet = new LinkedHashSet<String>();

  private ExportView paramExportView = new ExportView();

  public ExportView getParamExportView() {
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

    pvColumn.setCellValueFactory(new PropertyValueFactory<>("pv"));
    pvColumn.setCellValueFactory(cellData -> cellData.getValue().pvProperty());

    exportColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));
    pvColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.7));
    exportColumn.minWidthProperty().bind(tableView.widthProperty().multiply(0.3));
    pvColumn.minWidthProperty().bind(tableView.widthProperty().multiply(0.7));
    exportColumn.setCellValueFactory(cellData -> cellData.getValue().exportProperty());
    exportColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

    this.proposalList =
        FXCollections.observableArrayList(
            new Callback<ExportPV, Observable[]>() {

              @Override
              public Observable[] call(ExportPV param) {
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
                paramExportView.getParameters().clear();
                exportSet.forEach(
                    p -> {
                      paramExportView.getParameters().add(p);
                    });
              }
            }
          }
        });
    //    tableView.setColumnResizePolicy(null);
    tableView.getColumns().addAll(pvColumn, exportColumn);

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
    //    tableView.getSelectionModel().selected
    tableView.setOnKeyPressed(
        e -> {
          if (e.getCode() == KeyCode.ENTER) {
            var selection = tableView.getSelectionModel().getSelectedItem();
            if (selection != null) {
              selection.toggleExport();
            }
          }
        });
    tableView.setEditable(true);
    gridPane.add(tableView, 0, 1);
    createExportTab();
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
      exportSet.forEach(
          item -> {
            proposalList.add(new ExportPV(item, true));
          });
      for (Proposal p : proposals) {
        proposalList.add(new ExportPV(p.getValue(), false));
      }
    }
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
