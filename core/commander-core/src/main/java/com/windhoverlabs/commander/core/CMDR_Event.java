package com.windhoverlabs.commander.core;

import java.time.Instant;
import javafx.beans.property.SimpleStringProperty;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;

public class CMDR_Event {
  private SimpleStringProperty message;
  private Instant generationTime;
  EventSeverity severity;

  public EventSeverity getSeverity() {
    return severity;
  }

  public SimpleStringProperty getMessage() {
    return message;
  }

  public CMDR_Event(String newMessage, Instant newGenerationTime, EventSeverity newSeverity) {
    message = new SimpleStringProperty(newMessage);
    generationTime = newGenerationTime;
    severity = newSeverity;
  }

  public Instant getGenerationTime() {
    return generationTime;
  }

  public String toString() {
    return message.getValue();
  }
}
