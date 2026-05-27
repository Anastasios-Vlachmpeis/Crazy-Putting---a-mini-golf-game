package AdvancedGUI;

import GolfCourseData.GolfCourse;
import AdvancedGUI.BuilderModules.Tabs.BaseModificationView;
import AdvancedGUI.BuilderModules.Tabs.HillModificationView;
import AdvancedGUI.BuilderModules.Tabs.TargetModificationView;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

public class GUI_courseBuilder {
    // Method to build and show the window
    public void display(String title, GolfCourse course) {
        //Create a brand new Stage (Window)
        Stage builderWindow = new Stage();
        double[] preViewSize = {800, 800};
        
        // Block interaction with other windows until this one is closed
        builderWindow.initModality(Modality.APPLICATION_MODAL);
        builderWindow.setTitle(title);
        builderWindow.setMinWidth(1000);
        builderWindow.setMinHeight(800);

        //Configure tabs
        TabPane tabPane = new TabPane();

        // Create tab content views
        BaseModificationView baseView = new BaseModificationView(course, preViewSize);
        HillModificationView hillView = new HillModificationView(course, preViewSize);
        TargetModificationView targetView = new TargetModificationView(course, preViewSize);

        //Set up the Tabs
        Tab baseTab = new Tab("Dimensions", baseView);
        Tab hillTab = new Tab("Hills & Slopes", hillView);
        Tab targetTab = new Tab("Target / Hole", targetView);

        baseTab.setClosable(false);        
        hillTab.setClosable(false);        
        targetTab.setClosable(false);

        tabPane.getTabs().addAll(baseTab, hillTab, targetTab);

        //Listening for preview updates for all tabs
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                // Check which tab content was opened, cast it, and call its refresh method
                if (newTab.getContent() instanceof BaseModificationView) {
                    ((BaseModificationView) newTab.getContent()).refreshView();
                } else if (newTab.getContent() instanceof HillModificationView) {
                    ((HillModificationView) newTab.getContent()).refreshView();
                } else if (newTab.getContent() instanceof TargetModificationView) {
                    ((TargetModificationView) newTab.getContent()).refreshView();
                }
            }
        });


        /* 
        // Create Tab: Base Confuguration tab
        Tab baseTab = new Tab("Base Course Configuration");
        baseTab.setClosable(false);
        // Set its content to an instance of your separate class
        baseTab.setContent(new BaseModificationView(course, preViewSize));

        // Create Tab: Terrain / Hill Modifications
        Tab hillTab = new Tab("Hills & Slopes");
        hillTab.setClosable(false);
        // Set its content to an instance of your separate class
        hillTab.setContent(new HillModificationView(course, preViewSize));

        // Create Tab: Target / Hole Modifications
        Tab targetTab = new Tab("Target / Hole");
        targetTab.setClosable(false);
        // Set its content to an instance of your other separate class
        targetTab.setContent(new TargetModificationView(course, preViewSize));

        tabPane.getTabs().addAll(baseTab, hillTab, targetTab);
        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        */

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        Scene scene = new Scene(root);
        builderWindow.setScene(scene);
        
        // showAndWait() tells Java to wait until this window is closed before moving on
        builderWindow.showAndWait(); 
    }

    
}
