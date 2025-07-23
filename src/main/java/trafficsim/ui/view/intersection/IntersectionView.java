package trafficsim.ui.view.intersection;

import java.util.function.Consumer;
import javafx.scene.Cursor;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import trafficsim.core.model.Intersection;
import trafficsim.ui.adapter.IntersectionUtil;
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

        double px = IntersectionUtil.toPx(model.position().x);
        double py = IntersectionUtil.toPx(model.position().y);

        setLayoutX(px);
        setLayoutY(py);

        setOnMouseEntered(event ->
        {
            InteractionMode mode = controller.getCurrentMode();

            if (mode == InteractionMode.NORMAL || mode == InteractionMode.PLACING_ROAD
                    || mode == InteractionMode.PLACING_CAR)
            {
                highlight.setVisible(true);

                if (mode == InteractionMode.PLACING_ROAD || mode == InteractionMode.PLACING_CAR)
                {
                    getScene().setCursor(Cursor.CROSSHAIR);
                } else
                {
                    getScene().setCursor(Cursor.HAND);
                }
            }
        });

        setOnMouseExited(event ->
        {
            highlight.setVisible(false);
            getScene().setCursor(Cursor.DEFAULT);
        });

        setOnMouseClicked(event ->
        {
            InteractionMode mode = controller.getCurrentMode();

            switch (mode) {
            case NORMAL:
                editAction.accept(model);
                break;
            case PLACING_ROAD:
                controller.onIntersectionPickedForRoad(model);
                break;
            case PLACING_CAR:
                controller.onIntersectionPickedForCar(model);
            default:
                /* ignoring clicks in other modes*/
            }
            event.consume();

        });
    }

    public Intersection getModel()
    {
        return model;
    }

}
