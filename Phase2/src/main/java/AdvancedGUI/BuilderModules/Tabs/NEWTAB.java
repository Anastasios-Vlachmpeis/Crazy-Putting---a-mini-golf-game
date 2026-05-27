package AdvancedGUI.BuilderModules.Tabs;
/* 

//EXAMPLE TAB

import AdvancedGUI.BuilderModules.CoursePreview;
import GolfCourseData.GolfCourse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class NEWTAB extends BorderPane{

    private CoursePreview coursePreview;

    public BaseModificationView(GolfCourse course, double[] preViewSize) {

        this.coursePreview = new CoursePreview(course, preViewSize[0], preViewSize[1]);

        VBox rightMenu = new VBox(10);
        rightMenu.setPadding(new Insets(20));
        rightMenu.setAlignment(Pos.TOP_LEFT);
        rightMenu.setPrefWidth(250);
        rightMenu.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("LABEL OF SETTINGS");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        rightMenu.getChildren().addAll(
            title
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
}
*/
