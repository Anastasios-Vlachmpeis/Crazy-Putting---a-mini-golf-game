package AdvancedGUI.GameModules;

import GolfCourseData.GolfCourse;
import AdvancedGUI.GameModules.Scene.MainGameContainer;
import GameEngine.GameManager;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.geometry.Insets;


public class GUI_Game{
    //Main GUI phase 3
    private StackPane gameContainer;
    private MainGameContainer mainGameContainer;

    public StackPane getContainer() {
        return gameContainer;
    }

    public void show() {
        gameContainer.setVisible(true);
        mainGameContainer.refreshCourseFromBuilder();
    }

    public void hide() {
        gameContainer.setVisible(false);
    }

    public MainGameContainer getMainGameContainer() {
        return mainGameContainer;
    }

    public GUI_Game(GolfCourse course, GameManager gameManager) {
        this.gameContainer = new StackPane();

        //Startup invisible
        this.gameContainer.setVisible(false);
        this.gameContainer.setPickOnBounds(false);

        this.mainGameContainer = new MainGameContainer(gameManager);
        
        //Make a button to return to menu
        Button closeButton = new Button("Quit to Menu");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        closeButton.setOnAction(e -> this.hide());
        StackPane.setAlignment(closeButton, Pos.TOP_LEFT);
        StackPane.setMargin(closeButton, new Insets(10, 0, 0, 10));

        this.gameContainer.getChildren().addAll(mainGameContainer, closeButton);
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


    /* 
    // Add a standard main method to launch the application
    //change in pom.xml <mainClass>SimpleGUI.GUI_phase2</mainClass> to <mainClass>AdvancedGUI.GUI_phase3</mainClass>
    public static void main(String[] args) {
        launch(args);
    }
    */
    
}
