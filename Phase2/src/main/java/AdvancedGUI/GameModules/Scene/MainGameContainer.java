package AdvancedGUI.GameModules.Scene;

import Bots.GolfBot;
import Bots.ManhattanBot;
import GameEngine.GameManager;
import GameEngine.GameState;
import GameEngine.ShotResult;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Duration;

public class MainGameContainer extends StackPane {

    private static final double MAX_SHOT_SPEED = 5.0;

    private final GameManager gameManager;
    private final Game3DScene game3DScene;
    private final GameHUDOverlay hudOverlay;
    private Timeline currentShotTimeline;
    private Timeline currentDropTimeline;

    //multiplayer
    private boolean isMultiplayer = false;
    private boolean isPlayerTurn = true;
    private GolfBot activeBot = null;

    public MainGameContainer(GameManager gameManager) {
        this.gameManager = gameManager;

        // 1. Instantiate layout modules layers
        this.game3DScene = new Game3DScene(gameManager, 1000, 800);
        this.game3DScene.widthProperty().bind(this.widthProperty());
        this.game3DScene.heightProperty().bind(this.heightProperty()); //Makes the game as big as possible
        this.hudOverlay = new GameHUDOverlay(gameManager);

        // 2. Structural assembly order (3D back panel, 2D front HUD)
        this.getChildren().addAll(game3DScene, hudOverlay);

        // 3. Hook up interactive operational click lines hooks
        configureActions();
        syncVisualPositions();
    }

    private void configureActions() {
        hudOverlay.getShootButton().setOnAction(e -> executeShotAnimation());
        hudOverlay.getBotButton().setOnAction(e -> playBotStroke());
        game3DScene.setShotHandler(velocity -> executeShotAnimation(velocity[0], velocity[1]));
        game3DScene.setVelocityPreviewHandler(velocity -> hudOverlay.setVelocity(velocity[0], velocity[1]));
        hudOverlay.getResetButton().setOnAction(e -> {
            stopShotAnimation();
            gameManager.resetGame();
            syncVisualPositions();
        });

        // Event listener to pop up win/loss alert overlays instantly
        gameManager.currentStateProperty().addListener((obs, oldState, newState) -> {
            javafx.application.Platform.runLater(() -> {
                if (newState == GameState.HOLED_OUT) {
                    playDropInThenShowWinModal();
                } else if (newState == GameState.GAME_OVER) {
                    
                    // ---> [NIEUW] Controleer wie de limiet bereikte
                    String loserName = gameManager.getIsPlayerTurn() ? "Player" : "Bot";
                    showModalAlert("Game Over", loserName + " reached the maximum stroke limit of 10!");
                    
                } else if (newState == GameState.AIMING && gameManager.isMultiplayerMode() && !gameManager.getIsPlayerTurn()) {
                    triggerBotTurn();
                }
            });
        });
    }

    public void syncVisualPositions() {
        // Update player Ball
        double playerH = gameManager.getTerrainHeight(gameManager.getPlayerX(), gameManager.getPlayerY());
        game3DScene.updatePlayerBall(gameManager.getPlayerX(), gameManager.getPlayerY(), playerH);

        // Update Bot Ball
        if (gameManager.isMultiplayerMode()) {
            game3DScene.setMultiplayerVisibility(true);
            double botH = gameManager.getTerrainHeight(gameManager.getBotX(), gameManager.getBotY());
            game3DScene.updateBotBall(gameManager.getBotX(), gameManager.getBotY(), botH);
        } else {
            game3DScene.setMultiplayerVisibility(false);
        }

        // Flag
        double[] target = gameManager.getCourse().getTargetXYR();
        game3DScene.renderFlagPosition(target[0], target[1], gameManager.getTerrainHeight(target[0], target[1]));
    }

    public void refreshCourseFromBuilder() {
        stopShotAnimation();
        gameManager.resetGame();
        game3DScene.refreshCourseGeometry();
        syncVisualPositions();
    }

    private void executeShotAnimation() {
        if (gameManager.isMultiplayerMode() && !gameManager.getIsPlayerTurn()) {
            System.out.println("Wait for the bot to finish its turn!");
            return;
        }
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

            double[] cappedVelocity = capShotVelocity(vx, vy);
            vx = cappedVelocity[0];
            vy = cappedVelocity[1];
            hudOverlay.setVelocity(vx, vy);

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
                        
                        if (gameManager.getIsPlayerTurn()) {
                            game3DScene.updatePlayerBall(stepX, stepY, stepHeight);
                        } else {
                            game3DScene.updateBotBall(stepX, stepY, stepHeight);
                        }
                        gameManager.updateLivePosition(stepX, stepY);
                    }
                );
                currentShotTimeline.getKeyFrames().add(keyFrame);
            }

            currentShotTimeline.setOnFinished(e -> {
                gameManager.finishShot();
                if (gameManager.getLastShotResult() == ShotResult.WATER) {
                    javafx.application.Platform.runLater(() -> {
                        showModalAlert("Hit the water", "Your ball landed in the water.");
                    });
                }
                if (gameManager.getLastShotResult() == ShotResult.OUT_OF_BOUNDS) {
                    playOutOfBoundsFall(trajectory);
                    return;
                }

                currentShotTimeline = null;
                syncVisualPositions();

                // If we are in multiplayer and the game is ready for the next shot, swap turns
                if (isMultiplayer && gameManager.getCurrentState() == GameState.AIMING) {
                    isPlayerTurn = !isPlayerTurn; 
                    
                    // If it is now the bot's turn, trigger the bot
                    if (!isPlayerTurn && activeBot != null) {
                        triggerBotTurn();
                    }
                }
            });
            currentShotTimeline.play();

        } catch (NumberFormatException ex) {
            System.out.println("Error: Invalid numerical values inside text input field blocks.");
        }
    }

    private void playOutOfBoundsFall(double[][] trajectory) {
        double edgeX = gameManager.getEdgeRecoveryX();
        double edgeY = gameManager.getEdgeRecoveryY();
        double edgeHeight = gameManager.getTerrainHeight(edgeX, edgeY);
        double[] direction = outOfBoundsDirection(trajectory, edgeX, edgeY);

        currentDropTimeline = game3DScene.createOutOfBoundsFallAnimation(
            gameManager.getIsPlayerTurn(),
            edgeX,
            edgeY,
            edgeHeight,
            direction[0],
            direction[1],
            () -> {
                javafx.application.Platform.runLater(() ->
                    showModalAlert("Out of bounds", "Your ball fell off the edge.")
                );
                currentShotTimeline = null;
                currentDropTimeline = null;
                syncVisualPositions();

                if (isMultiplayer && gameManager.getCurrentState() == GameState.AIMING) {
                    isPlayerTurn = !isPlayerTurn;
                    if (!isPlayerTurn && activeBot != null) {
                        triggerBotTurn();
                    }
                }
            }
        );
        currentDropTimeline.play();
    }

    private double[] outOfBoundsDirection(double[][] trajectory, double edgeX, double edgeY) {
        if (trajectory != null && trajectory.length > 0) {
            double[] finalRow = trajectory[trajectory.length - 1];
            double dx = finalRow[1] - edgeX;
            double dy = finalRow[2] - edgeY;
            if (Math.sqrt(dx * dx + dy * dy) > 0.0001) {
                return new double[] { dx, dy };
            }
        }

        double[] size = gameManager.getCourse().getSize();
        double centerX = (size[0] + size[1]) / 2.0;
        double centerY = (size[2] + size[3]) / 2.0;
        return new double[] { edgeX - centerX, edgeY - centerY };
    }

    private double[] capShotVelocity(double vx, double vy) {
        double speed = Math.sqrt(vx * vx + vy * vy);

        if (speed > MAX_SHOT_SPEED) {
            vx = vx / speed * MAX_SHOT_SPEED;
            vy = vy / speed * MAX_SHOT_SPEED;
        }

        return new double[] {vx, vy};
    }

    private void stopShotAnimation() {
        if (currentShotTimeline != null) {
            currentShotTimeline.stop();
            currentShotTimeline = null;
        }
        if (currentDropTimeline != null) {
            currentDropTimeline.stop();
            currentDropTimeline = null;
        }
    }

    private void playDropInThenShowWinModal() {
        boolean playerWon = gameManager.getIsPlayerTurn();
        String winnerName = playerWon ? "Player (White)" : "Bot (Orange)";
        int finalScore = playerWon ? gameManager.getPlayerStrokes() : gameManager.getBotStrokes();

        double[] target = gameManager.getCourse().getTargetXYR();
        double targetHeight = gameManager.getTerrainHeight(target[0], target[1]);
        currentDropTimeline = game3DScene.createDropInAnimation(
            playerWon,
            target[0],
            target[1],
            targetHeight,
            () -> {
                currentDropTimeline = null;
                Platform.runLater(() ->
                    showModalAlert("We have a Winner!", winnerName + " completed the course in " + finalScore + " strokes.")
                );
            }
        );
        currentDropTimeline.play();
    }

    public void setMultiplayerMode(boolean isMultiplayer, String botName) {
        Bots.GolfBot bot = null;
        if (isMultiplayer && botName != null) {
            if (botName.equals("Simple Bot")) {
                bot = new Bots.SimpleBot(gameManager.getCourse(), gameManager.getSolver());
            } else if (botName.equals("ML Bot")) {
                bot = new Bots.MLBot(gameManager.getCourse(), gameManager.getSolver());
            } //Add all other bots
        }
        gameManager.setMultiplayerMode(isMultiplayer, bot);
    }

    private void triggerBotTurn() {
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(event -> {
            double[] botVelocity = gameManager.getBotShot();
            
            hudOverlay.setVelocity(botVelocity[0], botVelocity[1]);
            executeShotAnimation(botVelocity[0], botVelocity[1]);
        });
        pause.play();
    }

    private void showModalAlert(String title, String content) {
        //create window
        javafx.stage.Stage popupStage = new javafx.stage.Stage();
        popupStage.initModality(Modality.WINDOW_MODAL);
        
        Window owner = getScene() == null ? null : getScene().getWindow();
        if (owner != null) {
            popupStage.initOwner(owner);
        }
        
        //remove default top bar
        popupStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        javafx.scene.layout.VBox rootBox = new javafx.scene.layout.VBox(20);
        rootBox.setAlignment(javafx.geometry.Pos.CENTER);
        rootBox.setPadding(new javafx.geometry.Insets(30, 40, 30, 40));
        
        rootBox.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95);" +
            "-fx-background-radius: 20;" +
            "-fx-border-radius: 20;" +
            "-fx-border-color: #f39c12;" +
            "-fx-border-width: 3;"
        );
        
        // Add shadow around popup
        rootBox.setEffect(new javafx.scene.effect.DropShadow(15, javafx.scene.paint.Color.rgb(0, 0, 0, 0.4)));

        // add text
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #333333;");

        javafx.scene.control.Label contentLabel = new javafx.scene.control.Label(content);
        contentLabel.setFont(javafx.scene.text.Font.font("Arial", 16));
        contentLabel.setStyle("-fx-text-fill: #555555;");
        contentLabel.setWrapText(true);
        contentLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        javafx.scene.control.Button okButton = new javafx.scene.control.Button("Continue");
        String idleStyle = "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 10; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 10; -fx-cursor: hand;";
        
        okButton.setStyle(idleStyle);
        okButton.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
            okButton.setStyle(isNowHovered ? hoverStyle : idleStyle);
        });
        
        okButton.setOnAction(e -> popupStage.close());

        rootBox.getChildren().addAll(titleLabel, contentLabel, okButton);

        javafx.scene.Scene scene = new javafx.scene.Scene(rootBox);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    private void playBotStroke() {
        ManhattanBot bot = new ManhattanBot(gameManager.getCourse(), gameManager.getSolver());
        double[] v = bot.shoot();
        hudOverlay.setVelocity(v[0], v[1]);
        executeShotAnimation(v[0], v[1]);
    }
}
