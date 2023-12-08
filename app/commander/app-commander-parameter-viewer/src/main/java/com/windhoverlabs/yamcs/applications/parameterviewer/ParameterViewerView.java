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
import javafx.css.PseudoClass;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.phoebus.framework.persistence.Memento;

/**
 * Panel for exporting data into files
 *
 * @author lgomez
 */
@SuppressWarnings("nls")
public class ParameterViewerView extends VBox {
  private final TextField start = new TextField();

  private final String utcRegex = "d{4}-d{2}-d{2}Td{2}:d{2}:d{2}.d{3}+d{2}:d{2}";

  //

  private SimpleStringProperty currentParam = new SimpleStringProperty("Param Value:");

  public SimpleStringProperty getCurrentParam() {
    return currentParam;
  }

  public void setCurrentParam(SimpleStringProperty currentParam) {
    this.currentParam = currentParam;
  }

  public String getStart() {
    return start.getText();
  }

  private final TextField end = new TextField();

  public String getEnd() {
    return end.getText();
  }

  void setEnd(String time) {
    end.setText(time);
  }

  void setStart(String time) {
    start.setText(time);
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
    // * Samples To Export *
    // Start:  ___start_______________________________________________________________ [select]
    // End  :  ___end_________________________________________________________________ [x] Use
    // start/end time of Plot
    // Source: ( ) Plot  (*) Raw Archived Data  ( ) Averaged Archived Data  __time__   ( ) Linear
    // __linear__

    configureValidators();
    GridPane grid = new GridPane();
    //    grid.setHgap(5);
    //    grid.setVgap(5);
    //    grid.setPadding(new Insets(5));

    var l = new Copyable();
    //    TODO:Would be nice to make copyable somehow
    //    l.setEditable(false);
    l.setCursor(Cursor.TEXT);

    l.textProperty().bind(currentParam);

    grid.add(l, 0, 0);
    //    start.va
    //    \\d{4}-[0-1]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d\\+\\d{4}
    //    GridPane.setHgrow(start, Priority.ALWAYS);
    final TitledPane source = new TitledPane(Messages.ParameterTabTitle, grid);
    source.setCollapsible(false);

    getChildren().setAll(source);
  }

  void configureValidators() {
    start
        .textProperty()
        .addListener(
            event -> {
              System.out.println("Changed:" + !start.getText().matches(utcRegex));
              ;
              start.pseudoClassStateChanged(
                  PseudoClass.getPseudoClass("error"),
                  !start.getText().isEmpty() && !start.getText().matches(utcRegex));
            });
  }

  /** @param memento Where to save current state */
  public void save(final Memento memento) {}

  /** @param memento From where to restore saved state */
  public void restore(final Memento memento) {}
}
