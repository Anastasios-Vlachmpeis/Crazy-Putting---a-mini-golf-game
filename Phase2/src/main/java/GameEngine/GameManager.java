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

    //private GameState currentState;
    //private int strokeCount;
    
    // JavaFX Observable Properties for instant GUI text/label bindings
    private final ObjectProperty<GameState> currentState = new SimpleObjectProperty<>(GameState.AIMING);
    private final IntegerProperty strokeCount = new SimpleIntegerProperty(0);
    
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
        if (currentState.get() != GameState.AIMING || currentState.get() != GameState.ROLLING) {
            System.out.println("Cannot shoot: Ball is already in motion or game over!");
            return null;
        }

        currentState.set(GameState.ROLLING);
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

        // Update the resting coordinate models using the final row element index
        double[] finalState = trajectory[trajectory.length - 1]; 
        //course.setBallPosition(finalState[1], finalState[2]);
        this.currentBallX = finalState[1]; 
        this.currentBallY = finalState[2];

        //update the location
        course.setBallPosition(currentBallX, currentBallY);

        // Evaluate final landing conditions using targets configuration thresholds
        evaluateMatchRules();

        return trajectory; // Pass coordinates array straight back to GUI timeline to animate
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
            processPenalty("OUT OF BOUNDS! +1 Penalty Stroke. Resetting to last position.");
            return;
        }

        // CHECK FOR WATER
        if (course.isWater(currentBallX, currentBallY)) { //
            processPenalty("SPLASH! Ball landed in water. +1 Penalty Stroke. Resetting.");
            return;
        }

        // CHECK FOR VICTORY (Ball is inside the cup target radius)
        double[] target = course.getTargetXYR(); // {x, y, r]
        double distance = course.distanceToTarget(currentBallX, currentBallY);
        if (distance <= target[2]) {
            currentState.set(GameState.HOLED_OUT);
            System.out.println("Victory achieved in " + strokeCount.get() + " strokes!");
            return;
        }

        // 4. CHECK FOR DEFEAT (Max strokes)
        if (strokeCount.get() >= MAX_STROKES) {
            currentState.set(GameState.GAME_OVER);
            System.out.println("Defeat: Exceeded maximum stroke limits.");
            return;
        }

        // If none of the above are met, ball is at rest safely on the green. Allow next shot.
        currentState.set(GameState.AIMING);
    }

    private void processPenalty(String consoleMessage) {
        System.out.println(consoleMessage);
        strokeCount.set(strokeCount.get() + 1); // Extra penalty stroke
        
        // Roll back positions to where the player last shot from safely
        this.currentBallX = lastSafeX;
        this.currentBallY = lastSafeY;
        //course.setBallPosition(currentBallX, currentBallY);
        updateLivePosition(currentBallX, currentBallY);
        
        // Ready for recovery shot
        currentState.set(GameState.AIMING);
    }

    // Getters for the GUI view layer properties
    public ObjectProperty<GameState> currentStateProperty() { return currentState; }
    public GameState getCurrentState() { return currentState.get(); }

    public IntegerProperty strokeCountProperty() { return strokeCount; }
    public int getStrokeCount() { return strokeCount.get(); }

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