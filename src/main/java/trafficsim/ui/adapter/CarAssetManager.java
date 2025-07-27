package trafficsim.ui.adapter;

import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CarAssetManager
{
    private static final String[] IMAGE_FILES =
    { "/trafficsim/assets/images/car_blue.png" };

    private static final List<Image> images = new ArrayList<>();
    private static int nextIndex = 0;

    static
    {
        for (String path : IMAGE_FILES)
        {
            Image img = new Image(Objects.requireNonNull(CarAssetManager.class.getResourceAsStream(path)));
            images.add(img);
        }
        if (images.isEmpty())
        {
            throw new IllegalStateException("No car images found in assets.");
        }
    }

    private CarAssetManager()
    {
    }

    public static synchronized Image getNextCarImage()
    {
        Image img = images.get(nextIndex);
        nextIndex = (nextIndex + 1) % images.size();
        return img;
    }
}
