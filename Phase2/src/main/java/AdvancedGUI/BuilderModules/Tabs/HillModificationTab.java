package AdvancedGUI.BuilderModules.Tabs;

import AdvancedGUI.BuilderModules.CoursePreview;
import GolfCourseData.GolfCourse;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

public class HillModificationTab extends BorderPane{

    private CoursePreview coursePreview;
    private GolfCourse course;

    private double currentMouseX = 0;
    private double currentMouseY = 0;
    private boolean isMousePressed = false;
    private MouseButton pressedButton;

    private Slider strengthSlider;
    private Slider widthSlider;

    public HillModificationTab(GolfCourse course, double[] preViewSize) {
        //gamePreview
        //Canvas previewCanvas = new Canvas(400, 400);
        //CoursePreview(previewCanvas, course); // Helper method to draw the map
        //canvas size
        this.course = course;
        this.coursePreview = new CoursePreview(course, preViewSize[0], preViewSize[1]);

        //Right side configurator
        VBox rightMenu = new VBox(15);
        rightMenu.setPadding(new Insets(20));
        rightMenu.setAlignment(Pos.TOP_LEFT);
        rightMenu.setPrefWidth(250);
        rightMenu.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Add Gaussian Hills");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label instructions = new Label("Left click to elevate the course\nRight click to lower the course");
        /* 
        TextField heightField = new TextField();
        heightField.setPromptText("Hill Height (e.g., 2.5)");
        
        TextField widthField = new TextField();
        widthField.setPromptText("Hill Width / Sigma");
        */
        Label strengthLabel = new Label("Strength: 1.0");
        strengthSlider = new Slider(0, 2.5, 1);
        strengthSlider.setShowTickLabels(true);
        strengthSlider.setShowTickMarks(true);
        strengthSlider.setMajorTickUnit(0.5);

        strengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            strengthLabel.setText(String.format("Strength: %.1f", newVal))
        );

        // Minimum is set to 0.5 because a width of 0 causes a division-by-zero crash in the physics loop
        Label widthLabel = new Label("Width: 2.0");
        widthSlider = new Slider(0.5, 10.0, 2.0); 
        widthSlider.setShowTickLabels(true);
        widthSlider.setShowTickMarks(true);
        widthSlider.setMajorTickUnit(2.5);
        
        widthSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            widthLabel.setText(String.format("Width: %.1f", newVal))
        );
    
        rightMenu.getChildren().addAll(
            title, 
            instructions,
            strengthLabel, strengthSlider, 
            widthLabel, widthSlider
        );

        //combine BorderPane
        this.setCenter(coursePreview);
        this.setRight(rightMenu);

        // Track mouseposition
        this.coursePreview.setOnMouseMoved(e -> {
            currentMouseX = e.getX();
            currentMouseY = e.getY();
        });

        this.coursePreview.setOnMouseDragged(e -> {
            currentMouseX = e.getX();
            currentMouseY = e.getY();
        });

        // Start timer if mouse pressed
        this.coursePreview.setOnMousePressed(e -> {
            currentMouseX = e.getX();
            currentMouseY = e.getY();
            isMousePressed = true;
            pressedButton = e.getButton();
            //directly add a hill when buttn pressed
            addHillAtPixel(e.getX(), e.getY(), pressedButton);
        });

        // Stop timer if mouse released
        this.coursePreview.setOnMouseReleased(e -> {
            isMousePressed = false;
        });

        Timeline hillTimer = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            if (isMousePressed) {
                addHillAtPixel(currentMouseX, currentMouseY, pressedButton);
            }
        }));
        hillTimer.setCycleCount(Timeline.INDEFINITE);
        hillTimer.play();

        /* 
        //Mouse tracker for adding hills/valelys
        this.coursePreview.setOnMouseClicked(e -> {
            // Check if the user filled out the configuration parameters first
            double hillHeight = strengthSlider.getValue();
            double hillWidth = widthSlider.getValue();

            try {
                //Left Mouse --> hill, Right Mouse --> valley
                String actionType = "Hill";
                if (e.getButton() == MouseButton.SECONDARY) {
                    hillHeight = -hillHeight; // Turn into a negative value (Valley)
                    actionType = "Valley";
                }

                // Gather layout dimensions for inverse coordinate mapping
                double canvasW = coursePreview.getWidth();
                double canvasH = coursePreview.getHeight();
        
                double[] size = course.getSize(); // {minX, maxX, minY, maxY}
                double minX = size[0];
                double maxX = size[1];
                double minY = size[2];
                double maxY = size[3];

                double gameWidth = maxX - minX;
                double gameHeight = maxY - minY;

                // Calculate aspect ratio uniform scale factors
                double scale = Math.min(canvasW / gameWidth, canvasH / gameHeight);

                double canvasCenterX = canvasW / 2.0;
                double canvasCenterY = canvasH / 2.0;
                double gameCenterX = (minX + maxX) / 2.0;
                double gameCenterY = (minY + maxY) / 2.0;

                //INVERSE COORDINATE TRANSLATION (Pixels -> Meters)
                // Note: The subtraction on mouse Y automatically handles the graphical vertical inversion
                double clickGameX = gameCenterX + (e.getX() - canvasCenterX) / scale;
                double clickGameY = gameCenterY - (e.getY() - canvasCenterY) / scale;

                // Boundary safety check: only inject if click lands within the actual map limits
                if (clickGameX >= minX && clickGameX <= maxX && clickGameY >= minY && clickGameY <= maxY) {
                    course.addHill(clickGameX, clickGameY, hillHeight, hillWidth);
                    coursePreview.updatePreview();
                    if (actionType == "Hill") { 
                        System.out.println("Hill spawned at: (" + clickGameX + ", " + clickGameY + ")");
                    }
                    else {
                        System.out.println("Valley spawned at: (" + clickGameX + ", " + clickGameY + ")");
                    }
                }

            } catch (NumberFormatException ex) {
                System.out.println("Error while adding hills/valleys");
            }
        });
        */
    }

    private void addHillAtPixel(double pixelX, double pixelY, MouseButton button) {
        double hillHeight = strengthSlider.getValue();
        double hillWidth = widthSlider.getValue();
    
        // Gather layout dimensions for inverse coordinate mapping
        double canvasW = coursePreview.getWidth();
        double canvasH = coursePreview.getHeight();
        
        double[] size = course.getSize(); // {minX, maxX, minY, maxY}
        double minX = size[0];
        double maxX = size[1];
        double minY = size[2];
        double maxY = size[3];

        double gameWidth = maxX - minX;
        double gameHeight = maxY - minY;

        // Calculate aspect ratio uniform scale factors
        double scale = Math.min(canvasW / gameWidth, canvasH / gameHeight);

        double canvasCenterX = canvasW / 2.0;
        double canvasCenterY = canvasH / 2.0;
        double gameCenterX = (minX + maxX) / 2.0;
        double gameCenterY = (minY + maxY) / 2.0;

        //INVERSE COORDINATE TRANSLATION (Pixels -> Meters)
        // Note: The subtraction on mouse Y automatically handles the graphical vertical inversion
        double clickGameX = gameCenterX + (pixelX - canvasCenterX) / scale;
        double clickGameY = gameCenterY - (pixelY - canvasCenterY) / scale;

        if (button == MouseButton.SECONDARY) hillHeight = -hillHeight;

        if (clickGameX >= minX && clickGameX <= maxX && clickGameY >= minY && clickGameY <= maxY) {
            course.addHill(clickGameX, clickGameY, hillHeight, hillWidth);
            coursePreview.updatePreview();
        }
    }

    //refresh preview
    public void refreshView() {
        if (coursePreview != null) {
            coursePreview.updatePreview();
        }
    }

    /*
    public HillModificationView() {
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setStyle("-fx-padding: 15;");

        Label title = new Label("Add Artificial Gaussian Hills");
        title.setStyle("-fx-font-weight: bold;");
        
        TextField heightField = new TextField();
        heightField.setPromptText("Hill Height");
        TextField widthField = new TextField();
        widthField.setPromptText("Hill Width (Sigma)");
        
        Button addHillBtn = new Button("Inject Hill");
        
        this.getChildren().addAll(title, new Label("Height:"), heightField, new Label("Width:"), widthField, addHillBtn);
    }
    */
    
}
