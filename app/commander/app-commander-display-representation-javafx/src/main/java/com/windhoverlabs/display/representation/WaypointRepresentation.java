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
import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.representation.Preferences;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.display.builder.representation.javafx.widgets.RegionBaseRepresentation;
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

  private volatile Waypointpath waypoint;
  private Pane waypointPane;

  @Override
  public Pane createJFXNode() throws Exception {
    waypoint = new Waypointpath();
    waypoint.setUpdateThrottle(Preferences.image_update_delay, TimeUnit.MILLISECONDS);
    waypointPane = new Pane(waypoint);
    return waypointPane;
  }

  @Override
  protected void registerListeners() {
    super.registerListeners();
    model_widget.propWidth().addUntypedPropertyListener(lookListener);
    model_widget.propHeight().addUntypedPropertyListener(lookListener);
    model_widget.propFillColor().addUntypedPropertyListener(lookListener);
    model_widget.propScaleVisible().addUntypedPropertyListener(lookListener);
    model_widget.propFont().addUntypedPropertyListener(lookListener);
    model_widget.propForeground().addUntypedPropertyListener(lookListener);
    model_widget.propBackground().addUntypedPropertyListener(lookListener);

    model_widget.propWaypointALon().addUntypedPropertyListener(valueListener);
    model_widget.propWaypointALat().addUntypedPropertyListener(valueListener);
    model_widget.propWaypointBLon().addUntypedPropertyListener(valueListener);
    model_widget.propWaypointBLat().addUntypedPropertyListener(valueListener);
    model_widget.propWaypointCurrentLon().addUntypedPropertyListener(valueListener);
    model_widget.propWaypointCurrentLat().addUntypedPropertyListener(valueListener);
  }

  @Override
  protected void unregisterListeners() {
    model_widget.propWidth().removePropertyListener(lookListener);
    model_widget.propHeight().removePropertyListener(lookListener);
    model_widget.propFillColor().removePropertyListener(lookListener);
    model_widget.propScaleVisible().removePropertyListener(lookListener);

    model_widget.propWaypointALon().removePropertyListener(valueListener);
    model_widget.propWaypointALat().removePropertyListener(valueListener);
    model_widget.propWaypointBLon().removePropertyListener(valueListener);
    model_widget.propWaypointBLat().removePropertyListener(valueListener);
    model_widget.propWaypointCurrentLon().removePropertyListener(valueListener);
    model_widget.propWaypointCurrentLat().removePropertyListener(valueListener);
    super.unregisterListeners();
  }

  private void lookChanged(
      final WidgetProperty<?> property, final Object old_value, final Object new_value) {
    dirty_look.mark();
    toolkit.scheduleUpdate(this);
  }

  private void valueChanged(
      final WidgetProperty<?> property, final Object old_value, final Object new_value) {

    final VType vtypeCurrentLon = model_widget.propWaypointCurrentLon().getValue();
    final VType vtypeCurrentLat = model_widget.propWaypointCurrentLat().getValue();
    final VType vtypePrevLon = model_widget.propWaypointALon().getValue();
    final VType vtypePrevtLat = model_widget.propWaypointALat().getValue();
    final VType vtypeNextLon = model_widget.propWaypointBLon().getValue();
    final VType vtypeNextLat = model_widget.propWaypointBLat().getValue();

    double currentLon = VTypeUtil.getValueNumber(vtypeCurrentLon).doubleValue();
    double currentLat = VTypeUtil.getValueNumber(vtypeCurrentLat).doubleValue();
    double prevLon = VTypeUtil.getValueNumber(vtypePrevLon).doubleValue();
    double prevLat = VTypeUtil.getValueNumber(vtypePrevtLat).doubleValue();
    double nextLon = VTypeUtil.getValueNumber(vtypeNextLon).doubleValue();
    double nextLat = VTypeUtil.getValueNumber(vtypeNextLat).doubleValue();

    if (Double.isNaN(currentLon)
        || Double.isNaN(currentLat)
        || Double.isNaN(prevLon)
        || Double.isNaN(prevLat)
        || Double.isNaN(nextLon)
        || Double.isNaN(nextLat)) {
      return;
    }

    waypoint.updateWaypoints(currentLon, currentLat, prevLon, prevLat, nextLon, nextLat);

    waypoint.requestUpdate();
  }

  @Override
  public void updateChanges() {
    super.updateChanges();
    if (dirty_look.checkAndClear()) {
      double width = model_widget.propWidth().getValue();
      double height = model_widget.propHeight().getValue();
      jfx_node.setPrefSize(width, height);
      waypoint.setWidth(width);
      waypoint.setHeight(height);
      waypoint.setFillColor(JFXUtil.convert(model_widget.propFillColor().getValue()));
      waypoint.setFont(JFXUtil.convert(model_widget.propFont().getValue()));
      waypoint.setBackground(JFXUtil.convert(model_widget.propBackground().getValue()));
      waypoint.setForeground(JFXUtil.convert(model_widget.propForeground().getValue()));
      waypointPane.setStyle("-fx-background-color: white");
    }
  }
}
