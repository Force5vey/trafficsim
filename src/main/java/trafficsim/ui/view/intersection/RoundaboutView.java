package trafficsim.ui.view.intersection;

import java.util.Objects;
import java.util.function.Consumer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import trafficsim.core.model.Intersection;
import trafficsim.ui.controller.MainController;

public class RoundaboutView extends IntersectionView
{
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

    @Override
    public void updateView()
    {

    }
}