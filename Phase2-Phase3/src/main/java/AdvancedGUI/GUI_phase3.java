package AdvancedGUI;

import GolfCourseData.GolfCourse;
import javafx.application.Application;
import javafx.stage.Stage;
import AdvancedGUI.MainGUIModules.MainGameContainer;
import GameEngine.GameState;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import GameEngine.GameManager;
import Solvers.Solver;
import Solvers.RungeKuttaSolver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;


public class GUI_phase3 extends Application {
    //Main GUI phase 3
    @Override
    public void start(Stage GameStage) {
        // Instantiate builder window
        GUI_courseBuilder builderWindow = new GUI_courseBuilder();
        GolfCourse course = new GolfCourse();
        Solver solver = new RungeKuttaSolver();

        GameManager gameManager = new GameManager(course, solver);
        MainGameContainer gamePlayRoot = new MainGameContainer(gameManager);

        Button builderButton = new Button("Open Builder");
        //Made the button more beautiful :)
        builderButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
        builderButton.setOnAction(e -> builderWindow.display("BuilderWindow", course));
        //The main GUI is locked when builder window is open so no need to worry about crashes

        StackPane.setAlignment(builderButton, Pos.TOP_RIGHT);
        StackPane.setMargin(builderButton, new Insets(25));
        gamePlayRoot.getChildren().add(builderButton);

        /* 
        // Create a layout and add the components
        VBox layout = new VBox(15);
        layout.getChildren().addAll(builderButton);
        layout.setAlignment(Pos.CENTER);
        */

        Stage mainGameGUI = new Stage();
        Scene scene = new Scene(gamePlayRoot, 1000, 800);
        mainGameGUI.setTitle("Crazy Putting!");
        mainGameGUI.setMinWidth(600);
        mainGameGUI.setMinHeight(400);
        mainGameGUI.setScene(scene);
        mainGameGUI.show();
    }


        ///NOTE FOR IMPLEMENTATION OF GUI / ask stan if you have questions

        // this returns each frame to visualize 
        // there are 100 frames per second in here, so make sure to show them at the correct speed
        // 100 frames is a lot to render -> maybe render every 5 frames (20fps)
        // returns null if invalid (EG game over or game won or max shots reached)
        //
        // double[][] rawTrajectoryData = gameManager.hitBall(vx, vy);


        //Label strokeLabel = new Label();
        //Label stateLabel = new Label();
        //Label scoreLabel = new Label("Strokes: 0");
        // 
        //strokeLabel.textProperty().bind(gameManager.strokeCountProperty().asString("Strokes: %d"));
        //stateLabel.textProperty().bind(gameManager.currentStateProperty().asString("Status: %s"));
        //scoreLabel.textProperty().bind(gameManager.strokeCountProperty().asString("Strokes: %d"));
        //
        //These labels are auto updated so no need to add manual refreshing code :)


        //This resets the game
        //resetGame()


        //This locks the shooting button when the ball is still rolling -> make sure the shoot button is "shootButton" named
        //shootButton.disableProperty().bind(gameManager.currentStateProperty().isEqualTo(GameState.ROLLING));

        //Add this listener, and the game should automatically trigger:
        //  "showVictoryCelebrationPopup()" or "showDefeatScreenOverlay()"
        //
        /*  gameManager.currentStateProperty().addListener((obs, oldState, newState) -> {
                if (newState == GameState.HOLED_OUT) {
                    showVictoryCelebrationPopup();
                } else if (newState == GameState.GAME_OVER) {
                    showDefeatScreenOverlay();
                }
            });
        */


    // Add a standard main method to launch the application
    //change in pom.xml <mainClass>SimpleGUI.GUI_phase2</mainClass> to <mainClass>AdvancedGUI.GUI_phase3</mainClass>
    public static void main(String[] args) {
        launch(args);
    }
    
}
