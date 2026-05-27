package AdvancedGUI.BuilderModules.Tabs;

import AdvancedGUI.BuilderModules.CoursePreview;
import GolfCourseData.GolfCourse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class HillModificationView extends BorderPane{

    private CoursePreview coursePreview;

    public HillModificationView(GolfCourse course, double[] preViewSize) {
        //gamePreview
        //Canvas previewCanvas = new Canvas(400, 400);
        //CoursePreview(previewCanvas, course); // Helper method to draw the map
        //canvas size
        this.coursePreview = new CoursePreview(course, preViewSize[0], preViewSize[1]);

        //Right side configurator
        VBox rightMenu = new VBox(15);
        rightMenu.setPadding(new Insets(20));
        rightMenu.setAlignment(Pos.TOP_LEFT);
        rightMenu.setPrefWidth(250);
        rightMenu.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Add Gaussian Hills");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        TextField heightField = new TextField();
        heightField.setPromptText("Hill Height (e.g., 2.5)");
        
        TextField widthField = new TextField();
        widthField.setPromptText("Hill Width / Sigma");
        
        Button addHillBtn = new Button("Inject Hill");
        addHillBtn.setMaxWidth(Double.MAX_VALUE); // Makes button stretch to fill width
        
        rightMenu.getChildren().addAll(
            title, 
            new Label("Height:"), heightField, 
            new Label("Width:"), widthField, 
            addHillBtn
        );

        //combine BorderPane
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
