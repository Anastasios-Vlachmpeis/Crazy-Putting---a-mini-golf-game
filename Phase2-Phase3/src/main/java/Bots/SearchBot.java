package Bots;

import Bots.helpers.BotShotTrial;
import Bots.helpers.BotTrialResult;
import GolfCourseData.GolfCourse;
import Solvers.Solver;

/**
 * Shared base for the two search-based bots: ManhattanBot and NewtonBot.
 */
public abstract class SearchBot extends GolfBot {

    protected final Solver solver;

    // Tracks the best resting position found during the current search.
    // Initialised to MAX_VALUE so any real candidate immediately becomes the best.
    protected double bestRestDistance = Double.MAX_VALUE;
    protected double bestRestVx = 0.0;
    protected double bestRestVy = 0.0;

    protected SearchBot(GolfCourse course, Solver solver) {
        super(course, solver);
        this.solver = solver;
    }

    /** Call at the start of every shoot() to clear state from the previous shot. */
    protected void resetBestRest() {
        bestRestDistance = Double.MAX_VALUE;
        bestRestVx = 0.0;
        bestRestVy = 0.0;
    }

    /** Runs a full physics trial from the current ball position. */
    protected BotTrialResult runTrial(double vx, double vy) {
        return BotShotTrial.run(course, solver, vx, vy);
    }

    /**
     * Records a simulated shot as a resting-position candidate if it is valid
     * (not in water, not out of bounds) and closer to the target than anything seen so far.
     */
    protected void noteRestingCandidate(BotTrialResult shot, double vx, double vy) {
        if (shot.inWater || shot.outOfBounds) return;
        if (course.isWater(shot.finalX, shot.finalY)) return;

        double[] target = course.getTargetXYR();
        double stopDistance = shot.stopDistance(target[0], target[1]);

        if (stopDistance < bestRestDistance) {
            bestRestDistance = stopDistance;
            bestRestVx = vx;
            bestRestVy = vy;
        }
    }

    /** Returns true if (x, y) is within the target radius. */
    protected boolean isInTarget(double x, double y) {
        double[] target = course.getTargetXYR();
        return course.distanceToTarget(x, y) <= target[2];
    }

    /** Fallback — returns the resting velocity pair closest to the target found so far. */
    protected double[] bestRestFallback() {
        return new double[]{ bestRestVx, bestRestVy };
    }
}
