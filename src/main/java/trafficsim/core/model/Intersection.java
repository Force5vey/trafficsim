package trafficsim.core.model;

import javafx.beans.property.DoubleProperty;

public interface Intersection
{
    void update(double deltaTime);

    double getPositionX();

    double getPositionY();

    DoubleProperty positionXProperty();

    DoubleProperty positionYProperty();
}
