package AdvancedGUI.MainGUIModules;

import GameEngine.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Duration;

public class MainGameContainer extends StackPane {

    private final GameManager gameManager;
    private final Game3DScene game3DScene;
    private final GameHUDOverlay hudOverlay;
    private Timeline currentShotTimeline;

    public MainGameContainer(GameManager gameManager) {
        this.gameManager = gameManager;

        // 1. Instantiate layout modules layers
        this.game3DScene = new Game3DScene(gameManager, 1000, 800);
        this.hudOverlay = new GameHUDOverlay(gameManager);

        // 2. Structural assembly order (3D back panel, 2D front HUD)
        this.getChildren().addAll(game3DScene, hudOverlay);

        // 3. Hook up interactive operational click lines hooks
        configureActions();
        syncVisualPositions();
    }

    private void configureActions() {
        hudOverlay.getShootButton().setOnAction(e -> executeShotAnimation());
        game3DScene.setShotHandler(velocity -> executeShotAnimation(velocity[0], velocity[1]));
        hudOverlay.getResetButton().setOnAction(e -> {
            stopShotAnimation();
            gameManager.resetGame();
            syncVisualPositions();
        });

        // Event listener to pop up win/loss alert overlays instantly
        gameManager.currentStateProperty().addListener((obs, oldState, newState) -> {
            if (newState == GameState.HOLED_OUT) {
                showModalAlert("Victory!", "Course completed in " + gameManager.getStrokeCount() + " strokes.");
            } else if (newState == GameState.GAME_OVER) {
                showModalAlert("Game Over", "Maximum allowed stroke allocation limits reached!");
            }
        });
    }

    public void syncVisualPositions() {
        // Position 3D Ball Node
        game3DScene.renderBallPosition(
            gameManager.liveXProperty().get(),
            gameManager.liveYProperty().get(),
            gameManager.liveHeightProperty().get()
        );

        // Position 3D Target Flag Node
        double[] target = gameManager.getCourse().getTargetXYR();
        game3DScene.renderFlagPosition(
            target[0], 
            target[1], 
            gameManager.getTerrainHeight(target[0], target[1])
        );
    }

    public void refreshCourseFromBuilder() {
        stopShotAnimation();
        gameManager.resetGame();
        game3DScene.refreshCourseGeometry();
        syncVisualPositions();
    }

    private void executeShotAnimation() {
        try {
            double vx = hudOverlay.getVelocityX();
            double vy = hudOverlay.getVelocityY();
            executeShotAnimation(vx, vy);

        } catch (NumberFormatException ex) {
            System.out.println("Error: Invalid numerical values inside text input field blocks.");
        }
    }

    private void executeShotAnimation(double vx, double vy) {
        try {
            if (currentShotTimeline != null && currentShotTimeline.getStatus() == Animation.Status.RUNNING) {
                return;
            }

            // Run calculations matrix profile out
            double[][] trajectory = gameManager.hitBall(vx, vy);
            if (trajectory == null || trajectory.length == 0) return;

            currentShotTimeline = new Timeline();
            currentShotTimeline.setCycleCount(1);
            Duration frameDuration = Duration.millis(12);

            for (int i = 0; i < trajectory.length; i++) {
                final int frame = i;
                double stepX = trajectory[frame][1]; //
                double stepY = trajectory[frame][2]; //

                KeyFrame keyFrame = new KeyFrame(
                    frameDuration.multiply(frame),
                    event -> {
                        double stepHeight = gameManager.getTerrainHeight(stepX, stepY);
                        
                        // Pass parameters right down into 3D space matrix paths
                        game3DScene.renderBallPosition(stepX, stepY, stepHeight);
                        gameManager.updateLivePosition(stepX, stepY);
                    }
                );
                currentShotTimeline.getKeyFrames().add(keyFrame);
            }

            currentShotTimeline.setOnFinished(e -> {
                gameManager.finishShot();
                if (gameManager.getLastShotResult() == ShotResult.WATER) {
                    game3DScene.setBallVisible(false);
                    showModalAlert("Hit the water", "Your ball landed in the water. Penalty stroke applied.");
                    game3DScene.setBallVisible(true);
                }
                currentShotTimeline = null;
                syncVisualPositions();
            });
            currentShotTimeline.play();

        } catch (NumberFormatException ex) {
            System.out.println("Error: Invalid numerical values inside text input field blocks.");
        }
    }

    private void showModalAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Window owner = getScene() == null ? null : getScene().getWindow();
        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        }
        alert.showAndWait();
    }

    private void stopShotAnimation() {
        if (currentShotTimeline != null) {
            currentShotTimeline.stop();
            currentShotTimeline = null;
        }
    }
}
