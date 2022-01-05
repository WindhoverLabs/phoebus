package com.windhoverlabs.applications.commander.map;

import javafx.fxml.FXML;

/**
 * Controller for the commander app
 *
 * @lgomez
 */
@SuppressWarnings("nls")
public class MissionMapController {

  public MissionMapController() {}

  @FXML
  public void initialize() {}

  /** Call when no longer needed */
  public void shutdown() {
    System.out.println("shutdown");
  }
}
