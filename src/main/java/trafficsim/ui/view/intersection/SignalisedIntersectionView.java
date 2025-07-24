package trafficsim.ui.view.intersection;

import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import trafficsim.core.model.Intersection;
import trafficsim.ui.adapter.IntersectionUtil;
import trafficsim.ui.controller.MainController;
import trafficsim.core.model.Road;
import trafficsim.core.model.SignalGroup;
import trafficsim.core.model.SignalisedIntersection;
import trafficsim.core.model.TrafficLightState;
import trafficsim.core.model.Vec2;

public class SignalisedIntersectionView extends IntersectionView
{
    private static final double SIGNAL_OFFSET_PX = 25.0;
    private static final double SIGNAL_RADIUS_PX = 5.0;

    private final Map<Road, Circle> signalViews = new HashMap<>();
    private final SignalisedIntersection sigModel;

    public SignalisedIntersectionView(Intersection model, Consumer<Intersection> editAction, MainController controller)
    {
        super(model, editAction, controller);
        this.sigModel = (SignalisedIntersection) model;

        double px = model.position().x * 10.0;
        double py = model.position().y * 10.0;

        Rectangle horizontalBar = new Rectangle(40, 16, Color.DARKSLATEGRAY);
        horizontalBar.setX(px - 20);
        horizontalBar.setY(py - 8);

        Rectangle verticalBar = new Rectangle(16, 40, Color.DARKSLATEGRAY);
        verticalBar.setX(px - 8);
        verticalBar.setY(py - 20);

        baseNodes.add(horizontalBar);
        baseNodes.add(verticalBar);

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