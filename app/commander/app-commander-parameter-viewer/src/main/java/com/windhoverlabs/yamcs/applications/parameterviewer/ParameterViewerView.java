/*******************************************************************************
 * Copyright (c) 2010-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.windhoverlabs.yamcs.applications.parameterviewer;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.phoebus.framework.persistence.Memento;

/** @author lgomez */
@SuppressWarnings("nls")
public class ParameterViewerView extends VBox {
  private final ListView<Copyable> ParamsTable = new ListView<Copyable>();

  private SimpleStringProperty currentParam = new SimpleStringProperty("Param Value:");

  private ObservableList<Copyable> params = FXCollections.observableArrayList();

  public static final Logger log = Logger.getLogger(ParameterViewerView.class.getPackageName());

  private ArrayList<String> parameters = new ArrayList<String>();

  public SimpleStringProperty getCurrentParam() {
    return currentParam;
  }

  public void setCurrentParam(SimpleStringProperty currentParam) {
    this.currentParam = currentParam;
  }

  public ArrayList<String> getParameters() {
    return parameters;
  }

  public void updateParams(Set<String> PVs) {
    //	  params.clear();
    //	  This impl could really use some improvements
    boolean exists = false;
    for (var pv : PVs) {
      for (var p : params) {
        if (p.getText().contains(pv)) {
          exists = true;
          break;
        }
      }

      if (!exists) {
        params.add(new Copyable(pv));
      }

      exists = false;
    }
  }

  public void removeParam(String pvName) {
    //	  TODO: I really need to use a HashMap, instead of using these horrid hacks.
    int indexOfParam = -1;
    for (var p : params) {
      if (p.getText().contains(pvName)) {
        indexOfParam = params.indexOf(p);
        break;
      }
    }
    if (indexOfParam != -1) {
      params.remove(indexOfParam);
    }
  }

  public void updateParamValue(String value, String PV) {
    for (var p : params) {
      if (p.getText().contains(PV)) {
        p.setText(value);
        //			  ParamsTable.refresh();
        break;
      }
    }
  }

  /** @param model Model from which to export */
  public ParameterViewerView() {

    ParamsTable.setItems(params);
    GridPane grid = new GridPane();

    var l = new Copyable();

    var lContainer = new Pane(l);

    //   TODO:Add Border Between Labels

    l.textProperty().bind(currentParam);
    l.setStyle(" -fx-border-color:black; -fx-border-width: 0.3;");

    grid.add(lContainer, 0, 0);
    grid.add(new Pane(new Copyable("PlaceHolder")), 0, 1);
    //    l.getBorder().getInsets();
    final TitledPane parametersTabView = new TitledPane(Messages.ParameterTabTitle, ParamsTable);
    parametersTabView.setCollapsible(true);

    getChildren().setAll(parametersTabView);
  }

  /** @param memento Where to save current state */
  public void save(final Memento memento) {}

  /** @param memento From where to restore saved state */
  public void restore(final Memento memento) {}
}
