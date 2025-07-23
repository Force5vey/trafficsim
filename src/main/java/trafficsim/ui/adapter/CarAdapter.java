package trafficsim.ui.adapter;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

import trafficsim.core.model.Car;
import trafficsim.core.model.Vec2;

public final class CarAdapter
{
    private static final double PX_PER_M = 10.0;

    private final Car model;
    private final ReadOnlyDoubleWrapper xPx = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper yPx = new ReadOnlyDoubleWrapper();

    public CarAdapter(Car model)
    {
        this.model = model;
    }

    public void pullFromModel()
    {
        Vec2 p = model.worldPos();
        xPx.set(p.x * PX_PER_M);
        yPx.set(p.y * PX_PER_M);
    }

    public ReadOnlyDoubleProperty xProperty()
    {
        return xPx.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty yProperty()
    {
        return yPx.getReadOnlyProperty();
    }
}
