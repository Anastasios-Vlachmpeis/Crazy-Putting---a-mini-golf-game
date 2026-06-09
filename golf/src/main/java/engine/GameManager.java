package engine;

/*
 * This is the main game controller.
 * It keeps the public methods used by the GUI and uses helper classes for turns, shots, rules, and recovery positions.
 */

import bots.GolfBot;
import domain.course.GolfCourse;
import solvers.Solver;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class GameManager {
    private static final int MAX_STROKES = 10;

    private final GolfCourse course;
    private final Solver solver;
    private final TurnManager turnManager = new TurnManager();
    private final ShotRunner shotRunner;
    private final StrokeCounter strokeCounter = new StrokeCounter();

    // Tracking current game coordinates
    private double currentBallX;
    private double currentBallY;
    private double lastSafeX;
    private double lastSafeY;
    private double waterRecoveryX;
    private double waterRecoveryY;
    private double edgeRecoveryX;
    private double edgeRecoveryY;

    private double playerX, playerY;
    private double botX, botY;

    // JavaFX Observable Properties for GUI bindings
    private final ObjectProperty<GameState> currentState = new SimpleObjectProperty<>(GameState.AIMING);
    private ShotResult lastShotResult = ShotResult.NORMAL;

    // For the 3D modeling
    private final DoubleProperty liveX = new SimpleDoubleProperty(0);
    private final DoubleProperty liveY = new SimpleDoubleProperty(0);
    private final DoubleProperty liveHeight = new SimpleDoubleProperty(0);

    public GameManager(GolfCourse course, Solver solver) {
        this.course = course;
        this.solver = solver;
        this.shotRunner = new ShotRunner(course, solver);
        resetGame();
    }

    public void setMultiplayerMode(boolean isMultiplayer, GolfBot bot) {
        turnManager.setMultiplayerMode(isMultiplayer, bot);
    }

    public double[] getBotShot() {
        return turnManager.getBotShot();
    }

    public boolean isMultiplayerMode() {
        return turnManager.isMultiplayerMode();
    }

    public BooleanProperty isPlayerTurnProperty() {
        return turnManager.isPlayerTurnProperty();
    }

    public boolean getIsPlayerTurn() {
        return turnManager.isPlayerTurn();
    }

    public double[][] hitBall(double vx, double vy) {
        if (currentState.get() != GameState.AIMING) {
            System.out.println("Cannot shoot: Ball is already in motion or game over!");
            return null;
        }

        currentState.set(GameState.ROLLING);
        lastShotResult = ShotResult.NORMAL;
        strokeCounter.addStroke(turnManager.isPlayerTurn());

        lastSafeX = currentBallX;
        lastSafeY = currentBallY;

        double[][] trajectory = shotRunner.runShot(currentBallX, currentBallY, vx, vy);
        RecoveryPositionCalculator.RecoveryPositions recoveryPositions =
            RecoveryPositionCalculator.calculate(course, trajectory, lastSafeX, lastSafeY);
        waterRecoveryX = recoveryPositions.waterX();
        waterRecoveryY = recoveryPositions.waterY();
        edgeRecoveryX = recoveryPositions.edgeX();
        edgeRecoveryY = recoveryPositions.edgeY();

        double[] finalState = trajectory[trajectory.length - 1];
        currentBallX = finalState[1];
        currentBallY = finalState[2];
        course.setBallPosition(currentBallX, currentBallY);
        saveLandingPositionForCurrentTurn();

        return trajectory;
    }

    public void finishShot() {
        if (currentState.get() == GameState.ROLLING) {
            evaluateMatchRules();
        }
    }

    public void resetGame() {
        double[] spawn = course.getOriginalStartPosition();

        playerX = spawn[0];
        playerY = spawn[1];
        botX = spawn[0];
        botY = spawn[1];

        currentBallX = spawn[0];
        currentBallY = spawn[1];
        lastSafeX = spawn[0];
        lastSafeY = spawn[1];
        waterRecoveryX = spawn[0];
        waterRecoveryY = spawn[1];
        edgeRecoveryX = spawn[0];
        edgeRecoveryY = spawn[1];

        updateLivePosition(currentBallX, currentBallY);
        strokeCounter.reset();
        currentState.set(GameState.AIMING);
        lastShotResult = ShotResult.NORMAL;
        turnManager.resetTurn();

        System.out.println("Match reset. Ball returned to tee box.");
    }

    private void saveLandingPositionForCurrentTurn() {
        if (turnManager.isPlayerTurn()) {
            playerX = currentBallX;
            playerY = currentBallY;
        } else {
            botX = currentBallX;
            botY = currentBallY;
        }
    }

    private void evaluateMatchRules() {
        RuleEvaluation evaluation = RuleEvaluator.evaluate(
            course,
            currentBallX,
            currentBallY,
            turnManager.isPlayerTurn(),
            strokeCounter.getPlayerStrokes(),
            strokeCounter.getBotStrokes(),
            MAX_STROKES
        );

        lastShotResult = evaluation.shotResult();

        if (evaluation.isPenalty()) {
            processPenalty(
                evaluation.consoleMessage(),
                getRecoveryX(evaluation.recoveryType()),
                getRecoveryY(evaluation.recoveryType())
            );
            return;
        }

        if (evaluation.nextState() != null) {
            currentState.set(evaluation.nextState());
            if (evaluation.consoleMessage() != null) {
                System.out.println(evaluation.consoleMessage());
            }
            return;
        }

        prepareNextShot();
    }

    private double getRecoveryX(RecoveryType recoveryType) {
        return recoveryType == RecoveryType.EDGE ? edgeRecoveryX : waterRecoveryX;
    }

    private double getRecoveryY(RecoveryType recoveryType) {
        return recoveryType == RecoveryType.EDGE ? edgeRecoveryY : waterRecoveryY;
    }

    private void processPenalty(String consoleMessage, double recoveryX, double recoveryY) {
        System.out.println(consoleMessage);

        currentBallX = recoveryX;
        currentBallY = recoveryY;
        course.setBallPosition(currentBallX, currentBallY);
        updateLivePosition(currentBallX, currentBallY);
        saveLandingPositionForCurrentTurn();
        currentState.set(GameState.AIMING);
    }

    private void prepareNextShot() {
        if (turnManager.isMultiplayerMode()) {
            turnManager.switchTurn();

            if (turnManager.isPlayerTurn()) {
                currentBallX = playerX;
                currentBallY = playerY;
            } else {
                currentBallX = botX;
                currentBallY = botY;
            }

            lastSafeX = currentBallX;
            lastSafeY = currentBallY;
            course.setBallPosition(currentBallX, currentBallY);
            updateLivePosition(currentBallX, currentBallY);
        }

        currentState.set(GameState.AIMING);
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }

    public double getBotX() {
        return botX;
    }

    public double getBotY() {
        return botY;
    }

    public ObjectProperty<GameState> currentStateProperty() {
        return currentState;
    }

    public GameState getCurrentState() {
        return currentState.get();
    }

    public IntegerProperty playerStrokesProperty() {
        return strokeCounter.playerStrokesProperty();
    }

    public int getPlayerStrokes() {
        return strokeCounter.getPlayerStrokes();
    }

    public IntegerProperty botStrokesProperty() {
        return strokeCounter.botStrokesProperty();
    }

    public int getBotStrokes() {
        return strokeCounter.getBotStrokes();
    }

    public ShotResult getLastShotResult() {
        return lastShotResult;
    }

    public double getEdgeRecoveryX() {
        return edgeRecoveryX;
    }

    public double getEdgeRecoveryY() {
        return edgeRecoveryY;
    }

    public DoubleProperty liveXProperty() {
        return liveX;
    }

    public DoubleProperty liveYProperty() {
        return liveY;
    }

    public DoubleProperty liveHeightProperty() {
        return liveHeight;
    }

    public GolfCourse getCourse() {
        return course;
    }

    public double getTerrainHeight(double x, double y) {
        return course.height(x, y);
    }

    public void updateLivePosition(double x, double y) {
        liveX.set(x);
        liveY.set(y);
        liveHeight.set(course.height(x, y));
    }

    public Solver getSolver() {
        return solver;
    }
}
