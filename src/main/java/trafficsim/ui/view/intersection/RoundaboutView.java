/***************************************************************

- File:        RoundaboutView.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Renders and manages the UI for roundabout intersections.

- Description:
- Provides the JavaFX visual representation for roundabout intersections,
- including the roundabout image and user interaction handlers for editing
- and selection.

***************************************************************/

package trafficsim.ui.view.intersection;

import java.util.Objects;
import java.util.function.Consumer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import trafficsim.core.model.Intersection;
import trafficsim.ui.controller.MainController;

public class RoundaboutView extends IntersectionView
{
    /**
    * Constructs a RoundaboutView for the given intersection model.
    * Initializes the roundabout image and sets up interaction handlers.
    *
    * @param model        The intersection model to visualize (should be a Roundabout).
    * @param editAction   Callback to invoke when the intersection is selected for editing.
    * @param controller   Reference to the main controller for interaction context.
    */
    public RoundaboutView(Intersection model, Consumer<Intersection> editAction, MainController controller)
    {
        super(model, editAction, controller);

        double px = model.position().x * 10.0;
        double py = model.position().y * 10.0;

        Image roundaboutImage = new Image(Objects
                .requireNonNull(getClass().getResourceAsStream("/trafficsim/assets/images/roundabout_tile.png")));
        ImageView roundaboutView = new ImageView(roundaboutImage);

        double imageSize = 140.0;
        roundaboutView.setFitWidth(imageSize);
        roundaboutView.setFitHeight(imageSize);
        roundaboutView.setX(px - imageSize / 2.0);
        roundaboutView.setY(py - imageSize / 2.0);
        roundaboutView.setPreserveRatio(true);
        roundaboutView.setSmooth(true);

        baseNodes.add(roundaboutView);
        attachMouseHandlers(roundaboutView, editAction, controller);
    }

    /**
    * Updates the roundabout view.
    * (No dynamic state to update; method present for interface compatibility.)
    */
    @Override
    public void updateView()
    {

    }
}