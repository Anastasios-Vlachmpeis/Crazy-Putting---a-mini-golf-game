package Bots;

import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulator;

/**
 * A bot that improves it's shot guess with Newton-style updates on simulated miss values
 */
public class NewtonBot extends GolfBot {

    private static final double MAX_SPEED = 5.0;
    private static final int MAX_SEARCH_DEPTH = 5;

    private double currentVx;
    private double currentVy;
    private double slopeDelta;
    private double seedVx;
    private double seedVy;
    private int simulationCount;
    private int updateCount;
    private int searchDepth;

    // bestRestResult[0] distance at rest, [1] vx, [2] vy
    private final double[] bestRestResult = new double[3];

    public NewtonBot(GolfCourse course, ShotSimulator shotSimulator) {
        super(course, shotSimulator);
    }

    // Picks vx and vy for the current ball position
    public double[] shoot() {
        return new double[] {0.0, 0.0 };
    }

    //Find a rough starting velocity with a small grid scan
    private void findSeedVelocity() {
    }

    // Repeats Newton-like updates from a starting velocity pair
    private double[] runNewtonSteps(double vx, double vy) {
        return new double[] {0.0, 0.0};
    }

    // Return miss in x, miss in y, and closest distance during the shot
    private double[] measureShotMiss(double vx, double vy) {
        return new double[] {0.0, 0.0, 0.0};
    }

    //Estimates slope for vx when useVx is true, otherwise for vy
    private double slopeAt(double vx, double vy, boolean useVx) {
        return 0.0;
    }


    public int getSimulationCount() {
        return simulationCount;
    }
    
}
