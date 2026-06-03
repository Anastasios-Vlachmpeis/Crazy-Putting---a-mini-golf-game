package AdvancedGUI.LauncherModules;

import GolfCourseData.GolfCourse;
import javafx.application.Application;
import javafx.stage.Stage;
import AdvancedGUI.BuilderModules.GUI_courseBuilder;
import AdvancedGUI.GameModules.GUI_Game;
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

    private GUI_courseBuilder builder;
    private Settings settings;
    private GUI_Game game;

    @Override
    public void start(Stage LauncherStage){
        //Created stackpane for layering
        StackPane root = new StackPane();

        GolfCourse course = new GolfCourse();
        Solver solver = new RungeKuttaSolver();
        GameManager gameManager = new GameManager(course, solver);

        this.builder = new GUI_courseBuilder(course);
        this.settings = new Settings(course);
        this.game = new GUI_Game(course, gameManager);

        //Add Big launcher title
        Label gameName = new Label("Crazy Putting!");
        gameName.setPadding(new Insets(100, 10, 100, 10));
        gameName.setFont(Font.font("Arial", FontWeight.BOLD, 150));
        gameName.setStyle("-fx-text-fill: #FFFFFF;");
        gameName.setEffect(new DropShadow(10, Color.BLACK));

        //Add options buttons
        Button singlePlayerButton = new Button("Singleplayer");
        styleGameButton(singlePlayerButton);
        singlePlayerButton.setOnAction(e -> game.show(false, null));

        Button multiPlayerButton = new Button("Multiplayer");
        styleGameButton(multiPlayerButton);
        multiPlayerButton.setOnAction(e -> {
            String chosenBot = settings.getSelectedBot();
            game.show(true, chosenBot);
        });

        Button builderButton = new Button("Open Builder");
        styleGameButton(builderButton);
        builderButton.setOnAction(e -> {builder.show(); });

        Button settingsButton = new Button("Open Settings");
        styleGameButton(settingsButton);
        settingsButton.setOnAction(e -> settings.show());

        Button quitButton = new Button("Quit Game");
        styleGameButton(quitButton);
        quitButton.setOnAction(e -> javafx.application.Platform.exit());

        //make layout
        VBox layout = new VBox(15);
        layout.getChildren().addAll(gameName, singlePlayerButton, multiPlayerButton, builderButton, settingsButton, quitButton);
        layout.setAlignment(Pos.CENTER);

        //Add background to scene
        Image backgroundImage = new Image("file:src\\main\\java\\AdvancedGUI\\LauncherModules\\Background.png");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(root.widthProperty());
        backgroundView.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().addAll(
            backgroundView,
            layout,
            settings.getContainer(),
            builder.getContainer(),
            game.getContainer()
        );

        settings.hide();
        builder.hide();
        game.hide();
        
        Scene scene = new Scene(root, 1000, 800);
        StackPane.setAlignment(layout, Pos.TOP_CENTER);
        StackPane.setMargin(layout, new Insets(00, 0, 250, 0));

        //Show stage
        Stage launcher = new Stage();
        launcher.setTitle("Crazy Putting!");
        launcher.setMinWidth(1850);
        launcher.setMinHeight(1000);
        launcher.setMaximized(true);
        launcher.setFullScreen(true);
        //launcher.setResizable(false);
        launcher.setScene(scene);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.F11) {
                // Toggle de fullscreen status: zet hem op het tegenovergestelde van wat het nu is
                launcher.setFullScreen(!launcher.isFullScreen());
            }
        });

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
        "-fx-min-width: 450px;" + // Grow slightly wider if hovered
        "-fx-min-height: 80px;" +
        "-fx-background-color: #e67e22;"+ 
        "-fx-text-fill: white;" +
        "-fx-font-weight: bold;" + 
        "-fx-font-size: 35px; " + //Grow text if hovering over the button
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
