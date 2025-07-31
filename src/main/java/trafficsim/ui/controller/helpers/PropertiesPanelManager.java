/***************************************************************

- File:        PropertiesPanelManager.java
- Date:        1 August 2025
- Author:      Edmond Leaveck
- Purpose:     Manages the properties panel UI for editing and creating simulation objects.

- Description:
- Handles the display, validation, and editing of properties for intersections,
- roads, and cars. Provides methods to create and validate model objects from
- user input and to generate update events for the simulation engine.

***************************************************************/

package trafficsim.ui.controller.helpers;

import java.util.Optional;
import java.util.function.Consumer;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.*;
import trafficsim.core.events.ModelCommandEvent;
import trafficsim.core.events.UpdateItemEvent;
import trafficsim.core.model.*;
import trafficsim.ui.adapter.UnitConverter;
import trafficsim.ui.controller.helpers.InteractionModeManager.Mode;

public class PropertiesPanelManager
{
    // UI Controls 
    private final TitledPane propertiesPane;
    private final GridPane propertiesGrid;
    private final Label validationLabel;
    private final HBox editButtonsBox;
    private final Button deleteButton;

    private ComboBox<String> intersectionTypeCombo;
    private Label param1Label, param2Label;
    private TextField param1Field, param2Field;
    private Label roadSpeedLabel;
    private TextField roadSpeedField;
    private Label carMaxSpeedLabel, carAccelLabel;
    private TextField carMaxSpeedField, carAccelField;
    private Label propertiesPlaceholderLabel;
    private CheckBox carShowBubbleCheck;

    public PropertiesPanelManager(TitledPane propertiesPane, GridPane propertiesGrid, Label validationLabel,
            HBox editButtonsBox, Button deleteButton)
    {
        this.propertiesPane = propertiesPane;
        this.propertiesGrid = propertiesGrid;
        this.validationLabel = validationLabel;
        this.editButtonsBox = editButtonsBox;
        this.deleteButton = deleteButton;
        createPropertyControls();
        this.propertiesPlaceholderLabel = new Label("Select an 'Add' action or click an item to edit properties.");
        this.propertiesPlaceholderLabel.setWrapText(true);
    }

    /**
    * Sets up the properties panel UI for the given interaction mode and selected item.
    * Populates the panel with appropriate controls and validation messages.
    *
    * @param mode         The current interaction mode.
    * @param selectedItem The item currently selected for editing, or null.
    */
    public void setupForMode(Mode mode, Object selectedItem)
    {
        propertiesPane.setVisible(true);
        propertiesGrid.getChildren().clear();
        validationLabel.setText("");
        editButtonsBox.setVisible(mode == Mode.EDITING);
        editButtonsBox.setManaged(mode == Mode.EDITING);

        int row = 0;

        switch (mode) {
        case PLACING_INTERSECTION:
            propertiesGrid.add(new Label("Type:"), 0, row++, 2, 1);
            propertiesGrid.add(intersectionTypeCombo, 0, row++, 2, 1);
            propertiesGrid.add(param1Label, 0, row++, 2, 1);
            propertiesGrid.add(param1Field, 0, row++, 2, 1);
            propertiesGrid.add(param2Label, 0, row++, 2, 1);
            propertiesGrid.add(param2Field, 0, row++, 2, 1);
            intersectionTypeCombo.getSelectionModel().selectFirst();
            break;
        case PLACING_ROAD:
            propertiesGrid.add(roadSpeedLabel, 0, row++, 2, 1);
            propertiesGrid.add(roadSpeedField, 0, row++, 2, 1);
            roadSpeedField.setText("35");
            setValidationMessage("Select the first intersection.", false);
            break;
        case PLACING_CAR:
            propertiesGrid.add(carMaxSpeedLabel, 0, row++, 2, 1);
            propertiesGrid.add(carMaxSpeedField, 0, row++, 2, 1);
            propertiesGrid.add(carAccelLabel, 0, row++, 2, 1);
            propertiesGrid.add(carAccelField, 0, row++, 2, 1);
            carMaxSpeedField.setText("30");
            carAccelField.setText("15.0");
            setValidationMessage("Click an intersection to spawn a car.", false);
            break;
        case EDITING:
            populatePropertiesForEdit(selectedItem);
            break;
        case NORMAL:
            reset();
            break;
        }
    }

    /**
    * Resets the properties panel to its default state.
    * Clears all controls and validation messages.
    */
    public void reset()
    {
        propertiesGrid.getChildren().clear();
        propertiesGrid.add(propertiesPlaceholderLabel, 0, 0, 2, 1);
        validationLabel.setText("");
        editButtonsBox.setVisible(false);
        editButtonsBox.setManaged(false);
    }

    /**
    * Sets a validation message in the properties panel.
    *
    * @param message The message to display.
    * @param isError True if the message is an error, false for normal info.
    */
    public void setValidationMessage(String message, boolean isError)
    {
        validationLabel.setText(message);
        validationLabel.setStyle(isError ? "-fx-text-fill: #ff3333;" : "-fx-text-fill: -fx-text-base-color;");
    }

    /**
    * Creates an Intersection object from the current UI input fields.
    * Validates input and returns an Optional containing the intersection if valid.
    *
    * @param x The x-coordinate for the intersection.
    * @param y The y-coordinate for the intersection.
    * @return  Optional containing the new Intersection, or empty if invalid.
    */
    public Optional<Intersection> createIntersectionFromUI(double x, double y)
    {
        validationLabel.setText("");
        try
        {
            String type = intersectionTypeCombo.getValue();
            if ("Traffic Light".equals(type))
            {
                double totalTime = Double.parseDouble(param1Field.getText());
                double yellow = Double.parseDouble(param2Field.getText());
                if (totalTime <= 0 || yellow <= 0 || yellow >= totalTime)
                {
                    validationLabel.setText("Invalid times. Ensure total > yellow > 0.");
                    return Optional.empty();
                }
                return Optional.of(new SignalisedIntersection(x, y, totalTime, yellow));
            } else if ("Roundabout".equals(type))
            {
                double speedMph = Double.parseDouble(param1Field.getText());
                if (speedMph <= 0)
                {
                    validationLabel.setText("Speed must be positive.");
                    return Optional.empty();
                }
                double speedMps = UnitConverter.mphToMps(speedMph);
                return Optional.of(new Roundabout(x, y, speedMps));
            }
        } catch (NumberFormatException e)
        {
            validationLabel.setText("Invalid number format in intersection properties.");
            return Optional.empty();
        }
        return Optional.empty();
    }

    /**
    * Validates the road speed input field and returns the value in meters per second.
    *
    * @return Optional containing the speed in m/s, or empty if invalid.
    */
    public Optional<Double> validateRoadSpeed()
    {
        validationLabel.setText("");
        try
        {
            double speedMph = Double.parseDouble(roadSpeedField.getText());
            if (speedMph <= 0)
            {
                validationLabel.setText("Speed must be positive.");
                return Optional.empty();
            }
            return Optional.of(UnitConverter.mphToMps(speedMph));
        } catch (NumberFormatException e)
        {
            validationLabel.setText("Invalid number for speed.");
            return Optional.empty();
        }
    }

    /**
    * Creates a Car object from the current UI input fields.
    * Validates input and returns an Optional containing the car if valid.
    *
    * @param net The RoadNetwork to associate with the car.
    * @return    Optional containing the new Car, or empty if invalid.
    */
    public Optional<Car> createCarFromUI(RoadNetwork net)
    {
        validationLabel.setText("");
        try
        {
            double vMph = Double.parseDouble(carMaxSpeedField.getText());
            double timeTo60 = Double.parseDouble(carAccelField.getText());
            if (vMph <= 0 || timeTo60 <= 0)
            {
                validationLabel.setText("Car properties must be positive.");
                return Optional.empty();
            }
            double vMps = UnitConverter.mphToMps(vMph);
            double aMps2 = UnitConverter.MPH_60_IN_MPS / timeTo60;
            return Optional.of(new Car(net, vMps, aMps2));
        } catch (NumberFormatException e)
        {
            validationLabel.setText("Invalid number format in car properties.");
            return Optional.empty();
        }
    }

    /**
    * Populates the properties panel with controls and values for editing the given item.
    *
    * @param item The model object to edit (Intersection, Road, or Car).
    */
    public void populatePropertiesForEdit(Object item)
    {
        propertiesGrid.getChildren().clear();
        deleteButton.setVisible(true);
        int row = 0;

        param1Label.setVisible(false);
        param1Field.setVisible(false);
        param2Label.setVisible(false);
        param2Field.setVisible(false);
        roadSpeedLabel.setVisible(false);
        roadSpeedField.setVisible(false);
        carMaxSpeedLabel.setVisible(false);
        carMaxSpeedField.setVisible(false);
        carAccelLabel.setVisible(false);
        carAccelField.setVisible(false);

        if (item instanceof SignalisedIntersection)
        {
            validationLabel.setText("Editing Signalised Intersection");
            SignalisedIntersection model = (SignalisedIntersection) item;

            param1Label.setText("Cycle (s):");
            param1Field.setText(String.format("%.1f", model.getTotalCycleTime()));
            param1Label.setVisible(true);
            param1Field.setVisible(true);
            propertiesGrid.add(param1Label, 0, row++, 2, 1);
            propertiesGrid.add(param1Field, 0, row++, 2, 1);

            param2Label.setText("Yellow (s):");
            param2Field.setText(String.format("%.1f", model.getYellowDuration()));
            param2Label.setVisible(true);
            param2Field.setVisible(true);

            propertiesGrid.add(param2Label, 0, row++, 2, 1);
            propertiesGrid.add(param2Field, 0, row++, 2, 1);
        } else if (item instanceof Roundabout)
        {
            validationLabel.setText("Editing Roundabout");
            Roundabout model = (Roundabout) item;
            double speedMph = UnitConverter.mpsToMph(model.getSpeedLimit());

            param1Label.setText("Speed (MPH):");
            param1Field.setText(String.format("%.1f", speedMph));
            param1Label.setVisible(true);
            param1Field.setVisible(true);

            propertiesGrid.add(param1Label, 0, row++, 2, 1);
            propertiesGrid.add(param1Field, 0, row++, 2, 1);
        } else if (item instanceof Road)
        {
            validationLabel.setText("Editing Road");
            Road model = (Road) item;
            double speedMph = UnitConverter.mpsToMph(model.speedLimit());

            roadSpeedLabel.setText("Speed (MPH):");
            roadSpeedField.setText(String.format("%.1f", speedMph));
            roadSpeedLabel.setVisible(true);
            roadSpeedField.setVisible(true);

            propertiesGrid.add(roadSpeedLabel, 0, row++, 2, 1);
            propertiesGrid.add(roadSpeedField, 0, row++, 2, 1);
        } else if (item instanceof Car)
        {
            validationLabel.setText("Editing Car");
            Car model = (Car) item;

            double maxSpdMph = UnitConverter.mpsToMph(model.getMaxSpeed());
            carMaxSpeedLabel.setText("Max Spd (MPH):");
            carMaxSpeedField.setText(String.format("%.1f", maxSpdMph));
            carMaxSpeedLabel.setVisible(true);
            carMaxSpeedField.setVisible(true);

            propertiesGrid.add(carMaxSpeedLabel, 0, row++, 2, 1);
            propertiesGrid.add(carMaxSpeedField, 0, row++, 2, 1);

            double accelMps2 = model.getAcceleration();
            String timeTo60Text = "";
            if (accelMps2 > 1e-6)
            {
                double timeTo60 = UnitConverter.MPH_60_IN_MPS / accelMps2;
                timeTo60Text = String.format("%.2f", timeTo60);
            }
            carAccelLabel.setText("0-60 Time (s)");
            carAccelField.setText(timeTo60Text);
            carAccelLabel.setVisible(true);
            carAccelField.setVisible(true);

            propertiesGrid.add(carAccelLabel, 0, row++, 2, 1);
            propertiesGrid.add(carAccelField, 0, row++, 2, 1);
            carShowBubbleCheck.setSelected(((Car) item).getShowDataBubble());
            propertiesGrid.add(carShowBubbleCheck, 0, row++, 2, 1);
        }
    }

    /**
     * Validates the UI fields for the selected item and, if valid, creates a
     * thread-safe event containing the update logic.
     *
     * @param item The model object selected for editing.
     * @return An Optional containing the event if validation succeeds, otherwise
     *         empty.
     */
    public Optional<ModelCommandEvent> createUpdateEvent(Object item)
    {
        try
        {
            if (item instanceof SignalisedIntersection)
            {
                SignalisedIntersection model = (SignalisedIntersection) item;
                double newTotalTime = Double.parseDouble(param1Field.getText());
                double newYellow = Double.parseDouble(param2Field.getText());
                if (newTotalTime <= 0 || newYellow <= 0 || newYellow >= newTotalTime)
                {
                    setValidationMessage("Invalid times. Ensure total > yellow > 0.", true);
                    return Optional.empty();
                }
                Consumer<SignalisedIntersection> updater = m ->
                {
                    m.setTotalCycleTime(newTotalTime);
                    m.setYellowDuration(newYellow);
                };
                return Optional.of(new UpdateItemEvent<>(model, updater));

            } else if (item instanceof Roundabout)
            {
                Roundabout model = (Roundabout) item;
                double newSpeedMph = Double.parseDouble(param1Field.getText());
                if (newSpeedMph <= 0)
                {
                    setValidationMessage("Speed must be positive.", true);
                    return Optional.empty();
                }
                Consumer<Roundabout> updater = m -> m.setSpeedLimit(UnitConverter.mphToMps(newSpeedMph));
                return Optional.of(new UpdateItemEvent<>(model, updater));

            } else if (item instanceof Road)
            {
                Road model = (Road) item;
                double newSpeedMph = Double.parseDouble(roadSpeedField.getText());
                if (newSpeedMph <= 0)
                {
                    setValidationMessage("Speed must be positive.", true);
                    return Optional.empty();
                }
                Consumer<Road> updater = m -> m.setSpeedLimit(UnitConverter.mphToMps(newSpeedMph));
                return Optional.of(new UpdateItemEvent<>(model, updater));

            } else if (item instanceof Car)
            {
                Car model = (Car) item;
                double newMaxSpdMph = Double.parseDouble(carMaxSpeedField.getText());
                double newTimeTo60 = Double.parseDouble(carAccelField.getText());
                if (newMaxSpdMph <= 0 || newTimeTo60 <= 0)
                {
                    setValidationMessage("Car properties must be positive.", true);
                    return Optional.empty();
                }

                boolean showBubble = carShowBubbleCheck.isSelected();

                Consumer<Car> updater = c ->
                {
                    c.setMaxSpeed(UnitConverter.mphToMps(newMaxSpdMph));
                    if (newTimeTo60 > 1e-6)
                    {
                        double newAccelMps2 = UnitConverter.MPH_60_IN_MPS / newTimeTo60;
                        c.setAcceleration(newAccelMps2);
                    }
                    c.setShowDataBubble(showBubble);
                };
                return Optional.of(new UpdateItemEvent<>(model, updater));
            }
        } catch (NumberFormatException e)
        {
            setValidationMessage("Invalid number format.", true);
            return Optional.empty();
        }
        return Optional.empty();
    }

    /**
    * Creates and initializes all property controls used in the properties panel.
    * Called once during construction.
    */
    private void createPropertyControls()
    {
        intersectionTypeCombo = new ComboBox<>();
        intersectionTypeCombo.getItems().setAll("Traffic Light", "Roundabout");
        intersectionTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateIntersectionFields(newVal));
        param1Label = new Label();
        param1Field = new TextField();
        param2Label = new Label();
        param2Field = new TextField();

        roadSpeedLabel = new Label("Speed (MPH):");
        roadSpeedField = new TextField();

        carMaxSpeedLabel = new Label("Max Spd (MPH):");
        carMaxSpeedField = new TextField();
        carAccelLabel = new Label("0-60 Time (s)");
        carAccelField = new TextField();

        carShowBubbleCheck = new CheckBox("Show Data Bubble");
    }

    /**
    * Updates the intersection property fields based on the selected intersection type.
    *
    * @param type The selected intersection type ("Traffic Light" or "Roundabout").
    */
    private void updateIntersectionFields(String type)
    {
        if ("Traffic Light".equals(type))
        {
            param1Label.setText("Cycle (s):");
            param1Field.setText("20");
            param2Label.setText("Yellow (s):");
            param2Field.setText("3");
            param2Label.setVisible(true);
            param2Field.setVisible(true);
        } else if ("Roundabout".equals(type))
        {
            param1Label.setText("Speed (MPH):");
            param1Field.setText("15");
            param2Label.setVisible(false);
            param2Field.setVisible(false);
        }
    }
}
