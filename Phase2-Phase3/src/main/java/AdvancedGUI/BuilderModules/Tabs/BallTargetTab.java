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
        rightMenu.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Relocate Target & Ball");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label instructionLabel = new Label("• Left-Click: Place Ball\n• Right-Click: Place Target Hole");
        instructionLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
        /* 
        TextField xField = new TextField();
        xField.setPromptText("Target X");
        
        TextField yField = new TextField();
        yField.setPromptText("Target Y");
        
        Button moveBtn = new Button("Update Location");
        moveBtn.setMaxWidth(Double.MAX_VALUE);
        */
        
        rightMenu.getChildren().addAll(
            title, 
            instructionLabel
        );

        this.coursePreview.setOnMouseClicked(e -> {
            double canvasW = coursePreview.getWidth();
            double canvasH = coursePreview.getHeight();
            
            double[] size = course.getSize(); //
            double minX = size[0]; //
            double maxX = size[1]; //
            double minY = size[2]; //
            double maxY = size[3]; //

            double gameWidth = maxX - minX;
            double gameHeight = maxY - minY;

            if (gameWidth <= 0 || gameHeight <= 0) {
                gameWidth = 40.0; gameHeight = 40.0;
                minX = -20.0; maxX = 20.0;
                minY = -20.0; maxY = 20.0;
            }

            double scale = Math.min(canvasW / gameWidth, canvasH / gameHeight);
            
            double canvasCenterX = canvasW / 2.0;
            double canvasCenterY = canvasH / 2.0;
            double gameCenterX = (minX + maxX) / 2.0;
            double gameCenterY = (minY + maxY) / 2.0;

            // Translate screen pixels back to map space meters
            double clickGameX = gameCenterX + (e.getX() - canvasCenterX) / scale;
            double clickGameY = gameCenterY - (e.getY() - canvasCenterY) / scale;

            // Verify click falls within boundaries before applying
            if (clickGameX >= minX && clickGameX <= maxX && clickGameY >= minY && clickGameY <= maxY) {
                
                if (e.getButton() == MouseButton.PRIMARY) {
                    // LEFT CLICK: Relocate the ball launch start position
                    course.setBallPosition(clickGameX, clickGameY); //
                    System.out.printf("Ball relocated to: (%.2f, %.2f)%n", clickGameX, clickGameY);
                } 
                else if (e.getButton() == MouseButton.SECONDARY) {
                    // RIGHT CLICK: Relocate target array indices directly
                    double[] targets = course.getTargetXYR(); //
                    targets[0] = clickGameX;
                    targets[1] = clickGameY;
                    
                    System.out.printf("Target relocated to: (%.2f, %.2f)%n", clickGameX, clickGameY);
                }

                // Instantly repaint preview to reflect movement adjustments
                coursePreview.updatePreview(); 
            }
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
