package com.windhoverlabs.display.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFillColor;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.VisibleWidget;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.AlarmStatus;
import org.epics.vtype.Time;
import org.epics.vtype.VString;
import org.epics.vtype.VType;

/**
 * Widget that displays a tank with variable fill level
 *
 * @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WaypointModel extends VisibleWidget {

  /**
   * Special value of runtimePropValue that indicates that there is no PV (empty PV name).
   *
   * <p>Widget representation can detect this as a special case and for example show a general "OK"
   * state.
   *
   * <p>When the widget has a PV but becomes disconnected, the value will be <code>null</code>
   */
  public static final VType RUNTIME_VALUE_NO_PV =
      VString.of(
          Messages.ValueNoPV,
          Alarm.of(AlarmSeverity.NONE, AlarmStatus.CLIENT, Messages.ValueNoPV),
          Time.of(Instant.ofEpochSecond(0), 0, false));

  private volatile WidgetProperty<String> waypoint_a_pv_name;
  private volatile WidgetProperty<String> waypoint_current_pv_name;
  private volatile WidgetProperty<String> waypoint_b_pv_name;
  private volatile WidgetProperty<VType> waypoint_a_pv_value;
  private volatile WidgetProperty<VType> waypoint_current_pv_value;
  private volatile WidgetProperty<VType> waypoint_b_pv_value;
  private volatile WidgetProperty<Boolean> alarm_border;

  private static final WidgetPropertyDescriptor<VType> WaypointA =
      CommonWidgetProperties.newRuntimeValue("waypointA", Messages.WidgetProperties_Value);
  public static final WidgetPropertyDescriptor<VType> WaypointCurrent =
      CommonWidgetProperties.newRuntimeValue("waypointCurrent", Messages.WidgetProperties_Value);
  public static final WidgetPropertyDescriptor<VType> WaypointB =
      CommonWidgetProperties.newRuntimeValue("waypointB", Messages.WidgetProperties_Value);

  public static final WidgetPropertyDescriptor<String> WaypointAName =
      CommonWidgetProperties.newPVNamePropertyDescriptor(
          WidgetPropertyCategory.WIDGET, "waypoint_a_pv_name", Messages.WidgetProperties_PVName);
  public static final WidgetPropertyDescriptor<String> WaypointBName =
      CommonWidgetProperties.newPVNamePropertyDescriptor(
          WidgetPropertyCategory.WIDGET, "waypoint_b_pv_name", Messages.WidgetProperties_PVName);
  public static final WidgetPropertyDescriptor<String> WaypointCurrentName =
      CommonWidgetProperties.newPVNamePropertyDescriptor(
          WidgetPropertyCategory.WIDGET,
          "waypoint_current_pv_name",
          Messages.WidgetProperties_PVName);

  /** Widget descriptor */
  public static final WidgetDescriptor WIDGET_DESCRIPTOR =
      new WidgetDescriptor(
          "waypoint",
          WidgetCategory.MONITOR,
          "Waypoint",
          "/icons/tank.png",
          "Waypoint to visualize the trajectory of a vehicle",
          Arrays.asList("com.windhoverlabs.display.model.widgets.WaypointModel")) {
        @Override
        public Widget createWidget() {
          return new WaypointModel();
        }
      };

  public static final WidgetPropertyDescriptor<Boolean> propScaleVisible =
      newBooleanPropertyDescriptor(
          WidgetPropertyCategory.DISPLAY, "scale_visible", Messages.WidgetProperties_ScaleVisible);

  private volatile WidgetProperty<WidgetColor> fill_color;
  private volatile WidgetProperty<Boolean> scale_visible;

  public WaypointModel() {
    super(WIDGET_DESCRIPTOR.getType(), 150, 200);
  }

  @Override
  protected void defineProperties(final List<WidgetProperty<?>> properties) {
    super.defineProperties(properties);
    properties.add(fill_color = propFillColor.createProperty(this, new WidgetColor(0, 0, 255)));
    properties.add(scale_visible = propScaleVisible.createProperty(this, true));
    properties.add(waypoint_a_pv_value = WaypointA.createProperty(this, null));
    properties.add(waypoint_b_pv_value = WaypointB.createProperty(this, null));
    properties.add(waypoint_current_pv_value = WaypointCurrent.createProperty(this, null));

    properties.add(waypoint_a_pv_name = WaypointAName.createProperty(this, "WaypointAName"));
    properties.add(
        waypoint_current_pv_name = WaypointCurrentName.createProperty(this, "WaypointCurrentName"));
    properties.add(waypoint_b_pv_name = WaypointBName.createProperty(this, "WaypointBName"));
    properties.add(alarm_border = propBorderAlarmSensitive.createProperty(this, true));
  }

  @Override
  public WidgetProperty<?> getProperty(String name)
      throws IllegalArgumentException, IndexOutOfBoundsException {
    return super.getProperty(name);
  }

  /** @return 'fill_color' property */
  public WidgetProperty<WidgetColor> propFillColor() {
    return fill_color;
  }

  /** @return 'scale_visible' property */
  public WidgetProperty<Boolean> propScaleVisible() {
    return scale_visible;
  }

  /** @return 'minimum' property */
  public WidgetProperty<VType> propWaypointA() {
    return waypoint_a_pv_value;
  }

  /** @return 'maximum' property */
  public WidgetProperty<VType> propWaypointB() {
    return waypoint_b_pv_value;
  }

  /** @return 'maximum' property */
  public WidgetProperty<VType> propWaypointCurrent() {
    return waypoint_current_pv_value;
  }

  /** @return 'minimum' property */
  public WidgetProperty<String> propWaypointAPVName() {
    return waypoint_a_pv_name;
  }

  /** @return 'maximum' property */
  public WidgetProperty<String> propWaypointBPVName() {
    return waypoint_b_pv_name;
  }

  /** @return 'maximum' property */
  public WidgetProperty<String> propWaypointCurrentPVName() {
    return waypoint_current_pv_name;
  }

  @Override
  protected String getInitialTooltip() {
    // PV-based widgets shows the PV and value
    return "$(pv_name)\n$(pv_value)";
  }

  /** @return 'border_alarm_sensitive' property */
  public WidgetProperty<Boolean> propBorderAlarmSensitive() {
    return alarm_border;
  }
}
