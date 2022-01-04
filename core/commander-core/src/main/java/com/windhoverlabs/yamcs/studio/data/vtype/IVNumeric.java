package com.windhoverlabs.yamcs.studio.data.vtype;

import java.text.NumberFormat;

/** Partial implementation for numeric types. */
public class IVNumeric extends IVMetadata implements Display {

  private final Display display;

  IVNumeric(Alarm alarm, Time time, Display display) {
    super(alarm, time);
    this.display = display;
  }

  @Override
  public Double getLowerDisplayLimit() {
    return display.getLowerDisplayLimit();
  }

  @Override
  public Double getLowerCtrlLimit() {
    return display.getLowerCtrlLimit();
  }

  @Override
  public Double getLowerAlarmLimit() {
    return display.getLowerAlarmLimit();
  }

  @Override
  public Double getLowerWarningLimit() {
    return display.getLowerWarningLimit();
  }

  @Override
  public String getUnits() {
    return display.getUnits();
  }

  @Override
  public NumberFormat getFormat() {
    return display.getFormat();
  }

  @Override
  public Double getUpperWarningLimit() {
    return display.getUpperWarningLimit();
  }

  @Override
  public Double getUpperAlarmLimit() {
    return display.getUpperAlarmLimit();
  }

  @Override
  public Double getUpperCtrlLimit() {
    return display.getUpperCtrlLimit();
  }

  @Override
  public Double getUpperDisplayLimit() {
    return display.getUpperDisplayLimit();
  }
}
