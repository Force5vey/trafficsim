package trafficsim.ui.view.intersection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import trafficsim.core.model.Intersection;
import trafficsim.ui.controller.MainController;
import trafficsim.ui.controller.helpers.InteractionModeManager.Mode;

public abstract class IntersectionView
{
    protected Intersection model;
    protected final List<Node> baseNodes = new ArrayList<>();

    private static final DropShadow GLOW_EFFECT = new DropShadow(BlurType.GAUSSIAN, Color.ORANGE, 12, 0.7, 0, 0);

    public IntersectionView(Intersection model, Consumer<Intersection> editAction, MainController controller)
    {
        this.model = model;

    }

    public List<Node> getBaseNodes()
    {
        return baseNodes;
    }

    protected void attachMouseHandlers(Node node, Consumer<Intersection> editAction, MainController controller)
    {
        node.setOnMouseEntered(event ->
        {
            Mode mode = controller.getCurrentMode();
            if (mode == Mode.NORMAL || mode == Mode.PLACING_ROAD || mode == Mode.PLACING_CAR)
            {
                node.setEffect((GLOW_EFFECT));
                Cursor cursor = (mode == Mode.NORMAL) ? Cursor.HAND : Cursor.CROSSHAIR;
                node.getScene().setCursor(cursor);
            }
        });

        node.setOnMouseExited(event ->
        {
            node.setEffect(null);
            if (controller.getCurrentMode() == Mode.NORMAL)
            {
                node.getScene().setCursor(Cursor.DEFAULT);
            }
        });

        node.setOnMouseClicked(event ->
        {
            Mode mode = controller.getCurrentMode();
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
