package app;

import domain.course.GolfCourse;
import javafx.application.Application;
import javafx.stage.Stage;
import ui.builder.GUI_courseBuilder;
import ui.game.GUI_Game;
import bots.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import engine.GameManager;
import solvers.Solver;
import solvers.RungeKuttaSolver;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class Launcher extends Application{

    private GUI_courseBuilder builder;
    private Settings settings;
    private GUI_Game game;

    @Override
    public void start(Stage LauncherStage){
        //Created stackpane for layering
        StackPane root = new StackPane();
        root.setStyle(
            "-fx-background-image: url('file:src/main/resources/images/Background.png');" +
            "-fx-background-size: cover;" +
            "-fx-background-position: center center;" +
            "-fx-background-repeat: no-repeat;"
        );

        GolfCourse course = new GolfCourse();
        loadDefaultCourse(course);
        Solver solver = new RungeKuttaSolver();
        GameManager gameManager = new GameManager(course, solver);

        this.builder = new GUI_courseBuilder(course);
        this.settings = new Settings(course);
        this.game = new GUI_Game(course, gameManager);

        //Add Big launcher title
        Label gameName = new Label("Crazy Putting!");
        gameName.setPadding(new Insets(80, 10, 100, 10));
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
            String chosenBotName = settings.getSelectedBot();
            GolfBot bot = createBot(chosenBotName, course, solver);
            gameManager.setMultiplayerMode(true, bot);
            game.show(true, bot); 
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
        /* 
        Image backgroundImage = new Image("file:src/main/resources/images/Background.png");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(root.widthProperty());
        backgroundView.fitHeightProperty().bind(root.heightProperty());
        */

        root.getChildren().addAll(
            //backgroundView,
            layout,
            settings.getContainer(),
            builder.getContainer(),
            game.getContainer()
        );

        settings.hide();
        builder.hide();
        game.hide();
        
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        StackPane.setAlignment(layout, Pos.TOP_CENTER);
        StackPane.setMargin(layout, new Insets(0, 0, 250, 0));

        // Show stage
        LauncherStage.setTitle("Crazy Putting!");
        LauncherStage.setMinWidth(900);
        LauncherStage.setMinHeight(650);
        LauncherStage.setScene(scene);
        LauncherStage.setMaximized(true);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.F11) {
                LauncherStage.setFullScreen(!LauncherStage.isFullScreen());
            }
        });

        LauncherStage.show();
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

    private GolfBot createBot(String botName, GolfCourse course, Solver solver) {
    return switch (botName) {
        case "ML Bot" -> new MLBot(course, solver);
        case "Hill Bot" -> new HillBot(course, solver);
        case "ManHattan Bot" -> new ManhattanBot(course, solver);
        case "Newton Bot" -> new NewtonBot(course, solver);
        case "Simple Bot" -> new SimpleBot(course, solver);
        default -> new SimpleBot(course, solver);
    };
}

    private void loadDefaultCourse(GolfCourse course) {
        try {
            course.loadFromJsonResource("/Presets/Phase3Format/StartingCourse25x25.json");
        } catch (Exception exception) {
            System.err.println("Could not load default course preset. Falling back to built-in course: " + exception.getMessage());
        }
    }
}
