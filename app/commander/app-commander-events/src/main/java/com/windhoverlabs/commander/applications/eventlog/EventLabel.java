package com.windhoverlabs.commander.applications.eventlog;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;

public class EventLabel extends Label implements ReadOnlyProperty {

  public EventLabel(String text) {
    super(text);
  }

  @Override
  public void addListener(InvalidationListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeListener(InvalidationListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addListener(ChangeListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeListener(ChangeListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getValue() {
    // TODO Auto-generated method stub
    return getText();
  }

  @Override
  public Object getBean() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }
}
