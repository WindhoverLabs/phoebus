package com.windhoverlabs.display.builder.runtime;

import com.windhoverlabs.display.model.widgets.WaypointModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.csstudio.display.builder.runtime.PVNameToValueBinding;
import org.csstudio.display.builder.runtime.RuntimeAction;
import org.csstudio.display.builder.runtime.WidgetRuntime;

/**
 * Very simple Runtime for the WaypointModel
 *
 * @author Lorenzo Gomez
 */
@SuppressWarnings("nls")
public class WaypointWidgetRuntime extends WidgetRuntime<WaypointModel> {
  private final List<RuntimeAction> runtime_actions = new ArrayList<>(2);

  private final List<PVNameToValueBinding> bindings = new ArrayList<>();

  @Override
  public void initialize(final WaypointModel widget) {
    super.initialize(widget);
  }

  @Override
  public Collection<RuntimeAction> getRuntimeActions() {
    return runtime_actions;
  }

  @Override
  public void start() {
    super.start();

    bindings.add(
        new PVNameToValueBinding(this, widget.propWaypointALonPVName(), widget.propWaypointALon()));
    bindings.add(
        new PVNameToValueBinding(this, widget.propWaypointALatPVName(), widget.propWaypointALat()));
    bindings.add(
        new PVNameToValueBinding(
            this, widget.propWaypointCurrentLonPVName(), widget.propWaypointCurrentLon()));
    bindings.add(
        new PVNameToValueBinding(
            this, widget.propWaypointCurrentLatPVName(), widget.propWaypointCurrentLat()));
    bindings.add(
        new PVNameToValueBinding(this, widget.propWaypointBLonPVName(), widget.propWaypointBLon()));
    bindings.add(
        new PVNameToValueBinding(this, widget.propWaypointBLatPVName(), widget.propWaypointBLat()));
  }

  @Override
  public void stop() {
    // Disconnect Marker PVs and listeners
    for (PVNameToValueBinding binding : bindings) binding.dispose();
    super.stop();
  }
}
