/***************************************************************

- File:        CarAssetManager.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Manages car image assets for the simulation UI.

- Description:
- Loads and provides access to car images for rendering in the simulation.
- Supports cycling through available images to assign different appearances
- to cars. Ensures images are loaded only once and reused efficiently.

***************************************************************/

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

    /**
    * Returns the next car image in the sequence, cycling through available images.
    * Ensures that each car can have a distinct appearance if multiple images exist.
    *
    * @return The next Image object for a car.
    */
    public static synchronized Image getNextCarImage()
    {
        Image img = images.get(nextIndex);
        nextIndex = (nextIndex + 1) % images.size();
        return img;
    }
}
