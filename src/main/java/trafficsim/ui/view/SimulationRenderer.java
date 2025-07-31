/***************************************************************

- File:        SimulationRenderer.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Handles all JavaFX rendering for the TrafficSim simulation.

- Description:
- Manages the creation, updating, and removal of all visual elements
- in the simulation, including intersections, roads, cars, and traffic
- lights. Synchronizes the UI with the simulation model state and
- provides interaction hooks for editing and selection.

***************************************************************/

package trafficsim.ui.view;

import java.util.*;
import java.util.function.Consumer;
import javafx.animation.AnimationTimer;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import trafficsim.core.model.*;
import trafficsim.core.sim.SimulationEngine;
import trafficsim.ui.adapter.*;
import trafficsim.ui.controller.MainController;
import trafficsim.ui.view.intersection.*;
import trafficsim.ui.controller.helpers.InteractionModeManager.Mode;

public class SimulationRenderer
{
    private static final double ROAD_TILE_PX = 48.0;

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
    private final Map<Road, Group> roadTileViews = new HashMap<>();

    private final Map<Car, Group> carDataBubbleViews = new HashMap<>();
    private final Map<Car, Text> carDataTexts = new HashMap<>();

    private boolean areBubblesGloballyVisible = true;

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

    /**
    * Called when a new intersection is added to the simulation.
    * Creates and registers the corresponding IntersectionView and adds it to the UI.
    *
    * @param intersection The intersection model object to render.
    */
    public void onIntersectionAdded(Intersection intersection)
    {
        IntersectionView viewMgr = buildViewManager(intersection);
        intersectionViewMgrs.put(intersection, viewMgr);

        intersectionPane.getChildren().addAll(viewMgr.getBaseNodes());
    }

    /**
    * Retrieves the IntersectionView associated with the given model.
    *
    * @param model The intersection model.
    * @return The corresponding IntersectionView, or null if not present.
    */
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

            if (viewMgr instanceof SignalisedIntersectionView)
            {
                ((SignalisedIntersectionView) viewMgr).removeAllSignalViews(lightPane);
            }
        }
    }

    /**
    * Removes the visual representation of the specified road from the UI.
    *
    * @param road The road to remove.
    */
    public void removeRoad(Road road)
    {
        Line view = roadViews.remove(road);
        if (view != null)
        {
            roadPane.getChildren().remove(view);
        }

        Group tiles = roadTileViews.remove(road);
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

    /**
    * Called when a new road is added to the simulation.
    * Creates and registers the corresponding JavaFX Line and tile graphics.
    *
    * @param road The road model object to render.
    */
    public void onRoadAdded(Road road)
    {
        Line line = buildRoadView(road);
        roadViews.put(road, line);
        roadPane.getChildren().add(line);

        if (isCanonical(road))
        {
            Group tiles = buildRoadTiles(road);
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

    /**
    * Called when a new car is added to the simulation.
    * Creates and registers the corresponding ImageView and data bubble.
    *
    * @param car The car model object to render.
    */
    public void onCarAdded(Car car)
    {
        ImageView view = buildCarView(car);
        carViews.put(car, view);
        carAdapters.put(car, new CarAdapter(car));
        carPane.getChildren().add(view);

        Group bubble = buildCarDataBubble(car);
        carDataBubbleViews.put(car, bubble);
        carPane.getChildren().add(bubble);
        bubble.setVisible(areBubblesGloballyVisible && car.getShowDataBubble());
    }

    /**
    * Removes the visual representation of the specified car from the UI.
    *
    * @param car The car to remove.
    */
    public void removeCar(Car car)
    {
        ImageView view = carViews.remove(car);
        if (view != null)
        {
            carPane.getChildren().remove(view);
        }
        carAdapters.remove(car);

        Group bubble = carDataBubbleViews.remove(car);
        if (bubble != null)
        {
            carPane.getChildren().remove(bubble);
        }
        carDataTexts.remove(car);
    }

    /**
    * Removes all visual elements from the simulation UI.
    * Clears all internal mappings and JavaFX nodes.
    */
    public void clearAll()
    {
        carViews.clear();
        carAdapters.clear();
        intersectionViewMgrs.clear();
        roadViews.clear();
        carDataBubbleViews.clear();
        carDataTexts.clear();

        intersectionPane.getChildren().clear();
        roadPane.getChildren().clear();
        carPane.getChildren().clear();
        lightPane.getChildren().clear();
    }

    /**
    * Updates all car and intersection views to reflect the current simulation state.
    * Called on every animation frame.
    */
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

            // Data Bubble
            Group bubble = carDataBubbleViews.get(car);
            Text dataText = carDataTexts.get(car);
            if (bubble == null || dataText == null)
            {
                continue;
            }

            bubble.setVisible(areBubblesGloballyVisible && car.getShowDataBubble());

            double bubbleHeight = 50;
            double pointerHeight = 10;
            bubble.setLayoutX(centerX - bubble.getBoundsInLocal().getWidth() / 2.0);
            bubble.setLayoutY(centerY - fitH / 2.0 - bubbleHeight - pointerHeight);

            Vec2 pos = car.worldPos();
            double currentSpeedMps = car.getVelocity();
            double currentSpeedMph = UnitConverter.mpsToMph(currentSpeedMps);
            dataText.setText(String.format("X: %.1f, Y: %.1f\nSpd: %2.0f mph", pos.x, pos.y, currentSpeedMph));
        }

        for (IntersectionView viewMgr : intersectionViewMgrs.values())
        {
            viewMgr.updateView();
        }
    }

    /**
    * Creates a data bubble UI element for the specified car, displaying its position and speed.
    *
    * @param car The car model object.
    * @return A JavaFX Group representing the data bubble.
    */
    private Group buildCarDataBubble(Car car)
    {
        Rectangle bubbleBg = new Rectangle(120, 50);
        bubbleBg.setArcWidth(20);
        bubbleBg.setArcHeight(20);
        bubbleBg.setFill(Color.rgb(0, 0, 0, 0.6));
        bubbleBg.setStroke(Color.WHITE);
        bubbleBg.setStrokeWidth(1.5);

        Polygon pointer = new Polygon();
        pointer.getPoints().addAll(50.0, 50.0, 70.0, 50.0, 60.0, 60.0);

        pointer.setFill(Color.rgb(0, 0, 0, 0.7));
        pointer.setStroke(Color.WHITE);
        pointer.setStrokeWidth(1.5);

        Text dataText = new Text("X: 0, Y: 0\nSpd: 0 mph");
        dataText.setFill(Color.WHITE);
        dataText.setTextAlignment(TextAlignment.CENTER);
        dataText.setLayoutX(10);
        dataText.setLayoutY(20);
        carDataTexts.put(car, dataText);

        return new Group(bubbleBg, pointer, dataText);
    }

    /**
    * Sets the global visibility of all car data bubbles.
    *
    * @param visible True to show all bubbles, false to hide.
    */
    public void setAllBubblesVisible(boolean visible)
    {
        this.areBubblesGloballyVisible = visible;
        for (Map.Entry<Car, Group> entry : carDataBubbleViews.entrySet())
        {
            Car car = entry.getKey();
            Group bubble = entry.getValue();
            bubble.setVisible(visible && car.getShowDataBubble());
        }
    }

    /**
    * Creates and returns the ImageView for a car, including mouse event handlers.
    *
    * @param car The car model object.
    * @return The ImageView representing the car.
    */
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

    /**
    * Constructs the appropriate IntersectionView for the given intersection type.
    *
    * @param intersection The intersection model.
    * @return The IntersectionView for the intersection.
    * @throws IllegalArgumentException if the intersection type is unknown.
    */
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

    /**
    * Creates a JavaFX Line representing the road, with proper lane offset and endpoints.
    *
    * @param road The road model.
    * @return The Line node for the road.
    */
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
        line.setVisible(false);

        return line;
    }

    /**
    * Creates a group of tiled road images along the given road segment.
    *
    * @param road The road model.
    * @return A Group containing the tiled road images.
    */
    private Group buildRoadTiles(Road road)
    {
        Group tileGroup = new Group();
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

        // double angleRad = Math.atan2(dy, dx);
        double angleDeg = Math.toDegrees(Math.atan2(dy, dx));

        for (int i = 0; i < nTiles; ++i)
        {
            double t = Math.min(1.0, (i + 0.5) * ROAD_TILE_PX / usableLength);
            // t = Math.min(t, 1.0);

            double px = startX + (endX - startX) * t;
            double py = startY + (endY - startY) * t;

            ImageView tile = new ImageView(tileImg);
            tile.setFitWidth(ROAD_TILE_PX);
            tile.setPreserveRatio(true);
            tile.setSmooth(true);
            // tile.setMouseTransparent(true);

            double tileWidth = tile.getFitWidth();
            double tileHeight = tile.getImage().getHeight() * (tileWidth / tile.getImage().getWidth());

            // Center the tile
            tile.setX(px - tileWidth / 2.0);
            tile.setY(py - tileHeight / 2.0);
            tile.setRotate(angleDeg);

            tileGroup.getChildren().add(tile);
        }

        DropShadow glow = new DropShadow(BlurType.GAUSSIAN, Color.ORANGE, 12, 0.7, 0, 0);

        tileGroup.setOnMouseEntered(e ->
        {
            if (controller.getCurrentMode() == Mode.NORMAL)
            {
                tileGroup.setEffect(glow);
                roadPane.getScene().setCursor(Cursor.HAND);
            }
        });

        tileGroup.setOnMouseExited(e ->
        {
            if (controller.getCurrentMode() == Mode.NORMAL)
            {
                tileGroup.setEffect(null);
                roadPane.getScene().setCursor(Cursor.DEFAULT);
            }
        });

        tileGroup.setOnMouseClicked(e ->
        {
            if (controller.getCurrentMode() == Mode.NORMAL)
            {
                controller.selectForEditing(road);
            }
        });

        return tileGroup;
    }

    /**
    * Returns an unmodifiable collection of all intersection model objects currently rendered.
    *
    * @return Collection of Intersection objects.
    */
    public Collection<Intersection> getIntersections()
    {
        return Collections.unmodifiableSet(intersectionViewMgrs.keySet());
    }
}