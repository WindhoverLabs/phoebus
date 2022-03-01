package com.windhoverlabs.yamcs.applications.connections;

import java.util.Set;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;
import org.phoebus.ui.docking.DockStage;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

class ConnectionsManagerTestUI extends ApplicationTest {

  private DockPane tabs = null;
  /** Will be called with {@code @Before} semantics, i. e. before each test method. */
  @Start
  public void start(Stage stage) {

    // Add dock items to the original stage
    final DockItem tab1 = new DockItem("Tab 1");
    final BorderPane layout = new BorderPane();
    layout.setTop(new Label("Top"));
    layout.setCenter(new Label("Tab that indicates resize behavior"));
    layout.setBottom(new Label("Bottom"));
    tab1.setContent(layout);

    final DockItem tab2 = new DockItem("Tab 2");
    tab2.setContent(new Rectangle(500, 500, Color.RED));

    // The DockPane is added to a stage by 'configuring' it.
    // Initial tabs can be provided right away
    tabs = DockStage.configureStage(stage, tab1);

    stage.setX(0);
    stage.setY(0);
    stage.show();
  }

  @Test
  public void TestNumberOfDockItems() {
    Set<Node> menu = (Set<Node>) from(rootNode(Stage.getWindows().get(0))).queryAllAs(Node.class);

    Node rootPane = menu.iterator().next();

    Assertions.assertThat(rootPane instanceof BorderPane).isTrue();

    BorderPane pane = (BorderPane) rootPane;

    Assertions.assertThat(pane.centerProperty().get() instanceof DockPane).isTrue();

    DockPane dockPane = (DockPane) pane.centerProperty().get();

    Assertions.assertThat(dockPane.getDockItems().size() == 1).isTrue();

    Assertions.assertThat(dockPane.getTabs().get(0) instanceof DockItem).isTrue();
    //    Assertions.assertThat(dockPane.getTabs().get(1) instanceof DockItem).isTrue();

    Object[] renderedTabs = pane.lookup(".tab-header-area").lookupAll(".tab").toArray();
    Node renderedTab = (Node) renderedTabs[0];

    // Invoke the context menu on the tab
    Bounds tabBounds = renderedTab.getBoundsInLocal();

    this.moveTo(
        new Point2D(
            renderedTab.localToScene(tabBounds).getCenterX(),
            renderedTab.localToScene(tabBounds).getCenterY()));
    this.press(MouseButton.SECONDARY);
    //    Assertions.assertThat((Stage.getWindows().size() == 0)).isTrue();
  }
}
