package Bots;

import Bots.helpers.BotTrialResult;
import Bots.helpers.VelocitySearchWindow;
import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulator;
import Solvers.Solver;

/**
 * Guesses a starting shot, then improves it step by step using Newton-style updates
 */

public class NewtonBot extends SearchBot {

    private static final double MAX_SPEED = 5.0;
    private static final int MAX_SEARCH_DEPTH = 5;

    private double seedVx;
    private double seedVy;
    private double currentVx;
    private double currentVy;
    private double slopeDelta = 1.0;

    private int simulationCount;
    private int updateCount;
    private int searchDepth;

    public NewtonBot(GolfCourse course, ShotSimulator shotSimulator, Solver solver) {
        super(course, shotSimulator, solver);
    }

    public double[] shoot() {
        simulationCount = 0;
        updateCount = 0;
        searchDepth = 0;
        slopeDelta = 1.0;
        resetBestRest();

        findSeedVelocity();
        currentVx = seedVx;
        currentVy = seedVy;

        return runNewtonSteps(currentVx, currentVy);
    }

    // Scan the grid of speeds and keep the one that gets closest to the hole
    private void findSeedVelocity() {
        double[] start = course.getStartPosition();
        double[] target = course.getTargetXYR();

        VelocitySearchWindow window = new VelocitySearchWindow();
        window.orientForLayout(start[0], start[1], target[0], target[1]);

        seedVx = 0.0;
        seedVy = 0.0;
        double bestDistance = Double.MAX_VALUE;

        for (double[] candidate : window.buildCandidates(MAX_SPEED)) {
            double[] miss = measureShotMiss(candidate[0], candidate[1]);

            if (miss[2] < bestDistance) {
                bestDistance = miss[2];
                seedVx = candidate[0];
                seedVy = candidate[1];
            }
        }
    }

    //Adjust vx/vy until the shot reaches the hole, or give up and use the best resting shot
    private double[] runNewtonSteps(double vx, double vy) {
        searchDepth++;

        double[] target = course.getTargetXYR();
        double targetRadius = target[2];
        double[] miss = measureShotMiss(vx, vy);

        if (miss[2]<= targetRadius) {
            updateCount++;

            if (Math.hypot(vx, vy) <= MAX_SPEED) {
                return new double[]{ vx, vy };
            }

            slopeDelta /= 2.0;
            vx = seedVx;
            vy = seedVy;
        }

        if (searchDepth == MAX_SEARCH_DEPTH) {
            searchDepth = 0;
            return bestRestFallback();
        }

        double dvx = slopeAt(vx, vy, true);
        double dvy = slopeAt(vx, vy, false);
        if (Math.abs(dvx)< 1e-12) dvx = 1e-12;
        if (Math.abs(dvy)< 1e-12) dvy = 1e-12;

        return runNewtonSteps(vx - miss[0] / dvx, vy - miss[1] / dvy);
    }

    // returns an array with missX, missY and closestDistance for a simulated shot
    private double[] measureShotMiss(double vx, double vy) {
        simulationCount++;
        BotTrialResult shot = runTrial(vx, vy);
        noteRestingCandidate(shot, vx, vy);

        return new double[]{ shot.missXAtClosest, shot.missYAtClosest, shot.closestDistance };
    }

    // estimates how much the miss changes when vx or vy changes a little
    private double slopeAt(double vx, double vy, boolean useVx) {

        if (useVx) {
            double fwd = measureShotMiss(vx + slopeDelta, vy)[0];
            double bwd = measureShotMiss(vx - slopeDelta, vy)[0];
            return (fwd - bwd) / (2.0 * slopeDelta);
        }

        double fwd = measureShotMiss(vx, vy + slopeDelta)[1];
        double bwd = measureShotMiss(vx, vy - slopeDelta)[1];

        return (fwd - bwd) / (2.0 * slopeDelta);
    }

    public int getSimulationCount() { return simulationCount; }
}
