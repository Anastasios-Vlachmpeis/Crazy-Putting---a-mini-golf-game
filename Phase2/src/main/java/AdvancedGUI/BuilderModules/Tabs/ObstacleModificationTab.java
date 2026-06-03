package AdvancedGUI.BuilderModules.Tabs;

import java.util.Random;

import AdvancedGUI.BuilderModules.CoursePreview;
import GolfCourseData.GolfCourse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ObstacleModificationTab extends BorderPane {

    private final CoursePreview coursePreview;
    private final GolfCourse course;
    private final ComboBox<String> obstacleTypeBox;
    private final Slider radiusSlider;
    // private final TextField radiusField;
    // private final TextField xField;
    // private final TextField yField;
    private double radius = 2.0; 
    private final TextField randomCountField;
    private final Random random = new Random();

    public ObstacleModificationTab(GolfCourse course, double[] preViewSize) {
        this.course = course;
        this.coursePreview = new CoursePreview(course, preViewSize[0], preViewSize[1]);

        VBox rightMenu = new VBox(10);
        rightMenu.setPadding(new Insets(20));
        rightMenu.setAlignment(Pos.TOP_LEFT);
        rightMenu.setPrefWidth(250);
        rightMenu.setStyle("-fx-background-color: rgba(244, 244, 244, 0.9); -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Add Trees & Sandpits");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        obstacleTypeBox = new ComboBox<>();
        obstacleTypeBox.getItems().addAll("Sandpit", "Tree");
        obstacleTypeBox.setValue("Sandpit");
        obstacleTypeBox.setMaxWidth(Double.MAX_VALUE);

        Label radiusLabel = new Label("Radius: 2.0");
        radiusSlider = new Slider(0.25, 8.0, 2.0);
        radiusSlider.setShowTickLabels(true);
        radiusSlider.setShowTickMarks(true);
        radiusSlider.setMajorTickUnit(2.0);

        // radiusField = new TextField("2.0");
        // radiusField.setPromptText("Radius");

        radiusSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            radiusLabel.setText(String.format("Radius: %.1f", newVal.doubleValue()));
            /*radiusField.setText(String.format("%.2f", newVal.doubleValue()));*/
            radius = newVal.doubleValue();
        });

        // xField = new TextField();
        // xField.setPromptText("X coordinate");
        // yField = new TextField();
        // yField.setPromptText("Y coordinate");

        // Button addAtPointButton = new Button("Add at Point");
        // addAtPointButton.setMaxWidth(Double.MAX_VALUE);
        // addAtPointButton.setOnAction(e -> addObstacleFromFields());

        randomCountField = new TextField("5");
        randomCountField.setPromptText("Random count");

        Button addRandomButton = new Button("Add Random");
        addRandomButton.setMaxWidth(Double.MAX_VALUE);
        addRandomButton.setOnAction(e -> addRandomObstacles());

        Button clearSelectedButton = new Button("Clear Selected Type");
        clearSelectedButton.setMaxWidth(Double.MAX_VALUE);
        clearSelectedButton.setOnAction(e -> {
            if (isSandSelected()) {
                course.clearSandPits();
            } else {
                course.clearTrees();
            }
            coursePreview.updatePreview();
        });

        Button clearAllButton = new Button("Clear All Obstacles");
        clearAllButton.setMaxWidth(Double.MAX_VALUE);
        clearAllButton.setOnAction(e -> {
            course.clearObstacles();
            coursePreview.updatePreview();
        });

        rightMenu.getChildren().addAll(
            title,
            new Label("Obstacle Type"), obstacleTypeBox,
            radiusLabel, radiusSlider, /*radiusField,*/
            new Separator(),
            /*new Label("Specific Point"), xField, yField, addAtPointButton,
            new Separator(),*/
            new Label("Random Placement"), randomCountField, addRandomButton,
            new Separator(),
            clearSelectedButton, clearAllButton
        );

        this.setCenter(coursePreview);
        this.setRight(rightMenu);

        coursePreview.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                addObstacleAtPixel(e.getX(), e.getY());
            }
        });
    }

    // private void addObstacleFromFields() {
    //     try {
    //         double x = Double.parseDouble(xField.getText().trim());
    //         double y = Double.parseDouble(yField.getText().trim());
    //         addObstacleAtCoursePoint(x, y, getRadius());
    //     } catch (NumberFormatException ex) {
    //         System.out.println("Obstacle coordinates or radius are invalid.");
    //     }
    // }

    private void addObstacleAtPixel(double pixelX, double pixelY) {
        double[] point = pixelToCoursePoint(pixelX, pixelY);
        if (point != null) {
            // xField.setText(String.format("%.2f", point[0]));
            // yField.setText(String.format("%.2f", point[1]));
            addObstacleAtCoursePoint(point[0], point[1], getRadius());
        }
    }

    private void addRandomObstacles() {
        try {
            int count = Math.max(0, Integer.parseInt(randomCountField.getText().trim()));
            double radius = getRadius();
            double[] size = course.getSize();
            double minX = size[0] + radius;
            double maxX = size[1] - radius;
            double minY = size[2] + radius;
            double maxY = size[3] - radius;

            if (minX > maxX || minY > maxY) {
                return;
            }

            int added = 0;
            int attempts = 0;
            int maxAttempts = count * 20;
            while (added < count && attempts < maxAttempts) {
                attempts++;
                double x = minX + random.nextDouble() * (maxX - minX);
                double y = minY + random.nextDouble() * (maxY - minY);
                if (addObstacleAtCoursePoint(x, y, radius, false)) {
                    added++;
                }
            }
            coursePreview.updatePreview();
        } catch (NumberFormatException ex) {
            System.out.println("Random obstacle count or radius is invalid.");
        }
    }

    private boolean addObstacleAtCoursePoint(double x, double y, double radius) {
        return addObstacleAtCoursePoint(x, y, radius, true);
    }

    private boolean addObstacleAtCoursePoint(double x, double y, double radius, boolean refresh) {
        double[] size = course.getSize();
        if (x < size[0] || x > size[1] || y < size[2] || y > size[3]) {
            return false;
        }

        boolean added = false;
        if (isSandSelected()) {
            added = course.addSandPit(x, y, radius);
        } else if (isTreeSelected()) {
            added = course.addTree(x, y, radius);
        }

        if (!added) {
            System.out.println("Obstacle cannot be placed there.");
            return false;
        }

        if (refresh) {
            coursePreview.updatePreview();
        }
        return true;
    }

    private double[] pixelToCoursePoint(double pixelX, double pixelY) {
        double canvasW = coursePreview.getWidth();
        double canvasH = coursePreview.getHeight();

        double[] size = course.getSize();
        double minX = size[0];
        double maxX = size[1];
        double minY = size[2];
        double maxY = size[3];

        double gameWidth = maxX - minX;
        double gameHeight = maxY - minY;
        double scale = Math.min(canvasW / gameWidth, canvasH / gameHeight);

        double canvasCenterX = canvasW / 2.0;
        double canvasCenterY = canvasH / 2.0;
        double gameCenterX = (minX + maxX) / 2.0;
        double gameCenterY = (minY + maxY) / 2.0;

        double courseX = gameCenterX + (pixelX - canvasCenterX) / scale;
        double courseY = gameCenterY - (pixelY - canvasCenterY) / scale;

        if (courseX < minX || courseX > maxX || courseY < minY || courseY > maxY) {
            return null;
        }
        return new double[]{courseX, courseY};
    }

    private boolean isSandSelected() {
        return "Sandpit".equals(obstacleTypeBox.getValue());
    }

    private boolean isTreeSelected() {
        return "Tree".equals(obstacleTypeBox.getValue());
    }

    private double getRadius() {
        try {
            radius = Math.max(radiusSlider.getMin(), Math.min(radiusSlider.getMax(), radius));
            radiusSlider.setValue(radius);
            return radius;
        } catch (NumberFormatException ex) {
            return radiusSlider.getValue();
        }
    }

    public void refreshView() {
        if (coursePreview != null) {
            coursePreview.updatePreview();
        }
    }
}
