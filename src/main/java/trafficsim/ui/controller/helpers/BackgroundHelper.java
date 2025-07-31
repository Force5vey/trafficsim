/***************************************************************

- File:        BackgroundHelper.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Utility for setting up the simulation background in the UI.

- Description:
- Loads and applies a tiled grass image as the background for the simulation
- area. Ensures the background is visually consistent and non-interactive.

***************************************************************/

package trafficsim.ui.controller.helpers;

import java.util.Objects;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

public final class BackgroundHelper
{
    private BackgroundHelper()
    {
    }

    /**
    * Sets up the background of the given pane with a repeating grass tile image.
    * Makes the background pane mouse transparent.
    *
    * @param backgroundPane The JavaFX Pane to set the background for.
    */
    public static void setupBackground(Pane backgroundPane)
    {
        Image grassImage = new Image(Objects.requireNonNull(
                BackgroundHelper.class.getResourceAsStream("/trafficsim/assets/images/grass_tile.png")));

        if (grassImage.isError())
        {
            System.err.println("Failed to load grass tile image.");
            return;
        }

        BackgroundImage backgroundImage = new BackgroundImage(grassImage, BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);

        backgroundPane.setBackground(new Background(backgroundImage));
        backgroundPane.setMouseTransparent(true);
    }
}
