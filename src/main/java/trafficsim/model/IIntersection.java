package trafficsim.model;

import javafx.beans.property.DoubleProperty;

public interface IIntersection
{
    void update(double deltaTime);

    double getPositionX();

    double getPositionY();

    DoubleProperty positionXProperty();

    DoubleProperty positionYProperty();
}
