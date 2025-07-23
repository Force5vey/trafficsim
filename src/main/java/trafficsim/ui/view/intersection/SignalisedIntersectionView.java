package trafficsim.ui.view.intersection;

import java.util.function.Consumer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import trafficsim.core.model.Intersection;
import trafficsim.ui.adapter.IntersectionUtil;
import trafficsim.ui.controller.MainController;

public class SignalisedIntersectionView extends IntersectionView
{
    public SignalisedIntersectionView(Intersection model, Consumer<Intersection> editAction, MainController controller)
    {
        super(model, editAction, controller);

        Rectangle lightView = new Rectangle(10, 40);
        lightView.setFill(Color.DARKSLATEGRAY);
        lightView.setX(-lightView.getWidth() / 2);
        lightView.setY(-lightView.getHeight() / 2);

        getChildren().addAll(highlight, lightView);

        double px = IntersectionUtil.toPx(model.position().x);
        double py = IntersectionUtil.toPx(model.position().y);

        setLayoutX(px);
        setLayoutY(py);
    }
}