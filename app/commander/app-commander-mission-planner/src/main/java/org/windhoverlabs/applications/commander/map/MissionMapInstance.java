package org.windhoverlabs.applications.commander.map;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.phoebus.framework.nls.NLS;
import org.phoebus.framework.persistence.Memento;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapView;
import com.gluonhq.attach.position.Position;
import com.gluonhq.attach.position.PositionService;
import com.gluonhq.attach.util.Services;

import com.gluonhq.maps.MapPoint;

@SuppressWarnings("nls")
public class MissionMapInstance implements AppInstance
{
    private static final int DEFAULT_ZOOM = 3;
    private static final double DEFAULT_LAT = 50.0;
    private static final double DEFAULT_LONG = 4.0;
    /** Logger for all file browser code */
    public static final Logger logger = Logger.getLogger(MissionMapInstance.class.getPackageName());

    /** Memento tags */
    private static final String DIRECTORY = "directory",
                                SHOW_COLUMN = "show_col",
                                WIDTH = "col_width";
    
    private MapPoint mapPoint;
    private final AppDescriptor app;
    private MissionMapController controller;

    public MissionMapInstance(AppDescriptor app)
    {
        this.app = app;
        final FXMLLoader fxmlLoader;
        Node content;
        
        try
        {
            final URL fxml = getClass().getResource("Main.fxml");
            final ResourceBundle bundle = NLS.getMessages(MissionMapInstance.class);
            fxmlLoader = new FXMLLoader(fxml, bundle);
            content = (Node) fxmlLoader.load();
            controller = fxmlLoader.getController();
        }
        catch (IOException ex)
        {
            logger.log(Level.WARNING, "Cannot load UI", ex);
            content = new Label("Cannot load UI");
        }

        final DockItem tab = new DockItem(this, content);
        DockPane.getActiveDockPane().addTab(tab);
        
        MapView map = new MapView();
        map.addLayer(positionLayer());
        map.setZoom(DEFAULT_ZOOM);
        
//        TODO:Not sure if we should add the map to this SubScene.
        SubScene scene;
        scene = new SubScene(map, 1000, 700);
        GridPane rootPane = (GridPane) content;
        rootPane.add(map, 0, 0);
                
        tab.addClosedNotification(controller::shutdown);
    }
    
    private MapLayer positionLayer() {
        return Services.get(PositionService.class)
                .map(positionService -> {
                    positionService.start();

                    ReadOnlyObjectProperty<Position> positionProperty = positionService.positionProperty();
                    Position position = positionProperty.get();
                    if (position == null) {
                        position = new Position(DEFAULT_LAT, DEFAULT_LONG);
                    }
                    mapPoint = new MapPoint(position.getLatitude(), position.getLongitude());

                    PoiLayer answer = new PoiLayer();
                    answer.addPoint(mapPoint, new Circle(7, Color.RED));

                    positionProperty.addListener(e -> {
                        Position pos = positionProperty.get();
                        mapPoint.update(pos.getLatitude(), pos.getLongitude());
                    });
                    return answer;
                })
                .orElseGet(() -> {
                    PoiLayer answer = new PoiLayer();
                    mapPoint = new MapPoint(DEFAULT_LAT, DEFAULT_LONG);
                    answer.addPoint(mapPoint, new Circle(7, Color.RED));
                    return answer;
                });
    }

    @Override
    public AppDescriptor getAppDescriptor()
    {
        return app;
    }

    @Override
    public void restore(final Memento memento)
    {
    	System.out.println("restore");
    }

    @Override
    public void save(final Memento memento)
    {
    	System.out.println("save");
    }
}
