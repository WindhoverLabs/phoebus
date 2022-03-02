package com.windhoverlabs.yamcs.applications.connections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import com.windhoverlabs.yamcs.core.AbstractIntegrationTest;
import com.windhoverlabs.yamcs.core.YamcsObject;
import com.windhoverlabs.yamcs.core.YamcsServer;
import com.windhoverlabs.yamcs.core.YamcsServerConnection;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;
import org.phoebus.ui.docking.DockStage;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.yamcs.client.ClientException;

class ConnectionsManagerTestUI extends ApplicationTest {
  private YamcsServer newServer;
  private boolean instancesReady = false;
  YamcsServerConnection newConnection;
  static ConnectionsManagerIntegrationTest connectionsTest = null;
  private DockPane tabs = null;

  @BeforeEach
  public void before() throws ClientException {
    connectionsTest.before();
    newServer = new YamcsServer("sitl");
    newConnection = new YamcsServerConnection("sitl", "localhost", 9190, "admin", "rootpassword");

    newServer.setConnection(newConnection);

    assertThat("Connection is established", newServer.connect(), equalTo(true));
  }

  @BeforeAll
  public static void initYamcs() throws Exception {
    try {
      connectionsTest = new ConnectionsManagerIntegrationTest();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    AbstractIntegrationTest.setupYamcs();
  }
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

    assertThat(
        "At least 1 application has been loaded",
        ApplicationService.getApplications().size(),
        greaterThan(0));

    assertThat(
        "Connections app is loaded",
        ApplicationService.findApplication("Connections"),
        notNullValue());

    assertThat(
        "App with name \"Connections\" is of type com.windhoverlabs.yamcs.applications.connections.ConnectionsManagerApp",
        ApplicationService.findApplication("Connections")
            instanceof com.windhoverlabs.yamcs.applications.connections.ConnectionsManagerApp,
        equalTo(true));

    // The DockPane is added to a stage by 'configuring' it.
    // Initial tabs can be provided right away
    tabs = DockStage.configureStage(stage, tab1);

    ApplicationService.findApplication("Connections").create();

    stage.setX(0);
    stage.setY(0);
    stage.show();
  }

  @Test
  //  @Disabled("Disabled until bug #42 has been resolved")
  public void testAddConnection() {
    Set<Node> window = (Set<Node>) from(rootNode(Stage.getWindows().get(0))).queryAllAs(Node.class);

    Node rootPane = window.iterator().next();

    Assertions.assertThat(rootPane instanceof BorderPane).isTrue();

    BorderPane pane = (BorderPane) rootPane;

    Assertions.assertThat(pane.centerProperty().get() instanceof DockPane).isTrue();

    DockPane dockPane = (DockPane) pane.centerProperty().get();

    Assertions.assertThat(dockPane.getDockItems().size() == 2).isTrue();

    Assertions.assertThat(dockPane.getTabs().get(0) instanceof DockItem).isTrue();
    //    Assertions.assertThat(dockPane.getTabs().get(1) instanceof DockItem).isTrue();

    Object[] renderedTabs = pane.lookup(".tab-header-area").lookupAll(".tab").toArray();
    Node renderedTab = (Node) renderedTabs[1];

    DockItem firstTab = (DockItem) dockPane.getDockItems().get(1);

    assertThat(
        "Connections Tab has the title of \"Connections\"",
        from((Node) firstTab.getGraphic()).queryAs(Labeled.class).getText(),
        equalTo("Connections"));

    assertThat(
        "Content of Connections tab is TreeView",
        firstTab.getContent() instanceof javafx.scene.control.TreeView,
        equalTo(true));

    ObservableList<MenuItem> menuItems =
        ((javafx.scene.control.TreeView<YamcsObject<?>>) firstTab.getContent())
            .getContextMenu()
            .getItems();

    assertThat(
        "MenuItem 0 has \"Add Connection\" as label",
        menuItems.get(0).getText(),
        equalTo("Add Connection"));
    assertThat(
        "MenuItem 1 has \"Remove Connection\" as label",
        menuItems.get(1).getText(),
        equalTo("Remove Connection"));
    assertThat(
        "MenuItem 2 is of type SeparatorMenuItem ",
        menuItems.get(2) instanceof SeparatorMenuItem,
        equalTo(true));

    // Invoke the context menu on the Connections App
    Bounds tabBounds = firstTab.getContent().getBoundsInLocal();

    this.moveTo(
        new Point2D(
            renderedTab.localToScene(tabBounds).getCenterX(),
            renderedTab.localToScene(tabBounds).getCenterY()));
    this.press(MouseButton.SECONDARY);
  }
}
