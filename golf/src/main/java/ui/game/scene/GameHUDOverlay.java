package ui.game.scene;

import java.util.Locale;

import engine.GameManager;
import engine.GameState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GameHUDOverlay extends VBox {

    private final GameManager gameManager;
    
    // UI Form controls components accessible to the orchestrator container
    private TextField vxField;
    private TextField vyField;
    private Button shootButton;
    private Button resetButton;
    private Button botButton;

    public GameHUDOverlay(GameManager gameManager) {
        super(15); // 15px gap space between items
        this.gameManager = gameManager;
        
        // CRUCIAL: Pass mouse events straight through transparent slots to the 3D subscene behind it
        this.setPickOnBounds(false); 
        this.setPadding(new Insets(60, 0, 0, 10));
        this.setAlignment(Pos.TOP_LEFT);

        buildScoreCard();
        buildInputCard();
        setupBindings();
    }

    private void buildScoreCard() {
        VBox card = new VBox(5);
        card.setMaxWidth(220);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85); -fx-background-radius: 8px; -fx-border-color: #bdc3c7;");

        Label scoreLabel = new Label();
        scoreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        
        scoreLabel.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (gameManager.isMultiplayerMode()) {
                return "Player: " + gameManager.getPlayerStrokes() + "  |  Bot: " + gameManager.getBotStrokes();
            } else {
                return "Strokes: " + gameManager.getPlayerStrokes();
            }
        }, gameManager.playerStrokesProperty(), gameManager.botStrokesProperty()));

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        statusLabel.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(() -> {
            String state = gameManager.getCurrentState().toString();
            if (gameManager.isMultiplayerMode()) {
                String turn = gameManager.getIsPlayerTurn() ? "Player's Turn" : "Bot's Turn";
                return turn + " - " + state;
            } else {
                return "Status: " + state;
            }
        }, gameManager.currentStateProperty(), gameManager.isPlayerTurnProperty()));

        card.getChildren().addAll(scoreLabel, statusLabel);
        this.getChildren().add(card);
    }

    private void buildInputCard() {
        VBox card = new VBox(10);
        card.setMaxWidth(220);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85); -fx-background-radius: 8px;");

        vxField = new TextField("12.5");
        vyField = new TextField("5.0");

        shootButton = new Button("Launch Stroke");
        shootButton.setMaxWidth(Double.MAX_VALUE);
        shootButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");

        resetButton = new Button("Restart Level");
        resetButton.setMaxWidth(Double.MAX_VALUE);

        botButton = new Button("Bot Stroke");
        botButton.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(
            new Label("Velocity X:"), vxField,
            new Label("Velocity Y:"), vyField,
            shootButton, resetButton, botButton
        );
        this.getChildren().add(card);
    }

    private void setupBindings() {
        // Automatically lock user fields while physics loop calculations run frame outputs
        shootButton.disableProperty().bind(gameManager.currentStateProperty().isEqualTo(GameState.ROLLING));
        vxField.disableProperty().bind(gameManager.currentStateProperty().isEqualTo(GameState.ROLLING));
        vyField.disableProperty().bind(gameManager.currentStateProperty().isEqualTo(GameState.ROLLING));
    }

    // Getters to expose controls to the container
    public Button getShootButton() { return shootButton; }
    public Button getResetButton() { return resetButton; }
    public Button getBotButton() { return botButton; }
    
    public double getVelocityX() { return Double.parseDouble(vxField.getText().trim()); }
    public double getVelocityY() { return Double.parseDouble(vyField.getText().trim()); }

    public void setVelocity(double vx, double vy) {
        vxField.setText(String.format(Locale.US, "%.2f", vx));
        vyField.setText(String.format(Locale.US, "%.2f", vy));
    }
}
