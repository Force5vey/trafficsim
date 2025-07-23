package trafficsim.ui.view.intersection;

import java.util.function.Consumer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import trafficsim.core.model.Intersection;
import trafficsim.ui.adapter.IntersectionUtil;
import trafficsim.ui.controller.MainController;

public class RoundaboutView extends IntersectionView
{
    public RoundaboutView(Intersection model, Consumer<Intersection> editAction, MainController controller)
    {
        super(model, editAction, controller);

        Circle roundaboutShape = new Circle(30, Color.DARKGRAY);
        roundaboutShape.setStroke(Color.WHITE);
        roundaboutShape.setCenterX(0);
        roundaboutShape.setCenterY(0);

        getChildren().addAll(highlight, roundaboutShape);

        double px = IntersectionUtil.toPx(model.position().x);
        double py = IntersectionUtil.toPx(model.position().y);

        setLayoutX(px);
        setLayoutY(py);
    }
}