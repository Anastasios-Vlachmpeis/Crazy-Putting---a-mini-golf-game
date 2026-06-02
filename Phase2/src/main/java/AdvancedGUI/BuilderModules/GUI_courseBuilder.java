package AdvancedGUI.BuilderModules;

import GolfCourseData.GolfCourse;
import AdvancedGUI.BuilderModules.Tabs.BaseModificationTab;
import AdvancedGUI.BuilderModules.Tabs.HillModificationTab;
import AdvancedGUI.BuilderModules.Tabs.SaveLoadPresetsTab;
import AdvancedGUI.BuilderModules.Tabs.BallTargetTab;
import AdvancedGUI.BuilderModules.Tabs.PerlinNoiseTab;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GUI_courseBuilder {
    // Method to build and show the window
    public void display(String title, GolfCourse course) {
        //Create a brand new Stage (Window)
        Stage builderWindow = new Stage();
        double[] preViewSize = {1000, 1000};
        
        // Block interaction with other windows until this one is closed
        builderWindow.initModality(Modality.APPLICATION_MODAL);
        builderWindow.setTitle(title);
        builderWindow.setMinWidth(1000); 
        builderWindow.setMinHeight(800);
        builderWindow.setMaximized(true);

        //Configure tabs
        TabPane tabPane = new TabPane();

        // Create tab content views
        PerlinNoiseTab perlinView = new PerlinNoiseTab(course, preViewSize);
        SaveLoadPresetsTab saveLoadView = new SaveLoadPresetsTab(course, preViewSize);
        BaseModificationTab baseView = new BaseModificationTab(course, preViewSize);
        HillModificationTab hillView = new HillModificationTab(course, preViewSize);
        BallTargetTab targetView = new BallTargetTab(course, preViewSize);

        //Set up the Tabs
        Tab perlinTab = new Tab("Procedural Gen", perlinView);
        Tab saveLoadTab = new Tab("Save & Load", saveLoadView);
        Tab baseTab = new Tab("Dimensions & Height Function", baseView);
        Tab hillTab = new Tab("Hills & Valleys", hillView);
        Tab targetTab = new Tab("Target & Hole", targetView);

        perlinTab.setClosable(false);
        saveLoadTab.setClosable(false); 
        baseTab.setClosable(false);        
        hillTab.setClosable(false);        
        targetTab.setClosable(false);

        //Disable certain tabs when perlin is active
        saveLoadTab.disableProperty().bind(perlinView.perlinEnabledProperty());
        hillTab.disableProperty().bind(perlinView.perlinEnabledProperty());
        //Disable only certain features if Perlin is active
        baseView.bindFormulaLock(perlinView.perlinEnabledProperty());

        tabPane.getTabs().addAll(perlinTab, saveLoadTab, baseTab, hillTab, targetTab);

        //Listening for preview updates for all tabs
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                // Check which tab content was opened, cast it, and call its refresh method
                if (newTab.getContent() instanceof PerlinNoiseTab) {
                    ((PerlinNoiseTab) newTab.getContent()).refreshView();
                } else if (newTab.getContent() instanceof SaveLoadPresetsTab) {
                    ((SaveLoadPresetsTab) newTab.getContent()).refreshView();
                } else if (newTab.getContent() instanceof BaseModificationTab) {
                    ((BaseModificationTab) newTab.getContent()).refreshView();
                } else if (newTab.getContent() instanceof HillModificationTab) {
                    ((HillModificationTab) newTab.getContent()).refreshView();
                } else if (newTab.getContent() instanceof BallTargetTab) {
                    ((BallTargetTab) newTab.getContent()).refreshView();
                }
            }
        });

        Image backgroundImage = new Image("file:src\\main\\java\\AdvancedGUI\\LauncherModules\\Background.png");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setPreserveRatio(false);

        backgroundView.fitWidthProperty().bind(root.widthProperty());
        backgroundView.fitHeightProperty().bind(root.heightProperty());

        tabPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.0);");
        tabPane.setStyle("-fx-tab-min-width: 100px;");
        root.getChildren().addAll(backgroundView, tabPane);

        //BorderPane root = new BorderPane();
        //root.setCenter(tabPane);
        Scene scene = new Scene(root);
        builderWindow.setScene(scene);
        
        // showAndWait() tells Java to wait until this window is closed before moving on
        builderWindow.showAndWait(); 
    }

    
}
