package trafficsim.view.intersection;

import java.util.function.Consumer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import trafficsim.controller.MainController;
import trafficsim.model.IIntersection;

public class TrafficLightIntersectionView extends IntersectionView
{
    public TrafficLightIntersectionView(IIntersection model, Consumer<IIntersection> editAction,
            MainController controller)
    {
        super(model, editAction, controller);

        Rectangle lightView = new Rectangle(10, 40);
        lightView.setFill(Color.DARKSLATEGRAY);
        lightView.setX(-lightView.getWidth() / 2);
        lightView.setY(-lightView.getHeight() / 2);

        getChildren().addAll(highlight, lightView);
    }
}