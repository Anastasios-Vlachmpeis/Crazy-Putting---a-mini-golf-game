package bots;

import bots.helpers.BotTrialResult;
import bots.helpers.VelocitySearchWindow;
import bots.helpers.WaypointPlanner;
import domain.course.GolfCourse;
import solvers.Solver;

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

    public NewtonBot(GolfCourse course, Solver solver) {
        super(course, solver);
    }

    public double[] shoot() {
        simulationCount = 0;
        updateCount = 0;
        searchDepth = 0;
        slopeDelta = 1.0;
        resetBestRest();

        // aim at the next waypoint instead of the hole, then put the real hole back
        double[] aim = WaypointPlanner.activeAim(course);
        double[] realTarget = course.getTargetXYR().clone();

        // Make the ball roll to its true resting point instead of overshooting
        boolean atHole = aim[0] == realTarget[0] && aim[1] == realTarget[1];
        double simRadius = atHole ? realTarget[2] : 0.0;
        course.setTargetXYR(aim[0], aim[1], simRadius);

        try {
            findSeedVelocity();
            currentVx = seedVx;
            currentVy = seedVy;

            return runNewtonSteps(currentVx, currentVy);
        } finally {
            course.setTargetXYR(realTarget[0], realTarget[1], realTarget[2]);
        }
    }

    // Scan the grid of speeds, the best resting shot becomes the Newton starting point
    private void findSeedVelocity() {
        double[] start = course.getStartPosition();
        double[] aim = course.getTargetXYR();

        VelocitySearchWindow window = new VelocitySearchWindow();
        window.orientForLayout(start[0], start[1], aim[0], aim[1]);

        for (double[] candidate : window.buildCandidates(MAX_SPEED)) {
            measureShotMiss(candidate[0], candidate[1]); // update bestRest w/ noteRestingCandidate
        }
        // bestRest is the resting shot closest to the aim, w/ water and bounds filtered out
        seedVx = bestRestVx;
        seedVy = bestRestVy;
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

    // returns the resting miss in x and y, + the resting distance to the aim,
    // to keep the bot from overshooting
    private double[] measureShotMiss(double vx, double vy) {
        simulationCount++;
        BotTrialResult shot = runTrial(vx, vy);
        noteRestingCandidate(shot, vx, vy);

        double[] aim = course.getTargetXYR();
        double missX = aim[0] - shot.finalX;
        double missY = aim[1] - shot.finalY;
        return new double[]{missX, missY, Math.hypot(missX, missY)};
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
