package AdvancedGUI.LauncherModules;

import GolfCourseData.GolfCourse;
import javafx.application.Application;
import javafx.stage.Stage;
import AdvancedGUI.BuilderModules.GUI_courseBuilder;
import AdvancedGUI.GameModules.GUI_Game;
import AdvancedGUI.LauncherModules.SettingsModules.Settings;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import GameEngine.GameManager;
import Solvers.Solver;
import Solvers.RungeKuttaSolver;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;

public class Launcher extends Application{
    @Override
    public void start(Stage LauncherStage){
        //Created stackpane for layering
        StackPane root = new StackPane();

        GUI_courseBuilder builderWindow = new GUI_courseBuilder();
        GUI_Game gameWindow = new GUI_Game();
        Settings settings = new Settings();
        
        GolfCourse course = new GolfCourse();
        Solver solver = new RungeKuttaSolver();
        GameManager gameManager = new GameManager(course, solver);

        //Add Big launcher title
        Label gameName = new Label("Crazy Putting!");
        gameName.setPadding(new Insets(15, 10, 50, 10));
        gameName.setFont(Font.font("Arial", FontWeight.BOLD, 150));
        gameName.setStyle("-fx-text-fill: #FFFFFF;");
        gameName.setEffect(new DropShadow(10, Color.BLACK));

        //Add options buttons
        Button singlePlayerButton = new Button("Singleplayer");
        styleGameButton(singlePlayerButton);
        singlePlayerButton.setOnAction(e -> gameWindow.display("Crazy Putting! Singleplayer", course, gameManager));

        Button multiPlayerButton = new Button("Multiplayer");
        styleGameButton(multiPlayerButton);
        multiPlayerButton.setOnAction(e -> gameWindow.display("Crazy Putting! Multiplayer", course, gameManager));

        Button builderButton = new Button("Open Builder");
        styleGameButton(builderButton);
        builderButton.setOnAction(e -> builderWindow.display("Builder", course));

        Button settingsButton = new Button("Open settings");
        styleGameButton(settingsButton);
        settingsButton.setOnAction(e -> settings.display("Settings", course));

        //make layout
        VBox layout = new VBox(15);
        layout.getChildren().addAll(gameName, singlePlayerButton, multiPlayerButton, builderButton, settingsButton);
        layout.setAlignment(Pos.CENTER);

        //Add background to scene
        Image backgroundImage = new Image("file:src\\main\\java\\AdvancedGUI\\LauncherModules\\Background.png");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(root.widthProperty());
        backgroundView.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().add(backgroundView);
        root.getChildren().add(layout);

        Scene scene = new Scene(root, 1000, 800);
        StackPane.setAlignment(layout, Pos.TOP_CENTER);
        StackPane.setMargin(layout, new Insets(00, 0, 250, 0));

        //Show stage
        Stage launcher = new Stage();
        launcher.setTitle("Crazy Putting!");
        launcher.setMinWidth(600);
        launcher.setMinHeight(400);
        launcher.setMaximized(true);
        launcher.setScene(scene);
        launcher.show();
    }



    //Styling all buttons at once
    private void styleGameButton(Button button) {
        // Default Style
        String idleStyle = 
        "-fx-min-width: 400px;" +
        "-fx-min-height: 80px;" +
        "-fx-background-color: #f39c12;" +
        "-fx-text-fill: white;" +
        "-fx-font-weight: bold;"+ 
        "-fx-font-size: 25px;" +
        "-fx-padding: 15 20 15 20;"+ 
        "-fx-background-radius: 25;";
    
        // Hover Style
        String hoverStyle = 
        "-fx-min-width: 420px;" + // Grow slightly wider if hovered
        "-fx-min-height: 80px;" +
        "-fx-background-color: #e67e22;"+ 
        "-fx-text-fill: white;" +
        "-fx-font-weight: bold;" + 
        "-fx-font-size: 28px; " + //Grow text if hovering over the button
        "-fx-padding: 10 20 10 20;" + 
        "-fx-background-radius: 25;";

        button.setStyle(idleStyle);

        // Add Hover Listener
        button.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
            if (isNowHovered) {
                button.setStyle(hoverStyle);
            } else {
                button.setStyle(idleStyle);
            }
        });
    }
}
