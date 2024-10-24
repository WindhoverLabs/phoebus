package com.windhoverlabs.yamcs.core;

import java.time.Instant;
import org.yamcs.protobuf.Event.EventSeverity;

public class CMDR_Event {
  private String message;
  private String source;

  private Instant generationTime;
  private Instant receptionTime;
  private String instanceName;

  EventSeverity severity;
  String type;

  public CMDR_Event(
      String newMessage,
      Instant newGenerationTime,
      EventSeverity newSeverity,
      String newType,
      Instant newReceptionTime,
      String newSource,
      String newInstanceName) {
    message = newMessage;
    generationTime = newGenerationTime;
    severity = newSeverity;
    type = newType;
    receptionTime = newReceptionTime;
    source = newSource;
    instanceName = newInstanceName;
  }

  public Instant getReceptionTime() {

    return receptionTime;
  }

  public EventSeverity getSeverity() {
    return severity;
  }

  public String getMessage() {
    return message;
  }

  public Instant getGenerationTime() {
    return generationTime;
  }

  public String toString() {
    return message;
  }

  public String getType() {
    return type;
  }

  public String getSource() {
    return source;
  }
}
