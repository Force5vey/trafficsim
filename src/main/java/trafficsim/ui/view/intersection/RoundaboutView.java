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

        double px = model.position().x * 10.0;
        double py = model.position().y * 10.0;

        Circle roundaboutShape = new Circle(30, Color.DARKGRAY);
        roundaboutShape.setStroke(Color.WHITE);
        roundaboutShape.setCenterX(px);
        roundaboutShape.setCenterY(py);

        baseNodes.add(roundaboutShape);
        attachMouseHandlers(roundaboutShape, editAction, controller);
    }

    @Override
    public void updateView()
    {

    }
}