package AdvancedGUI.BuilderModules.Tabs;

import java.util.Locale;

import AdvancedGUI.BuilderModules.CoursePreview;
import GolfCourseData.GolfCourse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class BallTargetTab extends BorderPane {

    private CoursePreview coursePreview;

    public BallTargetTab(GolfCourse course, double[] preViewSize) {
        
        // LEFT SIDE: Preview
        //Canvas previewCanvas = new Canvas(400, 400);
        this.coursePreview = new CoursePreview(course, preViewSize[0], preViewSize[1]);

        //RIGHT SIDE: Controls
        VBox rightMenu = new VBox(15);
        rightMenu.setPadding(new Insets(20));
        rightMenu.setAlignment(Pos.TOP_LEFT);
        rightMenu.setPrefWidth(250);
        rightMenu.setStyle("-fx-background-color: rgba(244, 244, 244, 0.9); -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Relocate Target & Ball");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label instructionLabel = new Label("• Left-Click: Place Ball\n• Right-Click: Place Target Hole");
        //instructionLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
        /* 
        TextField xField = new TextField();
        xField.setPromptText("Target X");
        
        TextField yField = new TextField();
        yField.setPromptText("Target Y");
        
        Button moveBtn = new Button("Update Location");
        moveBtn.setMaxWidth(Double.MAX_VALUE);
        */

        //TextField rTarget = new TextField();
        //rTarget.setPromptText("Set target radius");

        Slider rTarget = new Slider(0.1, 0.5, 0.2);
        rTarget.setShowTickLabels(true);
        rTarget.setShowTickMarks(true);
        rTarget.setMajorTickUnit(0.1);

        rTarget.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Get the current target location so we don't accidentally move it
            double[] currentTarget = course.getTargetXYR(); 
            
            // Update the course with the same X and Y, but the new Radius
            course.setTargetXYR(currentTarget[0], currentTarget[1], newVal.doubleValue());
            
            // Instantly redraw the canvas!
            coursePreview.updatePreview();
        });
        
        rightMenu.getChildren().addAll(
            title, 
            instructionLabel,
            rTarget
        );

        this.coursePreview.setOnMouseClicked(e -> {
            double[] point = coursePreview.pixelToCoursePoint(e.getX(), e.getY());
            if (point == null) {
                return;
            }

            // Verify click falls within boundaries before applying
            if (e.getButton() == MouseButton.PRIMARY) {
                // LEFT CLICK: Relocate the ball launch start position
                course.setBallPosition(point[0], point[1]);
                course.setOriginalStartPosition(point[0], point[1]);
                System.out.printf("Ball relocated to: (%.2f, %.2f)%n", point[0], point[1]);
            } 
            else if (e.getButton() == MouseButton.SECONDARY) {
                // RIGHT CLICK: Relocate target array indices directly
                course.setTargetXYR(point[0], point[1], rTarget.getValue());
                
                System.out.printf("Target relocated to: (%.2f, %.2f)%n", point[0], point[1]);
            }

            // Instantly repaint preview to reflect movement adjustments
            coursePreview.updatePreview(); 
        });

        //ASSEMBLE
        this.setCenter(coursePreview);
        this.setRight(rightMenu);
    }

    //refresh preview
    public void refreshView() {
        if (coursePreview != null) {
            coursePreview.updatePreview();
        }
    }


    /* 
    public TargetModificationView() {
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setStyle("-fx-padding: 15;");

        Label title = new Label("Relocate the Hole / Target");
        title.setStyle("-fx-font-weight: bold;");
        
        TextField xField = new TextField();
        xField.setPromptText("Target X Coordinate");
        TextField yField = new TextField();
        yField.setPromptText("Target Y Coordinate");
        
        Button moveTargetBtn = new Button("Update Target Location");
        
        this.getChildren().addAll(title, new Label("X Position:"), xField, new Label("Y Position:"), yField, moveTargetBtn);
    }
    */
}
