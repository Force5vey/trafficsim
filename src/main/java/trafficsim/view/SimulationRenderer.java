package trafficsim.view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import trafficsim.controller.MainController;
import trafficsim.model.Car;
import trafficsim.model.IIntersection;
import trafficsim.model.Roundabout;
import trafficsim.model.TrafficLightIntersection;
import trafficsim.service.SimulationService;
import trafficsim.view.intersection.IntersectionView;
import trafficsim.view.intersection.RoundaboutView;
import trafficsim.view.intersection.TrafficLightIntersectionView;

public class SimulationRenderer
{
    private final Pane simulationPane;
    private final SimulationService simulationService;
    private final MainController controller;
    private final Map<IIntersection, Node> intersectionViewMap = new HashMap<>();

    public SimulationRenderer(Pane simulationPane, SimulationService simulationService, MainController controller)
    {
        this.simulationPane = simulationPane;
        this.simulationService = simulationService;
        this.controller = controller;

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
                for (IIntersection removed : c.getRemoved())
                {
                    Node view = intersectionViewMap.remove(removed);
                    if (view != null)
                    {
                        simulationPane.getChildren().remove(view);
                    }
                }
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
        IntersectionView view = null;

        Consumer<IIntersection> editCallback = controller::showEditIntersectionDialog;

        if (intersection instanceof TrafficLightIntersection)
        {
            view = new TrafficLightIntersectionView(intersection, editCallback, controller);
        } else if (intersection instanceof Roundabout)
        {
            view = new RoundaboutView(intersection, editCallback, controller);
        }

        if (view != null)
        {
            intersectionViewMap.put(intersection, view);
            simulationPane.getChildren().add(view);
        }
    }
}
