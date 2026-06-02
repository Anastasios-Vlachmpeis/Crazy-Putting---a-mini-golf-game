package GameEngine;

import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulatorV2;
import Solvers.Solver;
import Systems.GolfODE;
//For javaFX
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

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

    //private GameState currentState;
    //private int strokeCount;
    
    // JavaFX Observable Properties for instant GUI text/label bindings
    private final ObjectProperty<GameState> currentState = new SimpleObjectProperty<>(GameState.AIMING);
    private final IntegerProperty strokeCount = new SimpleIntegerProperty(0);
    private ShotResult lastShotResult = ShotResult.NORMAL;
    
    private final int MAX_STROKES = 10; //max number of shots until game over
    private double stepSize;

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
        strokeCount.set(strokeCount.get() + 1);

        // Save previous coordinates in case we need to roll back water/ penalty
        this.lastSafeX = currentBallX;
        this.lastSafeY = currentBallY;
        
        // Gather active coordinates from the data model
        //double[] ballPos = course.getStartPosition(); 
        //double[] startState = { ballPos[0], ballPos[1], vx, vy };
        //FIxed: starts from current location instead of original start position
        double[] startState = { currentBallX, currentBallY, vx, vy };

        // Wrap the course map inside your differential physics container
        GolfODE physicsEngine = new GolfODE(course); 
        // Process full resolution trajectory array path output matrices
        double[][] trajectory = simulator.schoot(physicsEngine, solver, startState, stepSize); 
        updateWaterRecoveryPosition(trajectory);

        // Update the resting coordinate models using the final row element index
        double[] finalState = trajectory[trajectory.length - 1]; 
        //course.setBallPosition(finalState[1], finalState[2]);
        this.currentBallX = finalState[1]; 
        this.currentBallY = finalState[2];

        //update the location
        course.setBallPosition(currentBallX, currentBallY);

        return trajectory; // Pass coordinates array straight back to GUI timeline to animate
    }

    public void finishShot() {
        if (currentState.get() == GameState.ROLLING) {
            evaluateMatchRules();
        }
    }

    public void resetGame() {
        double[] spawn = course.getOriginalStartPosition(); 
        this.currentBallX = spawn[0];
        this.currentBallY = spawn[1];
        this.lastSafeX = spawn[0];
        this.lastSafeY = spawn[1];
        
        //course.setBallPosition(currentBallX, currentBallY); 
        updateLivePosition(currentBallX, currentBallY);
        strokeCount.set(0);
        currentState.set(GameState.AIMING);
        System.out.println("Match reset. Ball returned to tee box.");
    }

    private void evaluateMatchRules() {
        // CHECK FOR OUT OF BOUNDS
        double[] boundaries = course.getSize(); // {minX, maxX, minY, maxY}
        boolean outOfBounds = (currentBallX < boundaries[0] || currentBallX > boundaries[1] ||
                               currentBallY < boundaries[2] || currentBallY > boundaries[3]);
        if (outOfBounds) {
            lastShotResult = ShotResult.OUT_OF_BOUNDS;
            processPenalty("OUT OF BOUNDS! +1 Penalty Stroke. Resetting to last position.");
            return;
        }

        // CHECK FOR WATER
        if (course.isWater(currentBallX, currentBallY)) { //
            lastShotResult = ShotResult.WATER;
            processPenalty(
                "SPLASH! Ball landed in water. +1 Penalty Stroke. Resetting near water edge.",
                waterRecoveryX,
                waterRecoveryY
            );
            return;
        }

        // CHECK FOR VICTORY (Ball is inside the cup target radius)
        double[] target = course.getTargetXYR(); // {x, y, r]
        double distance = course.distanceToTarget(currentBallX, currentBallY);
        if (distance <= target[2]) {
            lastShotResult = ShotResult.HOLED_OUT;
            currentState.set(GameState.HOLED_OUT);
            System.out.println("Victory achieved in " + strokeCount.get() + " strokes!");
            return;
        }

        // 4. CHECK FOR DEFEAT (Max strokes)
        if (strokeCount.get() >= MAX_STROKES) {
            lastShotResult = ShotResult.GAME_OVER;
            currentState.set(GameState.GAME_OVER);
            System.out.println("Defeat: Exceeded maximum stroke limits.");
            return;
        }

        // If none of the above are met, ball is at rest safely on the green. Allow next shot.
        lastShotResult = ShotResult.NORMAL;
        currentState.set(GameState.AIMING);
    }

    private void processPenalty(String consoleMessage) {
        processPenalty(consoleMessage, lastSafeX, lastSafeY);
    }

    private void processPenalty(String consoleMessage, double recoveryX, double recoveryY) {
        System.out.println(consoleMessage);
        strokeCount.set(strokeCount.get() + 1); // Extra penalty stroke
        
        // Roll back positions to where the player last shot from safely
        this.currentBallX = recoveryX;
        this.currentBallY = recoveryY;
        course.setBallPosition(currentBallX, currentBallY);
        updateLivePosition(currentBallX, currentBallY);
        
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

    // Getters for the GUI view layer properties
    public ObjectProperty<GameState> currentStateProperty() { return currentState; }
    public GameState getCurrentState() { return currentState.get(); }

    public IntegerProperty strokeCountProperty() { return strokeCount; }
    public int getStrokeCount() { return strokeCount.get(); }
    public ShotResult getLastShotResult() { return lastShotResult; }

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
}
