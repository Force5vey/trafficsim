package trafficsim.view.intersection;

import java.util.function.Consumer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import trafficsim.controller.MainController;
import trafficsim.model.IIntersection;

public class RoundaboutView extends IntersectionView
{
    public RoundaboutView(IIntersection model, Consumer<IIntersection> editAction, MainController controller)
    {
        super(model, editAction, controller);

        Circle roundaboutShape = new Circle(30, Color.DARKGRAY);
        roundaboutShape.setStroke(Color.WHITE);
        roundaboutShape.setCenterX(0);
        roundaboutShape.setCenterY(0);

        getChildren().addAll(highlight, roundaboutShape);
    }
}