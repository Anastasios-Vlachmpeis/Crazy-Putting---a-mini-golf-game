package AdvancedGUI.BuilderModules.Tabs;

import AdvancedGUI.BuilderModules.CoursePreview;
import GolfCourseData.GolfCourse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class TargetModificationView extends BorderPane {

    private CoursePreview coursePreview;

    public TargetModificationView(GolfCourse course, double[] preViewSize) {
        
        // LEFT SIDE: Preview
        //Canvas previewCanvas = new Canvas(400, 400);
        this.coursePreview = new CoursePreview(course, preViewSize[0], preViewSize[1]);

        //RIGHT SIDE: Controls
        VBox rightMenu = new VBox(15);
        rightMenu.setPadding(new Insets(20));
        rightMenu.setAlignment(Pos.TOP_LEFT);
        rightMenu.setPrefWidth(250);
        rightMenu.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Relocate Target");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        TextField xField = new TextField();
        xField.setPromptText("Target X");
        
        TextField yField = new TextField();
        yField.setPromptText("Target Y");
        
        Button moveBtn = new Button("Update Location");
        moveBtn.setMaxWidth(Double.MAX_VALUE);
        
        rightMenu.getChildren().addAll(
            title, 
            new Label("X Position:"), xField, 
            new Label("Y Position:"), yField, 
            moveBtn
        );

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
