/***************************************************************

- File:        IntersectionView.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Abstract base class for intersection UI rendering.

- Description:
- Defines the interface and shared logic for JavaFX visual
- representations of intersections, including mouse interaction
- handling and update hooks for subclasses.

***************************************************************/

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

    /**
    * Constructs an IntersectionView for the given intersection model.
    *
    * @param model        The intersection model to visualize.
    * @param editAction   Callback to invoke when the intersection is selected for editing.
    * @param controller   Reference to the main controller for interaction context.
    */
    public IntersectionView(Intersection model, Consumer<Intersection> editAction, MainController controller)
    {
        this.model = model;

    }

    /**
    * Returns the list of JavaFX nodes that make up the base visual representation
    * of this intersection.
    *
    * @return List of JavaFX Node objects.
    */
    public List<Node> getBaseNodes()
    {
        return baseNodes;
    }

    /**
    * Attaches mouse event handlers to the given node for interaction modes
    * such as editing, road placement, and car placement.
    *
    * @param node        The JavaFX node to attach handlers to.
    * @param editAction  Callback for edit selection.
    * @param controller  Reference to the main controller for interaction context.
    */
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

    /**
    * Returns the intersection model associated with this view.
    *
    * @return The Intersection model object.
    */
    public Intersection getModel()
    {
        return model;
    }

    /**
    * Updates the visual representation of the intersection.
    * Subclasses must implement this to reflect model state changes.
    */
    public abstract void updateView();

}
