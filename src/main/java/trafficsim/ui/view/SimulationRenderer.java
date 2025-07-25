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
import trafficsim.ui.controller.MainController.InteractionMode;
import trafficsim.ui.view.intersection.*;

import java.util.*;
import java.util.function.Consumer;

public class SimulationRenderer
{
    private final Pane intersectionPane;
    private final Pane roadPane;
    private final Pane carPane;
    private final Pane lightPane;

    private final SimulationEngine engine;
    private final MainController controller;

    private final Map<Car, Rectangle> carViews = new HashMap<>();
    private final Map<Car, CarAdapter> carAdapters = new HashMap<>();

    private final Map<Intersection, IntersectionView> intersectionViewMgrs = new HashMap<>();
    private final Map<Road, Line> roadViews = new HashMap<>();

    private static final double ROAD_ENDPOINT_OFFSET_PX = 25.0;

    public SimulationRenderer(Pane intersectionPane, Pane roadPane, Pane carPane, Pane lightPane,
            SimulationEngine engine, MainController controller)
    {
        this.intersectionPane = intersectionPane;
        this.roadPane = roadPane;
        this.carPane = carPane;
        this.lightPane = lightPane;

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
        IntersectionView viewMgr = buildViewManager(intersection);
        intersectionViewMgrs.put(intersection, viewMgr);

        intersectionPane.getChildren().addAll(viewMgr.getBaseNodes());

        lightPane.getChildren().add(viewMgr.getHighlightNode());
    }

    public IntersectionView getIntersectionView(Intersection model)
    {
        return intersectionViewMgrs.get(model);
    }

    public void removeIntersection(Intersection intersection)
    {
        IntersectionView viewMgr = intersectionViewMgrs.remove(intersection);
        if (viewMgr != null)
        {
            intersectionPane.getChildren().removeAll(viewMgr.getBaseNodes());

            lightPane.getChildren().remove(viewMgr.getHighlightNode());

            if (viewMgr instanceof SignalisedIntersectionView)
            {
                ((SignalisedIntersectionView) viewMgr).removeAllSignalViews(lightPane);
            }
        }
    }

    public void removeRoad(Road road)
    {
        Line view = roadViews.remove(road);
        if (view != null)
        {
            roadPane.getChildren().remove(view);
        }
    }

    public void onRoadAdded(Road road)
    {
        Line line = buildRoadView(road);
        roadViews.put(road, line);
        roadPane.getChildren().add(line);

        Intersection destination = road.to();
        if (destination instanceof SignalisedIntersection)
        {
            IntersectionView viewMgr = intersectionViewMgrs.get(destination);
            if (viewMgr instanceof SignalisedIntersectionView)
            {
                Node signalNode = ((SignalisedIntersectionView) viewMgr).createSignalForRoad(road,
                        controller::showEditIntersectionDialog, controller);
                lightPane.getChildren().add(signalNode);
            }
        }
    }

    public void onCarAdded(Car car)
    {
        Rectangle view = buildCarView(car);
        carViews.put(car, view);
        carAdapters.put(car, new CarAdapter(car));
        carPane.getChildren().add(view);
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

        for (IntersectionView viewMgr : intersectionViewMgrs.values())
        {
            viewMgr.updateView();
        }
    }

    private Rectangle buildCarView(Car car)
    {
        Rectangle r = new Rectangle(40, 20);
        r.setFill(Color.CORNFLOWERBLUE);
        return r;
    }

    private IntersectionView buildViewManager(Intersection intersection)
    {
        Consumer<Intersection> editCallback = controller::showEditIntersectionDialog;
        if (intersection instanceof SignalisedIntersection)
        {
            return new SignalisedIntersectionView(intersection, editCallback, controller);
        } else if (intersection instanceof Roundabout)
        {
            // TODO: Roundabout needs to beupdated to layered pane rnderer
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

        double dx = toX - fromX;
        double dy = toY - fromY;
        double length = Math.hypot(dx, dy);

        Line line = new Line();

        if (length > 1e-6)
        {
            double ux = dx / length;
            double uy = dy / length;

            double startX = fromX + ROAD_ENDPOINT_OFFSET_PX * ux;
            double startY = fromY + ROAD_ENDPOINT_OFFSET_PX * uy;
            double endX = toX - ROAD_ENDPOINT_OFFSET_PX * ux;
            double endY = toY - ROAD_ENDPOINT_OFFSET_PX * uy;

            line.setStartX(startX);
            line.setStartY(startY);
            line.setEndX(endX);
            line.setEndY(endY);
        }

        line.setStrokeWidth(12);
        line.setStroke(Color.DIMGRAY);

        line.setOnMouseEntered(e ->
        {
            if (controller.getCurrentMode() == InteractionMode.NORMAL)
            {
                line.setStroke(Color.ORANGE);
                roadPane.getScene().setCursor(Cursor.HAND);
            }
        });
        line.setOnMouseExited(e ->
        {
            line.setStroke(Color.DIMGRAY);
            if (controller.getCurrentMode() == InteractionMode.NORMAL)
            {
                roadPane.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        line.setOnMouseClicked(e -> controller.showEditRoadDialog(road));

        return line;
    }

    public Collection<Intersection> getIntersections()
    {
        return Collections.unmodifiableSet(intersectionViewMgrs.keySet());
    }
}
