package trafficsim.ui.view.intersection;

import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import trafficsim.core.model.Intersection;
import trafficsim.ui.controller.MainController;
import trafficsim.core.model.Road;
import trafficsim.core.model.SignalGroup;
import trafficsim.core.model.SignalisedIntersection;
import trafficsim.core.model.TrafficLightState;
import trafficsim.core.model.Vec2;

public class SignalisedIntersectionView extends IntersectionView
{
    private static final double SIGNAL_OFFSET_PX = 45.0;
    private static final double SIGNAL_RADIUS_PX = 7.0;

    private final Map<Road, Circle> signalViews = new HashMap<>();
    private final SignalisedIntersection sigModel;

    public SignalisedIntersectionView(Intersection model, Consumer<Intersection> editAction, MainController controller)
    {
        super(model, editAction, controller);
        this.sigModel = (SignalisedIntersection) model;

        double px = model.position().x * 10.0;
        double py = model.position().y * 10.0;

        Image intersectionImage = new Image(Objects
                .requireNonNull(getClass().getResourceAsStream("/trafficsim/assets/images/intersection_tile.png")));
        ImageView intersectionView = new ImageView(intersectionImage);

        double imageSize = 140;
        intersectionView.setFitWidth(imageSize);
        intersectionView.setFitHeight(imageSize);
        intersectionView.setX(px - imageSize / 2.0);
        intersectionView.setY(py - imageSize / 2.0);
        intersectionView.setPreserveRatio(true);
        intersectionView.setSmooth(true);

        baseNodes.add(intersectionView);

        baseNodes.forEach(node -> attachMouseHandlers(node, editAction, controller));
    }

    public Node createSignalForRoad(Road road, Consumer<Intersection> editAction, MainController controller)
    {
        Vec2 roadOriginPos = road.from().position();
        Vec2 intersectionCenterPos = model.position();

        double angleRad = Math.atan2(roadOriginPos.y - intersectionCenterPos.y,
                roadOriginPos.x - intersectionCenterPos.x);

        double px = model.position().x * 10.0;
        double py = model.position().y * 10.0;

        double lightX = px + SIGNAL_OFFSET_PX * Math.cos(angleRad);
        double lightY = py + SIGNAL_OFFSET_PX * Math.sin(angleRad);

        Circle signalCircle = new Circle(lightX, lightY, SIGNAL_RADIUS_PX);
        signalCircle.setStroke(Color.BLACK);
        signalCircle.setFill(Color.DARKSLATEGRAY);

        signalViews.put(road, signalCircle);

        attachMouseHandlers(signalCircle, editAction, controller);

        return signalCircle;
    }

    public void removeSignalForRoad(Road road, Pane parentPane)
    {
        Circle signalCircle = signalViews.remove(road);
        if (signalCircle != null)
        {
            parentPane.getChildren().remove(signalCircle);
        }
    }

    @Override
    public void updateView()
    {
        Map<Road, SignalGroup> modelState = sigModel.getSignalMap();

        for (Map.Entry<Road, Circle> viewEntry : signalViews.entrySet())
        {
            Road road = viewEntry.getKey();
            Circle circle = viewEntry.getValue();
            SignalGroup group = modelState.get(road);

            if (group != null)
            {
                updateCircleColor(circle, group.state());
            }
        }
    }

    private void updateCircleColor(Circle circle, TrafficLightState state)
    {
        switch (state) {
        case GREEN:
            circle.setFill(Color.LIME);
            break;
        case YELLOW:
            circle.setFill(Color.YELLOW);
            break;
        case RED:
            circle.setFill(Color.RED);
            break;
        default:
            circle.setFill(Color.DARKSLATEGRAY);
            break;
        }
    }

    public void removeAllSignalViews(Pane parentPane)
    {
        for (Circle signalCircle : signalViews.values())
        {
            parentPane.getChildren().remove(signalCircle);
        }
        signalViews.clear();
    }
}