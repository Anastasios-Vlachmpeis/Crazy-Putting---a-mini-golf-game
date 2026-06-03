package Bots;

import java.util.ArrayList;

import Bots.helpers.BotTrialResult;
import Bots.helpers.VelocitySearchWindow;
import Bots.helpers.WaypointPlanner;
import GolfCourseData.GolfCourse;
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

    public ManhattanBot(GolfCourse course, Solver solver) {
        super(course, solver);
    }

    public double[] shoot() {
        simulationCount = 0;
        searchDepth = 0;
        resetBestRest();

        // aim at the next waypoint instead of the hole, then put the real hole back
        double[] aim = WaypointPlanner.activeAim(course);
        double[] realTarget = course.getTargetXYR().clone();
        course.setTargetXYR(aim[0], aim[1], aim[2]);

        try {
            double[] start = course.getStartPosition();

            VelocitySearchWindow window = new VelocitySearchWindow();
            window.orientForLayout(start[0], start[1], aim[0], aim[1]);

            return runGridSearch(
                    window.getVxStep(), window.getVyStep(),
                    window.getMinVx(), window.getMaxVx(),
                    window.getMinVy(), window.getMaxVy());
        } finally {
            course.setTargetXYR(realTarget[0], realTarget[1], realTarget[2]);
        }
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
