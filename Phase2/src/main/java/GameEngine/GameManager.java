package GameEngine;

import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulatorV2;
import Solvers.Solver;
import Systems.GolfODE;
import Bots.GolfBot;
//For javaFX
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class GameManager {
    private final GolfCourse course;
    private final ShotSimulatorV2 simulator;
    private final Solver solver;

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

    public double getPlayerX() { return playerX; }
    public double getPlayerY() { return playerY; }
    public double getBotX() { return botX; }
    public double getBotY() { return botY; }

    //multiplayer
    private boolean isMultiplayer = false;
    private GolfBot activeBot = null;
    private final BooleanProperty isPlayerTurn = new SimpleBooleanProperty(true);

    public boolean isMultiplayerMode() { return isMultiplayer; }
    public BooleanProperty isPlayerTurnProperty() { return isPlayerTurn; }
    public boolean getIsPlayerTurn() { return isPlayerTurn.get(); }

    //private GameState currentState;
    //private int strokeCount;
    
    // JavaFX Observable Properties for instant GUI text/label bindings
    private final ObjectProperty<GameState> currentState = new SimpleObjectProperty<>(GameState.AIMING);
    private final IntegerProperty playerStrokes = new SimpleIntegerProperty(0);
    private final IntegerProperty botStrokes = new SimpleIntegerProperty(0);
    private ShotResult lastShotResult = ShotResult.NORMAL;
    
    private final int MAX_STROKES = 10; //max number of shots until game over
    private double stepSize;

    public void setMultiplayerMode(boolean isMultiplayer, GolfBot bot) {
        this.isMultiplayer = isMultiplayer;
        this.activeBot = bot;
        this.isPlayerTurn.set(true);
    }

    public double[] getBotShot() {
        if (activeBot != null) {
            return activeBot.shoot();
        }
        return new double[]{0, 0};
    }

    private void prepareNextShot() {
        if (isMultiplayer) {
            isPlayerTurn.set(!isPlayerTurn.get());

            if (isPlayerTurn.get()) {
                this.currentBallX = playerX;
                this.currentBallY = playerY;
                this.lastSafeX = playerX; //Fall back to previous position if landed in water
                this.lastSafeY = playerY;
            } else {
                this.currentBallX = botX;
                this.currentBallY = botY;
                this.lastSafeX = botX; 
                this.lastSafeY = botY;
            }
            course.setBallPosition(currentBallX, currentBallY);
            updateLivePosition(currentBallX, currentBallY);
        }
        currentState.set(GameState.AIMING);
    }

    public GameManager(GolfCourse course, Solver solver) {
        this.course = course;
        this.solver = solver;
        this.simulator = new ShotSimulatorV2(); // Centered physics loop processor
        //this.currentState = GameState.AIMING;
        //this.strokeCount = 0;

        this.stepSize = course.getStepSize(); //default is 100fps

        // Just for the first initialisation
        resetGame();
    }

    // The GUI or Bot triggers this method to initiate a play action
    public double[][] hitBall(double vx, double vy) {
        if (currentState.get() != GameState.AIMING) {
            System.out.println("Cannot shoot: Ball is already in motion or game over!");
            return null;
        }

        currentState.set(GameState.ROLLING);
        lastShotResult = ShotResult.NORMAL;
        //strokeCount++;
        if (isPlayerTurn.get()) {
            playerStrokes.set(playerStrokes.get() + 1);
        } else {
            botStrokes.set(botStrokes.get() + 1);
        };

        // Save previous coordinates in case we need to roll back water/ penalty
        this.lastSafeX = currentBallX;
        this.lastSafeY = currentBallY;
        
        // Gather active coordinates from the data model
        double[] startState = { currentBallX, currentBallY, vx, vy };

        // Wrap the course map inside your differential physics container
        GolfODE physicsEngine = new GolfODE(course); 
        // Process full resolution trajectory array path output matrices
        double[][] trajectory = simulator.schoot(physicsEngine, solver, startState, stepSize); 
        updateWaterRecoveryPosition(trajectory);
        updateEdgeRecoveryPosition(trajectory);

        // Update the resting coordinate models using the final row element index
        double[] finalState = trajectory[trajectory.length - 1]; 
        //course.setBallPosition(finalState[1], finalState[2]);
        this.currentBallX = finalState[1]; 
        this.currentBallY = finalState[2];

        //update the location
        course.setBallPosition(currentBallX, currentBallY);

        //Save landing position for correct player
        if (isPlayerTurn.get()) {
            playerX = currentBallX; playerY = currentBallY;
        } else {
            botX = currentBallX; botY = currentBallY;
        }

        return trajectory; // Pass coordinates array straight back to GUI timeline to animate
    }

    public void finishShot() {
        if (currentState.get() == GameState.ROLLING) {
            evaluateMatchRules();
        }
    }

    public void resetGame() {
        double[] spawn = course.getOriginalStartPosition(); 

        this.playerX = spawn[0]; this.playerY = spawn[1];
        this.botX = spawn[0]; this.botY = spawn[1];

        //For a the last shot
        this.currentBallX = spawn[0];
        this.currentBallY = spawn[1];
        this.lastSafeX = spawn[0];
        this.lastSafeY = spawn[1];
        
        //course.setBallPosition(currentBallX, currentBallY); 
        updateLivePosition(currentBallX, currentBallY);
        playerStrokes.set(0);
        botStrokes.set(0);
        currentState.set(GameState.AIMING);

        isPlayerTurn.set(true);//player starts

        System.out.println("Match reset. Ball returned to tee box.");
    }

    private void evaluateMatchRules() {
        // CHECK FOR OUT OF BOUNDS
        double[] boundaries = course.getSize(); // {minX, maxX, minY, maxY}
        boolean outOfBounds = (currentBallX < boundaries[0] || currentBallX > boundaries[1] ||
                               currentBallY < boundaries[2] || currentBallY > boundaries[3]);
        if (outOfBounds) {
            lastShotResult = ShotResult.OUT_OF_BOUNDS;
            processPenalty("OUT OF BOUNDS! Resetting to the edge.", edgeRecoveryX, edgeRecoveryY);
            return;
        }

        // CHECK FOR WATER
        if (course.isWater(currentBallX, currentBallY)) { //
            lastShotResult = ShotResult.WATER;
            processPenalty(
                "SPLASH! Ball landed in water. Resetting near water edge.",
                waterRecoveryX,
                waterRecoveryY
            );
            return;
        }

        // CHECK FOR VICTORY (Ball is inside the cup target radius)
        double[] target = course.getTargetXYR(); // {x, y, r}
        double distance = course.distanceToTarget(currentBallX, currentBallY);
        
        if (distance <= target[2]) {
            lastShotResult = ShotResult.HOLED_OUT;
            currentState.set(GameState.HOLED_OUT);
            
            String winnerName = isPlayerTurn.get() ? "Player" : "Bot";
            int winningStrokes = isPlayerTurn.get() ? playerStrokes.get() : botStrokes.get();
            
            System.out.println(winnerName + " achieved victory in " + winningStrokes + " strokes!");
            return;
        }

        // 4. CHECK FOR DEFEAT (Max strokes)
        int currentStrokes = isPlayerTurn.get() ? playerStrokes.get() : botStrokes.get();
        if (currentStrokes >= MAX_STROKES) {
            lastShotResult = ShotResult.GAME_OVER;
            currentState.set(GameState.GAME_OVER);
            System.out.println("Defeat: Exceeded maximum stroke limits.");
            return;
        }

        // If none of the above are met, ball is at rest safely on the green. Allow next shot.
        lastShotResult = ShotResult.NORMAL;
        //currentState.set(GameState.AIMING);
        prepareNextShot();
    }

    private void processPenalty(String consoleMessage) {
        processPenalty(consoleMessage, lastSafeX, lastSafeY);
    }

    private void processPenalty(String consoleMessage, double recoveryX, double recoveryY) {
        System.out.println(consoleMessage);
        
        // Roll back positions to where the player last shot from safely
        this.currentBallX = recoveryX;
        this.currentBallY = recoveryY;
        course.setBallPosition(currentBallX, currentBallY);
        updateLivePosition(currentBallX, currentBallY);

        //For saving to correct player
        if (isPlayerTurn.get()) {
            playerX = currentBallX; playerY = currentBallY;
        } else {
            botX = currentBallX; botY = currentBallY;
        }
        
        // Ready for recovery shot
        currentState.set(GameState.AIMING);
    }

    private void updateWaterRecoveryPosition(double[][] trajectory) {
        waterRecoveryX = lastSafeX;
        waterRecoveryY = lastSafeY;

        if (trajectory == null || trajectory.length < 2) {
            return;
        }

        for (int i = 1; i < trajectory.length; i++) {
            double x = trajectory[i][1];
            double y = trajectory[i][2];

            if (course.isWater(x, y)) {
                double previousX = trajectory[i - 1][1];
                double previousY = trajectory[i - 1][2];
                setRecoveryAwayFromWater(previousX, previousY, x, y);
                return;
            }
        }
    }

    private void setRecoveryAwayFromWater(double safeX, double safeY, double waterX, double waterY) {
        double dx = safeX - waterX;
        double dy = safeY - waterY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.0001) {
            waterRecoveryX = safeX;
            waterRecoveryY = safeY;
            return;
        }

        double clearance = 0.5;
        double candidateX = safeX + (dx / distance) * clearance;
        double candidateY = safeY + (dy / distance) * clearance;

        if (course.isWater(candidateX, candidateY)) {
            waterRecoveryX = safeX;
            waterRecoveryY = safeY;
        } else {
            waterRecoveryX = candidateX;
            waterRecoveryY = candidateY;
        }
    }

    private void updateEdgeRecoveryPosition(double[][] trajectory) {
        edgeRecoveryX = lastSafeX;
        edgeRecoveryY = lastSafeY;

        if (trajectory == null || trajectory.length < 2) {
            return;
        }

        for (int i = 1; i < trajectory.length; i++) {
            double previousX = trajectory[i - 1][1];
            double previousY = trajectory[i - 1][2];
            double currentX = trajectory[i][1];
            double currentY = trajectory[i][2];

            if (!isOutOfBounds(previousX, previousY) && isOutOfBounds(currentX, currentY)) {
                setEdgeRecoveryFromBoundary(previousX, previousY, currentX, currentY);
                return;
            }
        }
    }

    private boolean isOutOfBounds(double x, double y) {
        double[] boundaries = course.getSize();
        return x < boundaries[0] || x > boundaries[1] || y < boundaries[2] || y > boundaries[3];
    }

    private void setEdgeRecoveryFromBoundary(double previousX, double previousY, double currentX, double currentY) {
        double dx = currentX - previousX;
        double dy = currentY - previousY;
        double[] size = course.getSize();
        double contactFraction = 1.0;

        if (dx < 0.0) {
            contactFraction = Math.min(contactFraction, (size[0] - previousX) / dx);
        } else if (dx > 0.0) {
            contactFraction = Math.min(contactFraction, (size[1] - previousX) / dx);
        }

        if (dy < 0.0) {
            contactFraction = Math.min(contactFraction, (size[2] - previousY) / dy);
        } else if (dy > 0.0) {
            contactFraction = Math.min(contactFraction, (size[3] - previousY) / dy);
        }

        contactFraction = Math.max(0.0, Math.min(1.0, contactFraction));
        edgeRecoveryX = previousX + dx * contactFraction;
        edgeRecoveryY = previousY + dy * contactFraction;
    }

    // Getters for the GUI view layer properties
    public ObjectProperty<GameState> currentStateProperty() { return currentState; }
    public GameState getCurrentState() { return currentState.get(); }

    public IntegerProperty playerStrokesProperty() { return playerStrokes; }
    public int getPlayerStrokes() { return playerStrokes.get(); }
    
    public IntegerProperty botStrokesProperty() { return botStrokes; }
    public int getBotStrokes() { return botStrokes.get(); }

    public ShotResult getLastShotResult() { return lastShotResult; }
    public double getEdgeRecoveryX() { return edgeRecoveryX; }
    public double getEdgeRecoveryY() { return edgeRecoveryY; }

    //For the 3D modeling
    private final DoubleProperty liveX = new SimpleDoubleProperty(0);
    private final DoubleProperty liveY = new SimpleDoubleProperty(0);
    private final DoubleProperty liveHeight = new SimpleDoubleProperty(0);

    // Property getters for your 3D GUI bindings
    public DoubleProperty liveXProperty() { return liveX; }
    public DoubleProperty liveYProperty() { return liveY; }
    public DoubleProperty liveHeightProperty() { return liveHeight; }

    public GolfCourse getCourse() {
        return this.course;
    }

    public double getTerrainHeight(double x, double y) {
        return course.height(x, y);
    }

    public void updateLivePosition(double x, double y) {
        this.liveX.set(x);
        this.liveY.set(y);
        this.liveHeight.set(course.height(x, y));
    }

    public Solver getSolver() {
        return this.solver;
    }
}
