package AdvancedGUI.BuilderModules.Tabs;

import GolfCourseData.GolfCourse;
import javafx.scene.layout.BorderPane;
import GolfCourseData.GolfCourse;
import AdvancedGUI.BuilderModules.CoursePreview;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.beans.value.ChangeListener;

public class BaseModificationTab extends BorderPane{

    private CoursePreview coursePreview;
    private TextField terrainFormula;

    public BaseModificationTab(GolfCourse course, double[] preViewSize) {

        this.coursePreview = new CoursePreview(course, preViewSize[0], preViewSize[1]);

        //right side configuration
        VBox rightMenu = new VBox(10);
        rightMenu.setPadding(new Insets(20));
        rightMenu.setAlignment(Pos.TOP_LEFT);
        rightMenu.setPrefWidth(250);
        rightMenu.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Course Dimensions");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField widthXString = new TextField("100");
        TextField widthYString = new TextField("100");

        //TextField terrainFormula = new TextField("(sin(x-y)/7)+0.5");
        terrainFormula = new TextField("(sin(x-y)/7)+0.5");

        rightMenu.getChildren().addAll(
            title, 
            new Label("Width X:"), widthXString, 
            new Label("Width Y:"), widthYString,
            new Label("Enter Height Formula"), terrainFormula
        );

        //Adding listeners for auto ipdating
        ChangeListener<String> sizeUpdateListener = (observable, oldValue, newValue) -> {
            String xText = widthXString.getText();
            String yText = widthYString.getText();

            // Fire helper ONLY if both coordinate blocks are written out
            if (xText != null && !xText.trim().isEmpty() && 
                yText != null && !yText.trim().isEmpty()) {
                updateSize(course, xText, yText);
            }
        };

        // Dedicated Formula Listener: Only runs when the text field expression changes
        ChangeListener<String> formulaUpdateListener = (observable, oldValue, newValue) -> {
            String formula = terrainFormula.getText();

            // Fire helper ONLY if a formula string text exists
            if (formula != null && !formula.trim().isEmpty()) {
                updateFormula(course, formula);
            }
        };

        // Attach the listener to both inputs
        widthXString.textProperty().addListener(sizeUpdateListener);
        widthYString.textProperty().addListener(sizeUpdateListener);

        terrainFormula.textProperty().addListener(formulaUpdateListener);

        //Add to BorderPanel
        this.setCenter(coursePreview);
        this.setRight(rightMenu);
    }
    //refresh preview
    public void refreshView() {
        if (coursePreview != null) {
            coursePreview.updatePreview();
        }
    }

    private void updateSize(GolfCourse course, String xText, String yText){
        try {
            double widthX = Double.parseDouble(xText.trim());
            double widthY = Double.parseDouble(yText.trim());
                
            // Calculate boundaries centered around (0,0)
            double minX = -(widthX / 2.0);
            double maxX = widthX / 2.0;
            double minY = -(widthY / 2.0);
            double maxY = widthY / 2.0;

            // Safely update values inside the course instance
            course.setSize(minX, maxX, minY, maxY); 
            System.out.println("SetSize triggered");

            // Redraw the preview canvas instantly to reflect changes
            coursePreview.updatePreview();

        } catch (NumberFormatException ex) {
            // Fail silently while typing to prevent error floods during backspaces
        }

    }

    private void updateFormula(GolfCourse course, String formula){
        try {
            course.setTerrainFormula(formula);
            System.out.println("SetTerrainFormula triggered");

            coursePreview.updatePreview();
        } catch (NumberFormatException ex) {
            // Fail silently while typing to prevent error floods during backspaces
        }
    }

    public void bindFormulaLock(javafx.beans.property.BooleanProperty perlinEnabled) {
        terrainFormula.disableProperty().bind(perlinEnabled);
    }
}
