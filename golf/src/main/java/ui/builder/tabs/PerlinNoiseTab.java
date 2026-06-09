package ui.builder.tabs;

import ui.builder.CoursePreview;
import domain.course.GolfCourse;
import domain.terrain.RandomCourseGenerator;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class PerlinNoiseTab extends BorderPane {

    private CoursePreview coursePreview;
    private CheckBox enableToggle;

    public PerlinNoiseTab(GolfCourse course, double[] preViewSize) {
        this.coursePreview = new CoursePreview(course, preViewSize[0], preViewSize[1]);

        VBox rightMenu = new VBox(15);
        rightMenu.setPadding(new Insets(20));
        rightMenu.setAlignment(Pos.TOP_LEFT);
        rightMenu.setPrefWidth(250);
        rightMenu.setStyle("-fx-background-color: rgba(244, 244, 244, 0.9); -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Procedural Generation");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        enableToggle = new CheckBox("Enable Perlin Noise");
        enableToggle.setSelected(course.usePerlinNoise);

        Label seedLabel = new Label("World Seed (Text or Numbers):");
        TextField seedField = new TextField("Maastricht"); // Default starting seed
        seedField.setDisable(!course.usePerlinNoise);

        Button randomSeedBtn = new Button("Generate Random Seed");
        randomSeedBtn.setMaxWidth(Double.MAX_VALUE);
        randomSeedBtn.setDisable(!course.usePerlinNoise);

        Label elevationLabel = new Label("Water Elevation: 0.0");
        Slider elevationSlider = new Slider(-1.5, 1.5, 0.0); // Min -3, Max 3, Default 0
        elevationSlider.setShowTickLabels(true);
        elevationSlider.setShowTickMarks(true);
        elevationSlider.setMajorTickUnit(0.5);

        elevationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            elevationLabel.setText(String.format("Water Elevation: %.1f", newVal));
            course.setGlobalElevation(-(newVal.doubleValue()));
            coursePreview.updatePreview();
        });

        Label bottomLabel = new Label("Note: \nDisable Perlin noise to \nre-enable all the tabs!");
        bottomLabel.setStyle("-fx-text-fill: #626c6d; -fx-font-size: 20px; -fx-font-style: italic;"); // Een beetje subtiele styling
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        rightMenu.getChildren().addAll(
            title,
            new Label("Use organic Minecraft-style terrain \ninstead of math formulas."),
            enableToggle,
            seedLabel,
            seedField,
            randomSeedBtn,
            elevationLabel,
            elevationSlider,
            spacer,
            bottomLabel
        );

        // Checkbox Toggle
        enableToggle.setOnAction(e -> {
            boolean isEnabled = enableToggle.isSelected();
            course.usePerlinNoise = isEnabled;
            
            // Lock or unlock the text field based on the toggle
            seedField.setDisable(!isEnabled);
            randomSeedBtn.setDisable(!isEnabled);

            if (isEnabled) {
                applySeed(course, seedField.getText());
            } else {
                coursePreview.updatePreview(); // Revert preview back to the standard math formula
            }
        });

        // Real-time Seed Typing
        seedField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Instantly rebuild the map as the user types!
            if (course.usePerlinNoise && newVal != null && !newVal.isEmpty()) {
                applySeed(course, newVal);
            }
        });

        // Randomize Button
        randomSeedBtn.setOnAction(e -> {
            // Generate a random string and inject it into the text field.
            // The text listener above will catch this automatically and rebuild the map!
            int randomNum = (int) (Math.random() * 900000) + 100000;
            seedField.setText(String.valueOf(randomNum)); 
        });

        this.setCenter(coursePreview);
        this.setRight(rightMenu);
    }

    private void applySeed(GolfCourse course, String seedText) {
        // Convert the typed word into a valid numerical seed
        long numericSeed = seedText.hashCode(); 
        RandomCourseGenerator.generateSeededCourse(course, numericSeed);
        coursePreview.updatePreview();
    }

    public void refreshView() {
        if (coursePreview != null) {
            coursePreview.updatePreview();
        }
    }

    /**
     * Expose this property so the main GUI_courseBuilder can "listen" to 
     * the checkbox and lock out the other tabs when it is active.
     */
    public BooleanProperty perlinEnabledProperty() {
        return enableToggle.selectedProperty();
    }
}
