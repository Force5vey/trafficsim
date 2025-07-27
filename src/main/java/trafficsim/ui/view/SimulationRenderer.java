// src/main/java/trafficsim/ui/view/SimulationRenderer.java

package trafficsim.ui.view;

import java.util.*;
import java.util.function.Consumer;
import javafx.animation.AnimationTimer;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import trafficsim.core.model.*;
import trafficsim.core.sim.SimulationEngine;
import trafficsim.ui.adapter.*;
import trafficsim.ui.controller.MainController;
import trafficsim.ui.view.intersection.*;
import trafficsim.ui.controller.helpers.InteractionModeManager.Mode;

public class SimulationRenderer
{
    private static final double ROAD_TILE_PX = 32.0;

    private final Pane intersectionPane;
    private final Pane roadPane;
    private final Pane carPane;
    private final Pane lightPane;

    private final SimulationEngine engine;
    private final MainController controller;

    private final Map<Car, ImageView> carViews = new HashMap<>();
    private final Map<Car, CarAdapter> carAdapters = new HashMap<>();

    private final Map<Intersection, IntersectionView> intersectionViewMgrs = new HashMap<>();
    private final Map<Road, Line> roadViews = new HashMap<>();
    private final Map<Road, List<ImageView>> roadTileViews = new HashMap<>();

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

        List<ImageView> tiles = roadTileViews.remove(road);
        if (tiles != null)
        {
            roadPane.getChildren().removeAll(tiles);
        }
    }

    private boolean isCanonical(Road road)
    {
        Vec2 from = road.from().position();
        Vec2 to = road.to().position();
        return (from.x < to.x) || (from.x == to.x && from.y < to.y);
    }

    public void onRoadAdded(Road road)
    {
        Line line = buildRoadView(road);
        roadViews.put(road, line);
        roadPane.getChildren().add(line);

        if (isCanonical(road))
        {
            List<ImageView> tiles = buildRoadTiles(road);
            roadTileViews.put(road, tiles);
            roadPane.getChildren().addAll(tiles);
        }

        Intersection destination = road.to();
        if (destination instanceof SignalisedIntersection)
        {
            IntersectionView viewMgr = intersectionViewMgrs.get(destination);
            if (viewMgr instanceof SignalisedIntersectionView)
            {
                Node signalNode = ((SignalisedIntersectionView) viewMgr).createSignalForRoad(road,
                        controller::selectForEditing, controller);
                lightPane.getChildren().add(signalNode);
            }
        }
    }

    public void onCarAdded(Car car)
    {
        ImageView view = buildCarView(car);
        carViews.put(car, view);
        carAdapters.put(car, new CarAdapter(car));
        carPane.getChildren().add(view);
    }

    public void removeCar(Car car)
    {
        ImageView view = carViews.remove(car);
        if (view != null)
        {
            carPane.getChildren().remove(view);
        }
        carAdapters.remove(car);
    }

    public void clearAll()
    {
        carViews.clear();
        carAdapters.clear();
        intersectionViewMgrs.clear();
        roadViews.clear();

        intersectionPane.getChildren().clear();
        roadPane.getChildren().clear();
        carPane.getChildren().clear();
        lightPane.getChildren().clear();
    }

    private void refreshFrame()
    {
        for (Map.Entry<Car, CarAdapter> e : carAdapters.entrySet())
        {
            Car car = e.getKey();
            CarAdapter adapter = e.getValue();
            adapter.pullFromModel();

            ImageView view = carViews.get(car);
            if (view == null)
            {
                continue;
            }

            double centerX = adapter.xProperty().get();
            double centerY = adapter.yProperty().get();

            double fitW = view.getFitWidth();
            double fitH = view.getFitHeight();

            view.setX(centerX - fitW / 2.0);
            view.setY(centerY - fitH / 2.0);

            double angleDeg = Math.toDegrees(car.headingRad());
            view.setRotate(angleDeg);
        }

        for (IntersectionView viewMgr : intersectionViewMgrs.values())
        {
            viewMgr.updateView();
        }
    }

    private ImageView buildCarView(Car car)
    {
        Image img = CarAssetManager.getNextCarImage();
        ImageView view = new ImageView(img);
        view.setFitWidth(40);
        view.setFitHeight(20);
        view.setPreserveRatio(true);
        view.setSmooth(true);

        view.setOnMouseEntered(e ->
        {
            if (controller.getCurrentMode() == Mode.NORMAL)
            {
                view.setStyle("-fx-effect: dropshadow(gaussian, orange, 8, 0.5, 0, 0);");
                view.getScene().setCursor(Cursor.HAND);
            }
        });

        view.setOnMouseExited(e ->
        {
            view.setStyle("");
            if (controller.getCurrentMode() == Mode.NORMAL)
            {
                view.getScene().setCursor(Cursor.DEFAULT);
            }
        });

        view.setOnMouseClicked(e ->
        {
            if (controller.getCurrentMode() == Mode.NORMAL)
            {
                controller.selectForEditing(car);
            }
        });

        return view;
    }

    private IntersectionView buildViewManager(Intersection intersection)
    {
        Consumer<Intersection> editCallback = controller::selectForEditing;
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

        double dx = toX - fromX;
        double dy = toY - fromY;
        double length = Math.hypot(dx, dy);

        Line line = new Line();

        if (length > 1e-6)
        {
            double ux = dx / length;
            double uy = dy / length;

            Vec2 offset = IntersectionUtil.getLaneOffsetVector(road);

            double startX = fromX + ROAD_ENDPOINT_OFFSET_PX * ux;
            double startY = fromY + ROAD_ENDPOINT_OFFSET_PX * uy;
            double endX = toX - ROAD_ENDPOINT_OFFSET_PX * ux;
            double endY = toY - ROAD_ENDPOINT_OFFSET_PX * uy;

            line.setStartX(startX + offset.x);
            line.setStartY(startY + offset.y);
            line.setEndX(endX + offset.x);
            line.setEndY(endY + offset.y);
        }

        line.setStrokeWidth(8);
        line.setStroke(Color.DIMGRAY);

        line.setOnMouseEntered(e ->
        {
            if (controller.getCurrentMode() == Mode.NORMAL)
            {
                line.setStroke(Color.ORANGE);
                roadPane.getScene().setCursor(Cursor.HAND);
            }
        });
        line.setOnMouseExited(e ->
        {
            line.setStroke(Color.DIMGRAY);
            if (controller.getCurrentMode() == Mode.NORMAL)
            {
                roadPane.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        line.setOnMouseClicked(e ->
        {
            if (controller.getCurrentMode() == Mode.NORMAL)
            {
                controller.selectForEditing(road);
            }
        });

        return line;
    }

    private List<ImageView> buildRoadTiles(Road road)
    {
        List<ImageView> tiles = new ArrayList<>();
        Image tileImg = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/trafficsim/assets/images/road_tile.png")));

        Vec2 from = road.from().position();
        Vec2 to = road.to().position();

        double fromX = IntersectionUtil.toPx(from.x);
        double fromY = IntersectionUtil.toPx(from.y);
        double toX = IntersectionUtil.toPx(to.x);
        double toY = IntersectionUtil.toPx(to.y);

        double dx = toX - fromX;
        double dy = toY - fromY;
        double length = Math.hypot(dx, dy);

        // Offset for intersection buffer
        double ux = dx / length;
        double uy = dy / length;
        double startX = fromX + ROAD_ENDPOINT_OFFSET_PX * ux;
        double startY = fromY + ROAD_ENDPOINT_OFFSET_PX * uy;
        double endX = toX - ROAD_ENDPOINT_OFFSET_PX * ux;
        double endY = toY - ROAD_ENDPOINT_OFFSET_PX * uy;
        double usableLength = Math.hypot(endX - startX, endY - startY);

        int nTiles = (int) Math.ceil(usableLength / ROAD_TILE_PX);

        double angleRad = Math.atan2(dy, dx);
        double angleDeg = Math.toDegrees(angleRad);

        for (int i = 0; i < nTiles; ++i)
        {
            double t = (i + 0.5) * ROAD_TILE_PX / usableLength;
            t = Math.min(t, 1.0);

            double px = startX + (endX - startX) * t;
            double py = startY + (endY - startY) * t;

            ImageView tile = new ImageView(tileImg);
            tile.setFitWidth(ROAD_TILE_PX);
            tile.setPreserveRatio(true);
            tile.setSmooth(true);

            double tileWidth = tile.getFitWidth();
            double tileHeight = tile.getImage().getHeight() * (tileWidth / tile.getImage().getWidth());

            // Center the tile
            tile.setX(px - tileWidth / 2.0);
            tile.setY(py - tileHeight / 2.0);

            tile.setRotate(angleDeg);

            tiles.add(tile);
        }
        return tiles;
    }

    public Collection<Intersection> getIntersections()
    {
        return Collections.unmodifiableSet(intersectionViewMgrs.keySet());
    }
}