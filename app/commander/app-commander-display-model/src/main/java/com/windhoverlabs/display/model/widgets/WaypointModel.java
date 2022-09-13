package com.windhoverlabs.display.model.widgets;

import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newBooleanPropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBackgroundColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propBorderAlarmSensitive;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFillColor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propFont;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.propForegroundColor;

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
import org.csstudio.display.builder.model.persist.NamedWidgetColors;
import org.csstudio.display.builder.model.persist.NamedWidgetFonts;
import org.csstudio.display.builder.model.persist.WidgetColorService;
import org.csstudio.display.builder.model.persist.WidgetFontService;
import org.csstudio.display.builder.model.properties.CommonWidgetProperties;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.properties.WidgetFont;
import org.csstudio.display.builder.model.widgets.VisibleWidget;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.AlarmStatus;
import org.epics.vtype.Time;
import org.epics.vtype.VString;
import org.epics.vtype.VType;

/**
 * Widget that displays a waypoint. Useful for visualizing aircraft such as drones, fixedwings, etc
 *
 * @author Lorenzo Gomez
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

  private volatile WidgetProperty<String> waypoint_a_lon_pv_name;
  private volatile WidgetProperty<String> waypoint_a_lat_pv_name;
  private volatile WidgetProperty<String> waypoint_current_lon_pv_name;
  private volatile WidgetProperty<String> waypoint_current_lat_pv_name;
  private volatile WidgetProperty<String> waypoint_b_lon_pv_name;
  private volatile WidgetProperty<String> waypoint_b_lat_pv_name;
  private volatile WidgetProperty<VType> waypoint_a_lon_pv_value;
  private volatile WidgetProperty<VType> waypoint_a_lat_pv_value;
  private volatile WidgetProperty<VType> waypoint_current_lon_pv_value;
  private volatile WidgetProperty<VType> waypoint_current_lat_pv_value;
  private volatile WidgetProperty<VType> waypoint_b_lon_pv_value;
  private volatile WidgetProperty<VType> waypoint_b_lat_pv_value;
  private volatile WidgetProperty<Boolean> alarm_border;

  private volatile WidgetProperty<WidgetFont> font;
  private volatile WidgetProperty<WidgetColor> foreground;
  private volatile WidgetProperty<WidgetColor> background;

  private static final WidgetPropertyDescriptor<VType> WaypointALon =
      CommonWidgetProperties.newRuntimeValue("waypointALon", Messages.WidgetProperties_Value);
  private static final WidgetPropertyDescriptor<VType> WaypointALat =
      CommonWidgetProperties.newRuntimeValue("waypointALat", Messages.WidgetProperties_Value);
  public static final WidgetPropertyDescriptor<VType> WaypointCurrentLon =
      CommonWidgetProperties.newRuntimeValue("waypointCurrentLon", Messages.WidgetProperties_Value);
  public static final WidgetPropertyDescriptor<VType> WaypointCurrentLat =
      CommonWidgetProperties.newRuntimeValue("waypointCurrentLat", Messages.WidgetProperties_Value);
  public static final WidgetPropertyDescriptor<VType> WaypointBLon =
      CommonWidgetProperties.newRuntimeValue("waypointBLon", Messages.WidgetProperties_Value);
  public static final WidgetPropertyDescriptor<VType> WaypointBLat =
      CommonWidgetProperties.newRuntimeValue("waypointBLat", Messages.WidgetProperties_Value);

  public static final WidgetPropertyDescriptor<String> WaypointALonName =
      CommonWidgetProperties.newPVNamePropertyDescriptor(
          WidgetPropertyCategory.WIDGET,
          "waypoint_a_lon_pv_name",
          Messages.WidgetProperties_PVName);
  public static final WidgetPropertyDescriptor<String> WaypointALatName =
      CommonWidgetProperties.newPVNamePropertyDescriptor(
          WidgetPropertyCategory.WIDGET,
          "waypoint_a_lat_pv_name",
          Messages.WidgetProperties_PVName);

  public static final WidgetPropertyDescriptor<String> WaypointLonBName =
      CommonWidgetProperties.newPVNamePropertyDescriptor(
          WidgetPropertyCategory.WIDGET,
          "waypoint_b_lon_pv_name",
          Messages.WidgetProperties_PVName);
  public static final WidgetPropertyDescriptor<String> WaypointLatBName =
      CommonWidgetProperties.newPVNamePropertyDescriptor(
          WidgetPropertyCategory.WIDGET,
          "waypoint_b_lat_pv_name",
          Messages.WidgetProperties_PVName);
  public static final WidgetPropertyDescriptor<String> WaypointCurrentLonName =
      CommonWidgetProperties.newPVNamePropertyDescriptor(
          WidgetPropertyCategory.WIDGET,
          "waypoint_current_lon_pv_name",
          Messages.WidgetProperties_PVName);

  public static final WidgetPropertyDescriptor<String> WaypointCurrentLatName =
      CommonWidgetProperties.newPVNamePropertyDescriptor(
          WidgetPropertyCategory.WIDGET,
          "waypoint_current_lat_pv_name",
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
    properties.add(
        font = propFont.createProperty(this, WidgetFontService.get(NamedWidgetFonts.DEFAULT)));
    properties.add(
        foreground =
            propForegroundColor.createProperty(
                this, WidgetColorService.getColor(NamedWidgetColors.TEXT)));
    properties.add(
        background =
            propBackgroundColor.createProperty(
                this, WidgetColorService.getColor(NamedWidgetColors.READ_BACKGROUND)));
    properties.add(fill_color = propFillColor.createProperty(this, new WidgetColor(0, 0, 255)));
    properties.add(scale_visible = propScaleVisible.createProperty(this, true));
    properties.add(waypoint_a_lon_pv_value = WaypointALon.createProperty(this, null));
    properties.add(waypoint_a_lat_pv_value = WaypointALat.createProperty(this, null));
    properties.add(waypoint_b_lon_pv_value = WaypointBLon.createProperty(this, null));
    properties.add(waypoint_b_lat_pv_value = WaypointBLat.createProperty(this, null));
    properties.add(waypoint_current_lon_pv_value = WaypointCurrentLon.createProperty(this, null));
    properties.add(waypoint_current_lat_pv_value = WaypointCurrentLat.createProperty(this, null));
    properties.add(
        waypoint_a_lon_pv_name = WaypointALonName.createProperty(this, "WaypointALonName"));
    properties.add(
        waypoint_a_lat_pv_name = WaypointALatName.createProperty(this, "WaypointALatName"));
    properties.add(
        waypoint_current_lon_pv_name =
            WaypointCurrentLonName.createProperty(this, "WaypointCurrentLonName"));
    properties.add(
        waypoint_current_lat_pv_name =
            WaypointCurrentLatName.createProperty(this, "WaypointCurrentLatName"));
    properties.add(
        waypoint_b_lon_pv_name = WaypointLonBName.createProperty(this, "WaypointBLonName"));
    properties.add(
        waypoint_b_lat_pv_name = WaypointLatBName.createProperty(this, "WaypointBLatName"));
    properties.add(alarm_border = propBorderAlarmSensitive.createProperty(this, true));
  }

  @Override
  public WidgetProperty<?> getProperty(String name)
      throws IllegalArgumentException, IndexOutOfBoundsException {
    return super.getProperty(name);
  }

  /** @return 'scale_visible' property */
  public WidgetProperty<Boolean> propScaleVisible() {
    return scale_visible;
  }

  /** @return 'minimum' property */
  public WidgetProperty<VType> propWaypointALon() {
    return waypoint_a_lon_pv_value;
  }

  /** @return 'minimum' property */
  public WidgetProperty<VType> propWaypointALat() {
    return waypoint_a_lat_pv_value;
  }

  /** @return 'maximum' property */
  public WidgetProperty<VType> propWaypointBLon() {
    return waypoint_b_lon_pv_value;
  }
  /** @return 'maximum' property */
  public WidgetProperty<VType> propWaypointBLat() {
    return waypoint_b_lat_pv_value;
  }

  /** @return 'maximum' property */
  public WidgetProperty<VType> propWaypointCurrentLon() {
    return waypoint_current_lon_pv_value;
  }

  /** @return 'maximum' property */
  public WidgetProperty<VType> propWaypointCurrentLat() {
    return waypoint_current_lat_pv_value;
  }

  /** @return 'minimum' property */
  public WidgetProperty<String> propWaypointALonPVName() {
    return waypoint_a_lon_pv_name;
  }

  /** @return 'minimum' property */
  public WidgetProperty<String> propWaypointALatPVName() {
    return waypoint_a_lat_pv_name;
  }

  /** @return 'maximum' property */
  public WidgetProperty<String> propWaypointBLonPVName() {
    return waypoint_b_lon_pv_name;
  }

  /** @return 'maximum' property */
  public WidgetProperty<String> propWaypointBLatPVName() {
    return waypoint_b_lat_pv_name;
  }

  /** @return 'maximum' property */
  public WidgetProperty<String> propWaypointCurrentLonPVName() {
    return waypoint_current_lon_pv_name;
  }
  /** @return 'maximum' property */
  public WidgetProperty<String> propWaypointCurrentLatPVName() {
    return waypoint_current_lat_pv_name;
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

  /** @return 'font' property */
  public WidgetProperty<WidgetFont> propFont() {
    return font;
  }

  /** @return 'foreground_color' property */
  public WidgetProperty<WidgetColor> propForeground() {
    return foreground;
  }

  /** @return 'background_color' property */
  public WidgetProperty<WidgetColor> propBackground() {
    return background;
  }

  /** @return 'fill_color' property */
  public WidgetProperty<WidgetColor> propFillColor() {
    return fill_color;
  }
}
