package Bots;

import java.util.ArrayList;

import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulation;
import ShotEngine.ShotSimulator;

/**
 *  Basically a grid search bot, the Manhattan bot tries many shot speeds and narrows the search around good ones
 */
public class ManhattanBot extends GolfBot {

    private static final double MAX_SPEED = 5.0;
    private static final int MAX_SEARCH_DEPTH = 5;

    private double vxStep;
    private double vyStep;
    private double minVx;
    private double maxVx;
    private double minVy;
    private double maxVy;
    private double startX;
    private double startY;
    private int searchDepth;
    private int simulationCount;

    // bestRestResult[0] distance at rest, [1] vx, [2] vy
    private final double[] bestRestResult = new double[3];

    private ArrayList<double[]> trialVelocities;

    public ManhattanBot(GolfCourse course, ShotSimulator shotSimulator) {
        super(course, shotSimulator);
    }

    // Picks vx and vy for the current ball position
    public double[] shoot() {
        return new double[] { 0.0, 0.0 };
    }

    // Set up search bounds from ball and target layout
    private void setupSearchBounds() {
    }

    // Scan a velocity range and may search again with smaller steps
    private double[] runGridSearch(double vxStep,double vyStep,
        double minVx,double maxVx,double minVy,double maxVy) {

        return new double[] { 0.0, 0.0 };
    }

    //Run one simulated shot for a velocity pair
    private ShotSimulation tryShot(double vx, double vy) {
        return null;
    }

    public int getSimulationCount() {
        return simulationCount;
    }
}
