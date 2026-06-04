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

        // Make the ball roll to its true resting point instead of overshooting
        boolean atHole = aim[0] == realTarget[0] && aim[1] == realTarget[1];
        double simRadius = atHole ? realTarget[2] : 0.0;
        course.setTargetXYR(aim[0], aim[1], simRadius);

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

        double[] aim = course.getTargetXYR(); // temporarily set to the active waypoint in shoot()

        ArrayList<double[]> candidates = VelocitySearchWindow.buildCandidates(
            vxStep, vyStep, minVx, maxVx, minVy, maxVy, MAX_SPEED);

        double bestVx = 0.0;
        double bestVy = 0.0;
        double bestRestDist = Double.MAX_VALUE;

        for (double[] candidate : candidates) {
            BotTrialResult shot = tryShot(candidate[0], candidate[1]);

            noteRestingCandidate(shot, candidate[0], candidate[1]);

            // a shot that drowns or flies out is a big no no, skip it
            if (shot.inWater || shot.outOfBounds) continue;

            // rank by where the ball comes to REST, so that ya don't overshoot
            double restDistance = shot.stopDistance(aim[0], aim[1]);
            if (restDistance < bestRestDist) {
                bestRestDist = restDistance;
                bestVx = candidate[0];
                bestVy = candidate[1];
            }
        }

        // the best shot already parks in the aim radius, we are done
        if (bestRestDist <= aim[2]) {
            searchDepth = 0;
            return new double[]{ bestVx, bestVy };
        }

        if (searchDepth == MAX_SEARCH_DEPTH) {
            searchDepth = 0;
            return bestRestFallback();
        }

        return runGridSearch(
                vxStep / 4.0, vyStep / 4.0,
                bestVx - vxStep, bestVx + vxStep,
                bestVy - vyStep, bestVy + vyStep);
    }

    private BotTrialResult tryShot(double vx, double vy) {
        simulationCount++;
        return runTrial(vx, vy);
    }

    public int getSimulationCount() { return simulationCount; }
}
