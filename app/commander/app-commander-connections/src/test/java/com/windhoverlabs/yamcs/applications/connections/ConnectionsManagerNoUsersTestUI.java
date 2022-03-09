package com.windhoverlabs.yamcs.applications.connections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import com.windhoverlabs.yamcs.core.IntegrationTestNoUsers;
import com.windhoverlabs.yamcs.core.YamcsObject;
import com.windhoverlabs.yamcs.core.YamcsServer;
import com.windhoverlabs.yamcs.core.YamcsServerConnection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;
import org.phoebus.ui.docking.DockStage;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import org.yamcs.client.ClientException;

@TestMethodOrder(OrderAnnotation.class)
@Order(1)
class ConnectionsManagerNoUsersTestUI extends ApplicationTest {

  @BeforeAll
  public static void initYamcs() throws Exception {
    IntegrationTestNoUsers.setupYamcs();
  }

  @AfterAll
  public static void shutDownYamcs() throws Exception {
    org.yamcs.YamcsServer.getServer().shutDown();
  }

  private YamcsServer newServer;
  YamcsServerConnection newConnection;
  IntegrationTestNoUsers connectionsTest = null;
  private DockPane tabs = null;

  @BeforeEach
  public void before() throws ClientException {
    connectionsTest = new IntegrationTestNoUsers();
    connectionsTest.before();
    // TODO:Add Test for connections with the same name.
    newServer = new YamcsServer("sitl");
    newConnection = new YamcsServerConnection("sitl", "localhost", 9191);

    newServer.setConnection(newConnection);

    assertThat("Connection is established", newServer.connect(), equalTo(true));
  }

  private void closePane() {
    interact(
        () -> {
          try {
            DockStage.prepareToCloseItems((Stage) tabs.getScene().getWindow());
            DockStage.closeItems((Stage) tabs.getScene().getWindow());
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        });
  }

  @AfterEach
  public void after() throws InterruptedException {
    connectionsTest.after();
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
  @Order(1)
  public void testContextMenu() {
    Set<Node> window = (Set<Node>) from(rootNode(Stage.getWindows().get(0))).queryAllAs(Node.class);

    assertThat(
        "Connections Tree exists in scene",
        this.lookup("#ConnectionsTreeView").query(),
        notNullValue());

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

    assertThat("There is 9 menu items as part of Tree's ContextMenu", menuItems.size(), equalTo(9));

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
    assertThat(
        "MenuItem 3 has \"Connect All\" as label",
        menuItems.get(3).getText(),
        equalTo("Connect All"));

    assertThat(
        "MenuItem 4 has \"Disconnect All\" as label",
        menuItems.get(4).getText(),
        equalTo("Disconnect All"));

    assertThat(
        "MenuItem 5 has \"Connect\" as label", menuItems.get(5).getText(), equalTo("Connect"));

    assertThat(
        "MenuItem 6 has \"Disconnect\" as label",
        menuItems.get(6).getText(),
        equalTo("Disconnect"));

    assertThat(
        "MenuItem 7 has \"Set As Default\" as label",
        menuItems.get(7).getText(),
        equalTo("Set As Default"));

    assertThat("MenuItem 8 has \"Edit\" as label", menuItems.get(8).getText(), equalTo("Edit"));

    // Invoke the context menu on the Connections App
    Bounds tabBounds = firstTab.getContent().getBoundsInLocal();

    this.moveTo(
        new Point2D(
            renderedTab.localToScene(tabBounds).getCenterX(),
            renderedTab.localToScene(tabBounds).getCenterY()));
    this.press(MouseButton.SECONDARY);

    closePane();
  }

  /**
   * Code based on https://github.com/TestFX/TestFX/issues/540
   *
   * @param nodeQuery
   * @param timeout
   * @param timeUnit
   * @param fxRobot
   * @throws TimeoutException
   */
  public void waitForVisibleNode(String nodeQuery, long timeout, TimeUnit timeUnit, FxRobot fxRobot)
      throws TimeoutException {
    // First we wait for the node lookup to be non-null. Then, in the remaining time, wait for the
    // node to be visible.
    WaitForAsyncUtils.waitFor(
        timeout,
        TimeUnit.MILLISECONDS,
        new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            return fxRobot.lookup(nodeQuery).query().isVisible();
          }
        });
  }

  @Test
  @Order(2)
  public void testAddConnection() throws Exception {
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

    assertThat("There is 9 menu items as part of Tree's ContextMenu", menuItems.size(), equalTo(9));

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
    assertThat(
        "MenuItem 3 has \"Connect All\" as label",
        menuItems.get(3).getText(),
        equalTo("Connect All"));

    assertThat(
        "MenuItem 4 has \"Disconnect All\" as label",
        menuItems.get(4).getText(),
        equalTo("Disconnect All"));

    assertThat(
        "MenuItem 5 has \"Connect\" as label", menuItems.get(5).getText(), equalTo("Connect"));

    assertThat(
        "MenuItem 6 has \"Disconnect\" as label",
        menuItems.get(6).getText(),
        equalTo("Disconnect"));

    assertThat(
        "MenuItem 7 has \"Set As Default\" as label",
        menuItems.get(7).getText(),
        equalTo("Set As Default"));

    assertThat("MenuItem 8 has \"Edit\" as label", menuItems.get(8).getText(), equalTo("Edit"));

    // Invoke the context menu on the Connections App
    Bounds tabBounds = firstTab.getContent().getBoundsInLocal();

    this.clickOn(firstTab.getContent()).clickOn(MouseButton.SECONDARY);

    assertThat(
        "Connections Tree exists in scene",
        this.lookup("#ConnectionsTreeView").query(),
        notNullValue());

    assertThat(
        "Connections Context Menu exists in scene",
        this.lookup("#connectionsContextMenu").query(),
        notNullValue());

    // TODO:Test the item selected in context menu is what we expect it to be.

    this.type(KeyCode.ENTER);

    this.type(KeyCode.S, KeyCode.I, KeyCode.T, KeyCode.L);

    this.type(KeyCode.TAB);

    this.type(
        KeyCode.L, KeyCode.O, KeyCode.C, KeyCode.A, KeyCode.L, KeyCode.H, KeyCode.O, KeyCode.S,
        KeyCode.T);

    this.type(KeyCode.TAB);

    this.type(KeyCode.DIGIT9, KeyCode.DIGIT1, KeyCode.DIGIT9, KeyCode.DIGIT1);

    assertThat(
        "A node with id \"testConnectionButton\" exists.",
        this.lookup("#testConnectionButton").query(),
        notNullValue());
    assertThat(
        "A node with id \"testConnectionButton\" is of type Button.",
        this.lookup("#testConnectionButton").query() instanceof Button,
        equalTo(true));

    this.clickOn("#testConnectionButton").clickOn(MouseButton.PRIMARY);

    // TODO:Not sure if this is the best way for the testConnectionButton to be "done".
    WaitForAsyncUtils.waitForFxEvents();
    assertThat(
        "Dialog with id of \"testConnectionAlert\" exists on the scene",
        this.lookup("#testConnectionAlert").query().isVisible(),
        equalTo(true));
    closePane();
  }
}
