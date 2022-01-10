package com.windhoverlabs.applications.commander.map;

import com.gluonhq.attach.position.Position;
import com.gluonhq.attach.position.PositionService;
import com.gluonhq.attach.util.Services;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.phoebus.framework.nls.NLS;
import org.phoebus.framework.persistence.Memento;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;

/** @author lgomez */
@SuppressWarnings("nls")
public class MissionMapInstance implements AppInstance {
  private static final int DEFAULT_ZOOM = 3;
  private static final double DEFAULT_LAT = 29.40647409433943;
  private static final double DEFAULT_LONG = -95.02559307928686;
  /** Logger for all file browser code */
  public static final Logger logger = Logger.getLogger(MissionMapInstance.class.getPackageName());

  /** Memento tags */
  private static final String DIRECTORY = "directory",
      SHOW_COLUMN = "show_col",
      WIDTH = "col_width";

  private MapPoint mapPoint;
  private final AppDescriptor app;
  private MissionMapController controller;
  private MapView map;

  public MissionMapInstance(AppDescriptor app) {
    this.app = app;
    final FXMLLoader fxmlLoader;
    Node content;

    try {
      final URL fxml = getClass().getResource("Main.fxml");
      final ResourceBundle bundle = NLS.getMessages(MissionMapInstance.class);
      fxmlLoader = new FXMLLoader(fxml, bundle);
      content = (Node) fxmlLoader.load();
      controller = fxmlLoader.getController();
    } catch (IOException ex) {
      logger.log(Level.WARNING, "Cannot load UI", ex);
      content = new Label("Cannot load UI");
    }

    final DockItem tab = new DockItem(this, content);
    DockPane.getActiveDockPane().addTab(tab);

    map = new MapView();
    map.addLayer(positionLayer());
    map.setCenter(DEFAULT_LAT, DEFAULT_LONG);
    map.setZoom(DEFAULT_ZOOM);

    //        TODO:Not sure if we should add the map to this SubScene.
    SubScene scene;
    scene = new SubScene(map, 1000, 700);
    GridPane rootPane = (GridPane) content;
    rootPane.add(scene, 0, 0);

    Button setPositionButton = new Button("Position");

    setPositionButton
        .onActionProperty()
        .set(
            e -> {
              System.out.println("Fire");
            });

    rootPane.add(setPositionButton, 1, 0);

    tab.addClosedNotification(controller::shutdown);
  }

  private MapLayer positionLayer() {
    return Services.get(PositionService.class)
        .map(
            positionService -> {
              positionService.start();

              ReadOnlyObjectProperty<Position> positionProperty =
                  positionService.positionProperty();
              Position position = positionProperty.get();
              if (position == null) {
                position = new Position(DEFAULT_LAT, DEFAULT_LONG);
              }
              mapPoint = new MapPoint(position.getLatitude(), position.getLongitude());

              PoiLayer answer = new PoiLayer();
              answer.addPoint(mapPoint, new Circle(7, Color.RED));

              positionProperty.addListener(
                  e -> {
                    Position pos = positionProperty.get();
                    mapPoint.update(pos.getLatitude(), pos.getLongitude());
                    System.out.println("current position-->" + pos);
                  });
              return answer;
            })
        .orElseGet(
            () -> {
              PoiLayer answer = new PoiLayer();
              mapPoint = new MapPoint(DEFAULT_LAT, DEFAULT_LONG);
              answer.addPoint(mapPoint, new Circle(7, Color.RED));
              return answer;
            });
  }

  @Override
  public AppDescriptor getAppDescriptor() {
    return app;
  }

  @Override
  public void restore(final Memento memento) {
    System.out.println("restore");
  }

  @Override
  public void save(final Memento memento) {
    System.out.println("save");
  }

  // TODO: Implement
  private void initMapControls() {}
}
