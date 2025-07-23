package trafficsim.ui.view;

import javafx.animation.AnimationTimer;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

import trafficsim.core.model.*;
import trafficsim.core.sim.SimulationEngine;
import trafficsim.ui.adapter.*;
import trafficsim.ui.controller.MainController;
import trafficsim.ui.view.intersection.*;

import java.util.*;
import java.util.function.Consumer;

public class SimulationRenderer
{
    private final Pane root;
    private final SimulationEngine engine;
    private final MainController controller;

    private final Map<Car, Rectangle> carViews = new HashMap<>();
    private final Map<Car, CarAdapter> carAdapters = new HashMap<>();
    private final Map<Intersection, Node> intersectionViews = new HashMap<>();
    private final Map<Road, Line> roadViews = new HashMap<>();

    public SimulationRenderer(Pane root, SimulationEngine engine, MainController controller)
    {
        this.root = root;
        this.engine = engine;
        this.controller = controller;

        new AnimationTimer() {
            @Override
            public void handle(long now)
            {
                refreshFrame();
            }
        }.start();
    }

    public void onIntersectionAdded(Intersection intersection)
    {
        Node view = buildView(intersection);
        intersectionViews.put(intersection, view);
        root.getChildren().add(view);
    }

    public void removeIntersection(Intersection intersection)
    {
        Node view = intersectionViews.remove(intersection);
        if (view != null)
        {
            root.getChildren().remove(view);
        }
    }

    public void removeRoad(Road road)
    {
        Node view = roadViews.remove(road);
        if (view != null)
        {
            root.getChildren().remove(view);
        }
    }

    public void onRoadAdded(Road road)
    {
        Line line = buildRoadView(road);
        roadViews.put(road, line);
        root.getChildren().add(1, line);
    }

    public void onCarAdded(Car car)
    {
        Rectangle view = buildCarView(car);
        carViews.put(car, view);
        carAdapters.put(car, new CarAdapter(car));
        root.getChildren().add(view);
    }

    private void refreshFrame()
    {
        for (Map.Entry<Car, CarAdapter> e : carAdapters.entrySet())
        {
            CarAdapter adapter = e.getValue();
            adapter.pullFromModel();

            Rectangle view = carViews.get(e.getKey());
            view.setX(adapter.xProperty().get());
            view.setY(adapter.yProperty().get());
        }
    }

    //--------------------------------------------------------------

    private Rectangle buildCarView(Car car)
    {
        Rectangle r = new Rectangle(40, 20);
        r.setFill(Color.CORNFLOWERBLUE);
        return r;
    }

    private Node buildView(Intersection intersection)
    {
        Consumer<Intersection> editCallback = controller::showEditIntersectionDialog;
        if (intersection instanceof SignalisedIntersection)
        {
            return new SignalisedIntersectionView(intersection, editCallback, controller);
        } else if (intersection instanceof Roundabout)
        {
            return new RoundaboutView(intersection, editCallback, controller);
        } else
        {
            throw new IllegalArgumentException("unknown intersection type");
        }
    }

    private Line buildRoadView(Road road)
    {
        double fromX = IntersectionUtil.toPx(road.from().position().x);
        double fromY = IntersectionUtil.toPx(road.from().position().y);
        double toX = IntersectionUtil.toPx(road.to().position().x);
        double toY = IntersectionUtil.toPx(road.to().position().y);

        Line line = new Line(fromX, fromY, toX, toY);
        line.setStrokeWidth(12);
        line.setStroke(Color.DIMGRAY);

        line.setOnMouseEntered(e ->
        {
            line.setStroke(Color.ORANGE);
            root.getScene().setCursor(Cursor.HAND);
        });
        line.setOnMouseExited(e ->
        {
            line.setStroke(Color.DIMGRAY);
            root.getScene().setCursor(Cursor.DEFAULT);
        });
        line.setOnMouseClicked(e -> controller.showEditRoadDialog(road));

        return line;
    }

    public Collection<Intersection> getIntersections()
    {
        return Collections.unmodifiableSet(intersectionViews.keySet());
    }
}
