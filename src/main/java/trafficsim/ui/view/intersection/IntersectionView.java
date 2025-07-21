package trafficsim.ui.view.intersection;

import java.util.function.Consumer;
import javafx.scene.Cursor;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import trafficsim.core.model.Intersection;
import trafficsim.ui.controller.MainController;
import trafficsim.ui.controller.MainController.InteractionMode;

public class IntersectionView extends Region
{
    protected Intersection model;
    protected Circle highlight;

    private static final double HIGHLIGHT_RADIUS = 40;

    public IntersectionView(Intersection model, Consumer<Intersection> editAction, MainController controller)
    {
        this.model = model;

        this.highlight = new Circle(HIGHLIGHT_RADIUS, Color.TRANSPARENT);
        this.highlight.setStroke(Color.LIMEGREEN);
        this.highlight.setStrokeWidth(2);
        this.highlight.setVisible(false);
        this.highlight.setCenterX(0);
        this.highlight.setCenterY(0);

        layoutXProperty().bind(model.positionXProperty());
        layoutYProperty().bind(model.positionYProperty());

        setOnMouseEntered(event ->
        {
            if (controller.getCurrentMode() == InteractionMode.NORMAL)
            {
                highlight.setVisible(true);
                getScene().setCursor(Cursor.HAND);
            }
        });

        setOnMouseExited(event ->
        {
            highlight.setVisible(false);
            getScene().setCursor(Cursor.DEFAULT);
        });

        setOnMouseClicked(event ->
        {
            if (controller.getCurrentMode() == InteractionMode.NORMAL)
            {
                editAction.accept(model);
                event.consume();
            }
        });
    }

    public Intersection getModel()
    {
        return model;
    }

}
