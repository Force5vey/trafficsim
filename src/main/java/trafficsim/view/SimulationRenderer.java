package trafficsim.view;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import trafficsim.model.Car;
import trafficsim.model.IIntersection;
import trafficsim.model.Roundabout;
import trafficsim.model.TrafficLightIntersection;
import trafficsim.service.SimulationService;

public class SimulationRenderer
{
    private final Pane simulationPane;
    private final SimulationService simulationService;

    public SimulationRenderer(Pane simulationPane, SimulationService simulationService)
    {
        this.simulationPane = simulationPane;
        this.simulationService = simulationService;

        drawRoad();

        this.simulationService.getCars().addListener(this::onCarModelChanged);
        this.simulationService.getIntersections().addListener(this::onIntersectionModelChanged);
    }

    private void drawRoad()
    {
        Rectangle road = new Rectangle(0, 335, 1280, 50); //TODO, needs to get sized to waht the Pae size is.
        road.setFill(Color.GRAY);
        simulationPane.getChildren().add(road);
    }

    private void onCarModelChanged(ListChangeListener.Change<? extends Car> c)
    {
        while (c.next())
        {
            if (c.wasRemoved())
            {
                // TODO: view removal logic here
            }
            if (c.wasAdded())
            {
                for (Car addedCar : c.getAddedSubList())
                {
                    createViewForCar(addedCar);
                }
            }
        }
    }

    private void onIntersectionModelChanged(ListChangeListener.Change<? extends IIntersection> c)
    {
        while (c.next())
        {
            if (c.wasRemoved())
            {
                // TODO: removal logic here
            }

            if (c.wasAdded())
            {
                for (IIntersection addedIntersection : c.getAddedSubList())
                {
                    createViewForIntersection(addedIntersection);
                }
            }
        }
    }

    private void createViewForCar(Car carModel)
    {
        Rectangle carView = new Rectangle(40, 20);
        carView.setFill(Color.CORNFLOWERBLUE);

        carView.xProperty().bind(carModel.xPositionProperty());
        carView.setY(350);

        simulationPane.getChildren().add(carView);
    }

    private void createViewForIntersection(IIntersection intersection)
    {
        Node view = null;
        if (intersection instanceof TrafficLightIntersection)
        {
            Rectangle lightView = new Rectangle(10, 40);
            lightView.setFill(Color.DARKSLATEGRAY);
            //TODO: bind fill to the lightState proeprties
            view = lightView;
        } else if (intersection instanceof Roundabout)
        {
            Circle roundaboutView = new Circle(50, Color.GOLDENROD);
            roundaboutView.setStroke(Color.WHITE);
            view = roundaboutView;
        }

        if (view != null)
        {
            view.layoutXProperty().bind(intersection.positionXProperty());
            view.layoutYProperty().bind(intersection.positionYProperty());
            simulationPane.getChildren().add(view);

            view.setOnMouseClicked(event ->
            {
                // TODO: live editing dialog here
                System.out.println("Editing " + intersection.getClass().getSimpleName());
                event.consume();
            });
        }
    }
}
