/*******************************************************************************
 * Copyright (c) 2015-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.windhoverlabs.display.representation;

import com.windhoverlabs.display.model.widgets.WaypointModel;
import java.util.concurrent.TimeUnit;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.representation.Preferences;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.display.builder.representation.javafx.widgets.RegionBaseRepresentation;
import org.csstudio.javafx.rtplot.RTTank;
import org.epics.vtype.VType;

/**
 * Creates JavaFX item for model widget
 *
 * @author Kay Kasemir
 */
public class WaypointRepresentation extends RegionBaseRepresentation<Pane, WaypointModel> {
  private final DirtyFlag dirty_look = new DirtyFlag();
  private final UntypedWidgetPropertyListener lookListener = this::lookChanged;
  private final UntypedWidgetPropertyListener valueListener = this::valueChanged;

  private volatile RTTank tank;

  @Override
  public Pane createJFXNode() throws Exception {
    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-----1");
    tank = new RTTank();
    tank.setUpdateThrottle(Preferences.image_update_delay, TimeUnit.MILLISECONDS);
    new Waypointpath();
    return new Pane(Waypointpath.getCShape(new Line(0, 0, 50, 50)));
  }

  @Override
  protected void registerListeners() {
    super.registerListeners();
    model_widget.propWidth().addUntypedPropertyListener(lookListener);
    model_widget.propHeight().addUntypedPropertyListener(lookListener);
    model_widget.propFillColor().addUntypedPropertyListener(lookListener);
    model_widget.propScaleVisible().addUntypedPropertyListener(lookListener);

    model_widget.propWaypointA().addUntypedPropertyListener(valueListener);
    model_widget.propWaypointB().addUntypedPropertyListener(valueListener);
    model_widget.propWaypointCurrent().addUntypedPropertyListener(valueListener);
    valueChanged(null, null, null);
  }

  @Override
  protected void unregisterListeners() {
    model_widget.propWidth().removePropertyListener(lookListener);
    model_widget.propHeight().removePropertyListener(lookListener);
    model_widget.propFillColor().removePropertyListener(lookListener);
    model_widget.propScaleVisible().removePropertyListener(lookListener);

    model_widget.propWaypointA().removePropertyListener(valueListener);
    model_widget.propWaypointB().removePropertyListener(valueListener);
    model_widget.propWaypointCurrent().removePropertyListener(valueListener);
    super.unregisterListeners();
  }

  private void lookChanged(
      final WidgetProperty<?> property, final Object old_value, final Object new_value) {
    dirty_look.mark();
    toolkit.scheduleUpdate(this);
  }

  private void valueChanged(
      final WidgetProperty<?> property, final Object old_value, final Object new_value) {

    double value = 0;

    final VType vtype = model_widget.propWaypointCurrent().getValue();

    double min_val =
        VTypeUtil.getValueNumber(model_widget.propWaypointA().getValue()).doubleValue();
    double max_val =
        VTypeUtil.getValueNumber(model_widget.propWaypointB().getValue()).doubleValue();
    tank.setRange(min_val, max_val);

    if (toolkit.isEditMode()) {
      value = (min_val + max_val) / 2;
    } else {
      value = VTypeUtil.getValueNumber(vtype).doubleValue();
    }

    tank.setValue(value);
  }

  @Override
  public void updateChanges() {
    super.updateChanges();
    if (dirty_look.checkAndClear()) {
      double width = model_widget.propWidth().getValue();
      double height = model_widget.propHeight().getValue();
      jfx_node.setPrefSize(width, height);
      tank.setWidth(width);
      tank.setHeight(height);
      tank.setFillColor(JFXUtil.convert(model_widget.propFillColor().getValue()));
      tank.setScaleVisible(model_widget.propScaleVisible().getValue());
    }
  }
}
