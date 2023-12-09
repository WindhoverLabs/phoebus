/*******************************************************************************
 * Copyright (c) 2010-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.windhoverlabs.yamcs.applications.parameterviewer;

import java.util.ArrayList;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.phoebus.framework.persistence.Memento;

/**
 * Panel for exporting data into files
 *
 * @author lgomez
 */
@SuppressWarnings("nls")
public class ParameterViewerView extends VBox {
  private final ListView<Copyable> ParamsTable = new ListView<Copyable>();

  private SimpleStringProperty currentParam = new SimpleStringProperty("Param Value:");

  private ObservableList<Copyable> params = FXCollections.observableArrayList();

  public SimpleStringProperty getCurrentParam() {
    return currentParam;
  }

  public void setCurrentParam(SimpleStringProperty currentParam) {
    this.currentParam = currentParam;
  }

  private final TextField end = new TextField();

  public String getEnd() {
    return end.getText();
  }

  void setEnd(String time) {
    end.setText(time);
  }

  public static final Logger log = Logger.getLogger(ParameterViewerView.class.getPackageName());

  private ArrayList<String> parameters = new ArrayList<String>();

  public ArrayList<String> getParameters() {
    return parameters;
  }

  public void setParameters(ArrayList<String> parameters) {
    this.parameters = parameters;
  }

  /** @param model Model from which to export */
  public ParameterViewerView() {

    ParamsTable.setItems(params);
    GridPane grid = new GridPane();
    //    grid.setHgap(5);
    //    grid.setVgap(5);
    //    grid.setPadding(new Insets(5));

    var l = new Copyable();

    var lContainer = new Pane(l);

    //   TODO:Add Border Between Labels

    l.textProperty().bind(currentParam);
    l.setStyle(" -fx-border-color:black; -fx-border-width: 0.3;");

    grid.add(lContainer, 0, 0);
    grid.add(new Pane(new Copyable("PlaceHolder")), 0, 1);
    //    l.getBorder().getInsets();
    final TitledPane parametersTabView = new TitledPane(Messages.ParameterTabTitle, ParamsTable);
    parametersTabView.setCollapsible(false);

    getChildren().setAll(parametersTabView);
  }

  /** @param memento Where to save current state */
  public void save(final Memento memento) {}

  /** @param memento From where to restore saved state */
  public void restore(final Memento memento) {}
}
