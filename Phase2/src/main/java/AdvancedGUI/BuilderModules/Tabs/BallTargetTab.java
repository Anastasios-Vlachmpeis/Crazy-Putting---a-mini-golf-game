package AdvancedGUI.BuilderModules.Tabs;

import java.util.concurrent.ThreadLocalRandom;

import AdvancedGUI.BuilderModules.CoursePreview;
import GolfCourseData.GolfCourse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class BallTargetTab extends BorderPane {

    private CoursePreview coursePreview;
    private static final int RANDOM_POINT_ATTEMPTS = 100;
    private static final double RANDOM_EDGE_MARGIN_FRACTION = 0.05;

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

        Button randomizeButton = new Button("Random Placement");
        randomizeButton.setMaxWidth(Double.MAX_VALUE);
        randomizeButton.setOnAction(e -> randomizeBallAndTarget(course));
        
        rightMenu.getChildren().addAll(
            title, 
            instructionLabel,
            randomizeButton
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
                course.setTargetPosition(point[0], point[1]);
                
                System.out.printf("Target relocated to: (%.2f, %.2f)%n", point[0], point[1]);
            }

            // Instantly repaint preview to reflect movement adjustments
            coursePreview.updatePreview(); 
        });

        //ASSEMBLE
        this.setCenter(coursePreview);
        this.setRight(rightMenu);
    }

    private void randomizeBallAndTarget(GolfCourse course) {
        double targetRadius = GolfCourse.FIXED_TARGET_RADIUS;
        double[] targetPoint = randomCoursePoint(course, targetRadius);
        double[] ballPoint = randomCoursePoint(course, targetRadius);
        double minimumDistance = Math.max(targetRadius * 4.0, shortestCourseSide(course) * 0.2);

        for (int i = 0; i < RANDOM_POINT_ATTEMPTS && distance(ballPoint, targetPoint) < minimumDistance; i++) {
            ballPoint = randomCoursePoint(course, targetRadius);
        }

        course.setTargetPosition(targetPoint[0], targetPoint[1]);
        course.setBallPosition(ballPoint[0], ballPoint[1]);
        course.setOriginalStartPosition(ballPoint[0], ballPoint[1]);

        System.out.printf(
            "Randomized ball to: (%.2f, %.2f), target to: (%.2f, %.2f)%n",
            ballPoint[0],
            ballPoint[1],
            targetPoint[0],
            targetPoint[1]
        );
        coursePreview.updatePreview();
    }

    private double[] randomCoursePoint(GolfCourse course, double targetRadius) {
        for (int i = 0; i < RANDOM_POINT_ATTEMPTS; i++) {
            double[] point = randomPointInsideBounds(course, targetRadius);
            if (!course.isWater(point[0], point[1])) {
                return point;
            }
        }
        return randomPointInsideBounds(course, targetRadius);
    }

    private double[] randomPointInsideBounds(GolfCourse course, double targetRadius) {
        double[] size = course.getSize(); // {minX, maxX, minY, maxY}
        double width = size[1] - size[0];
        double height = size[3] - size[2];
        double edgeMargin = Math.max(targetRadius, Math.min(width, height) * RANDOM_EDGE_MARGIN_FRACTION);

        double minX = size[0] + edgeMargin;
        double maxX = size[1] - edgeMargin;
        double minY = size[2] + edgeMargin;
        double maxY = size[3] - edgeMargin;

        if (minX > maxX) {
            minX = maxX = (size[0] + size[1]) / 2.0;
        }
        if (minY > maxY) {
            minY = maxY = (size[2] + size[3]) / 2.0;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new double[] {
            minX == maxX ? minX : random.nextDouble(minX, maxX),
            minY == maxY ? minY : random.nextDouble(minY, maxY)
        };
    }

    private double shortestCourseSide(GolfCourse course) {
        double[] size = course.getSize();
        return Math.min(size[1] - size[0], size[3] - size[2]);
    }

    private double distance(double[] first, double[] second) {
        return Math.hypot(first[0] - second[0], first[1] - second[1]);
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
