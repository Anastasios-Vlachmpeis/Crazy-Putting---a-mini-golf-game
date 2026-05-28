package Bots;

import java.util.ArrayList;

import Bots.helpers.BotTrialResult;
import Bots.helpers.VelocitySearchWindow;
import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulator;
import Solvers.Solver;

/**
 * Basically a grid-search bot, it scans a velocity grid,
 * zooms in on the best candidate
 * and repeats up to MAX_SEARCH_DEPTH times (5 default)
 */

public class ManhattanBot extends SearchBot {

    private static final double MAX_SPEED = 5.0;
    private static final int MAX_SEARCH_DEPTH = 5;

    private int searchDepth;
    private int simulationCount;

    public ManhattanBot(GolfCourse course, ShotSimulator shotSimulator, Solver solver) {
        super(course, shotSimulator, solver);
    }

    public double[] shoot() {
        simulationCount = 0;
        searchDepth = 0;
        resetBestRest();

        double[] start = course.getStartPosition();
        double[] target = course.getTargetXYR();

        VelocitySearchWindow window = new VelocitySearchWindow();
        window.orientForLayout(start[0], start[1], target[0], target[1]);

        return runGridSearch(
                window.getVxStep(), window.getVyStep(),
                window.getMinVx(), window.getMaxVx(),
                window.getMinVy(), window.getMaxVy());
    }

    /**
     * Scans the given velocity range, tracks the path closest to the target,
     * then recursively zooms in — or falls back to the best resting position.
     */
    private double[] runGridSearch(
            double vxStep, double vyStep,
            double minVx, double maxVx,
            double minVy, double maxVy) {

        searchDepth++;

        ArrayList<double[]> candidates = VelocitySearchWindow.buildCandidates(
            vxStep, vyStep, minVx, maxVx, minVy, maxVy, MAX_SPEED);

        double bestPathVx = 0.0;
        double bestPathVy = 0.0;
        double closestPathDist = Double.MAX_VALUE;

        for (double[] candidate : candidates) {
            BotTrialResult shot = tryShot(candidate[0], candidate[1]);

            noteRestingCandidate(shot, candidate[0], candidate[1]);

            double pathDistance = shot.closestDistance;
            if (pathDistance< closestPathDist) {
                closestPathDist = pathDistance;
                bestPathVx = candidate[0];
                bestPathVy = candidate[1];
            }

            if (isInTarget(shot.finalX, shot.finalY)) {
                return new double[]{ candidate[0], candidate[1] };
            }
        }

        if (searchDepth == MAX_SEARCH_DEPTH) {
            searchDepth = 0;
            return bestRestFallback();
        }

        return runGridSearch(
                vxStep / 4.0, vyStep / 4.0,
                bestPathVx - vxStep, bestPathVx + vxStep,
                bestPathVy - vyStep, bestPathVy + vyStep);
    }

    private BotTrialResult tryShot(double vx, double vy) {
        simulationCount++;
        return runTrial(vx, vy);
    }

    public int getSimulationCount() { return simulationCount; }
}
