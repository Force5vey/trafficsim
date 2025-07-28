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
