package trafficsim.ui.view.intersection;

import javafx.scene.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.Cursor;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import trafficsim.core.model.Intersection;
import trafficsim.ui.adapter.IntersectionUtil;
import trafficsim.ui.controller.MainController;
import trafficsim.ui.controller.MainController.InteractionMode;

public abstract class IntersectionView
{
    protected Intersection model;
    protected Circle highlight;
    protected final List<Node> baseNodes = new ArrayList<>();

    private static final double HIGHLIGHT_RADIUS = 40;

    public IntersectionView(Intersection model, Consumer<Intersection> editAction, MainController controller)
    {
        this.model = model;

        this.highlight = new Circle(HIGHLIGHT_RADIUS, Color.TRANSPARENT);
        this.highlight.setStroke(Color.LIMEGREEN);
        this.highlight.setStrokeWidth(2);
        this.highlight.setVisible(false);

        this.highlight.setMouseTransparent(true);

        this.highlight.setCenterX(model.position().x * 10.0);
        this.highlight.setCenterY(model.position().y * 10.0);
    }

    public List<Node> getBaseNodes()
    {
        return baseNodes;
    }

    public Circle getHighlightNode()
    {
        return highlight;
    }

    protected void attachMouseHandlers(Node node, Consumer<Intersection> editAction, MainController controller)
    {
        node.setOnMouseEntered(event ->
        {
            InteractionMode mode = controller.getCurrentMode();
            if (mode == InteractionMode.NORMAL || mode == InteractionMode.PLACING_ROAD
                    || mode == InteractionMode.PLACING_CAR)
            {
                highlight.setVisible(true);
                node.getScene().setCursor(mode == InteractionMode.NORMAL ? Cursor.HAND : Cursor.CROSSHAIR);
            }
        });

        node.setOnMouseExited(event ->
        {
            highlight.setVisible(false);
            node.getScene().setCursor(Cursor.DEFAULT);
        });

        node.setOnMouseClicked(event ->
        {
            InteractionMode mode = controller.getCurrentMode();
            switch (mode) {
            case NORMAL:
                editAction.accept(model);
                break;
            case PLACING_ROAD:
                controller.onIntersectionPickedForRoad(model);
                break;
            case PLACING_CAR:
                controller.onIntersectionPickedForCar(model);
                break;
            default:
                //ignoring
            }
            event.consume();
        });
    }

    public Intersection getModel()
    {
        return model;
    }

    public abstract void updateView();

}
