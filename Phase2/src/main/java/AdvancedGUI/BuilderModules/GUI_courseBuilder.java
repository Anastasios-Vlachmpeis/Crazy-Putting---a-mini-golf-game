package AdvancedGUI.BuilderModules;

import GolfCourseData.GolfCourse;
import AdvancedGUI.BuilderModules.Tabs.BaseModificationTab;
import AdvancedGUI.BuilderModules.Tabs.HillModificationTab;
import AdvancedGUI.BuilderModules.Tabs.SaveLoadPresetsTab;
import AdvancedGUI.BuilderModules.Tabs.BallTargetTab;
import AdvancedGUI.BuilderModules.Tabs.PerlinNoiseTab;
import AdvancedGUI.BuilderModules.Tabs.ObstacleModificationTab;
import javafx.application.Application;
import javafx.geometry.Insets;
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

    private StackPane builderContainer;
    private TabPane tabPane;

    public StackPane getContainer() {
        return builderContainer;
    }

    public void show() {
        builderContainer.setVisible(true);
    }

    public void hide() {
        builderContainer.setVisible(false);
    }

    //We create the window before we actually open it
    // Method to build and show the window
    public GUI_courseBuilder(GolfCourse course) {
        this.builderContainer = new StackPane();
        this.builderContainer.setStyle(
            "-fx-background-image: url('file:src/main/java/AdvancedGUI/LauncherModules/Background.png');" +
            "-fx-background-size: cover;" +
            "-fx-background-position: center center;" +
            "-fx-background-repeat: no-repeat;"
        );
        
        double[] preViewSize = {900, 900};

        //Configure tabs
        tabPane = new TabPane();

        // Create tab content views
        PerlinNoiseTab perlinView = new PerlinNoiseTab(course, preViewSize);
        SaveLoadPresetsTab saveLoadView = new SaveLoadPresetsTab(course, preViewSize);
        BaseModificationTab baseView = new BaseModificationTab(course, preViewSize);
        HillModificationTab hillView = new HillModificationTab(course, preViewSize);
        ObstacleModificationTab obstacleView = new ObstacleModificationTab(course, preViewSize);
        BallTargetTab targetView = new BallTargetTab(course, preViewSize);

        //Set up the Tabs
        Tab perlinTab = new Tab();
        setupCustomTab(perlinTab, "Procedural Generation");
        perlinTab.setContent(perlinView);

        Tab saveLoadTab = new Tab();
        setupCustomTab(saveLoadTab, "Save & Load");
        saveLoadTab.setContent(saveLoadView);

        Tab baseTab = new Tab();
        setupCustomTab(baseTab, "Dimensions & Function");
        baseTab.setContent(baseView);

        Tab hillTab = new Tab();
        setupCustomTab(hillTab, "Hills & Valleys");
        hillTab.setContent(hillView);

        Tab obstacleTab = new Tab();
        setupCustomTab(obstacleTab, "Obstacles");
        obstacleTab.setContent(obstacleView);

        Tab targetTab = new Tab();
        setupCustomTab(targetTab, "Target & Hole");
        targetTab.setContent(targetView);

        tabPane.getTabs().addAll(perlinTab, saveLoadTab, baseTab, hillTab, obstacleTab, targetTab);
        tabPane.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-padding: 0; " +
            "-fx-tab-min-width: 200px; " +
            "-fx-tab-max-width: 200px; " +
            "-fx-tab-min-height: 40px;" 
        );

        perlinTab.setClosable(false);
        saveLoadTab.setClosable(false); 
        baseTab.setClosable(false);        
        hillTab.setClosable(false);        
        obstacleTab.setClosable(false);
        targetTab.setClosable(false);

        //Disable certain tabs when perlin is active
        saveLoadTab.disableProperty().bind(perlinView.perlinEnabledProperty());
        hillTab.disableProperty().bind(perlinView.perlinEnabledProperty());
        //Disable only certain features if Perlin is active
        baseView.bindFormulaLock(perlinView.perlinEnabledProperty());

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
                } else if (newTab.getContent() instanceof ObstacleModificationTab) {
                    ((ObstacleModificationTab) newTab.getContent()).refreshView();
                } else if (newTab.getContent() instanceof BallTargetTab) {
                    ((BallTargetTab) newTab.getContent()).refreshView();
                }
            }
        });
        /* 
        Image backgroundImage = new Image("file:src\\main\\java\\AdvancedGUI\\LauncherModules\\Background.png");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(builderContainer.widthProperty());
        backgroundView.fitHeightProperty().bind(builderContainer.heightProperty());
        */

        Button closeButton = new Button("Close Builder");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        closeButton.setOnAction(e -> this.hide());
        StackPane.setAlignment(closeButton, Pos.TOP_LEFT);
        StackPane.setMargin(closeButton, new Insets(40, 0, 0, 10));

        builderContainer.getChildren().addAll(/*backgroundView,*/ tabPane, closeButton);
    }

    private void setupCustomTab(Tab tab, String title) {
        Label tabLabel = new Label(title);

        // Define the inline styles as strings
        String idleStyle = 
            "-fx-background-color: rgba(255, 255, 255, 0.75);" +
            "-fx-text-fill: #333333;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-min-width: 250px;" +       
            "-fx-alignment: center;" +
            "-fx-background-radius: 15 15 0 0;";

        String hoverStyle = 
            "-fx-background-color: #e67e22;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-min-width: 250px;" +       
            "-fx-alignment: center;" +
            "-fx-background-radius: 15 15 0 0;";

        String selectedStyle = 
            "-fx-background-color: #f39c12;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-min-width: 250px;" +       
            "-fx-alignment: center;" +
            "-fx-background-radius: 15 15 0 0;";

        // Set initial state
        tabLabel.setStyle(idleStyle);
    
        // Replace the default tab text with our custom interactive Label
        tab.setGraphic(tabLabel);
        tab.setText(""); 

        // Java-based Hover Logic
        tabLabel.hoverProperty().addListener((obs, wasHover, isHover) -> {
            // Only apply hover effect if the tab is NOT currently selected
            if (!tab.isSelected()) {
                tabLabel.setStyle(isHover ? hoverStyle : idleStyle);
            }
        });

        // Java-based Selection Logic
        tab.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            tabLabel.setStyle(isSelected ? selectedStyle : idleStyle);
        });
    }
}
