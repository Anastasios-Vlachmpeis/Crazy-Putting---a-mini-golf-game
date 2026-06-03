package Bots.helpers;

import GolfCourseData.GolfCourse;
import Solvers.Solver;
import Systems.GolfODE;

/**
 * This runs one simulated putt for the search bots and tracks
 * the closest the ball got to the hole
 */
public final class BotShotTrial {

    private static final double STEP_SIZE = 0.01;
    private static final double SPEED_THRESHOLD = 0.01;
    private static final double COURSE_BOUND = 500.0;
    private static final int MAX_STEPS = 1000000;

    private BotShotTrial() {}

    public static BotTrialResult run(GolfCourse course, Solver solver, double v0x, double v0y) {

        GolfODE ode = new GolfODE(course);
        double[] start = course.getStartPosition();
        double[] target = course.getTargetXYR();
        double targetX = target[0];
        double targetY = target[1];

        double[] state = { start[0], start[1], v0x, v0y };
        double t = 0.0;

        double closestDistance = Double.MAX_VALUE;
        double missXAtClosest = 0.0;
        double missYAtClosest = 0.0;
        boolean inWater = false;
        boolean outOfBounds = false;
        double finalX = state[0];
        double finalY = state[1];

        for (int step = 0; step < MAX_STEPS; step++) {

            double x = state[0];
            double y = state[1];
            double vx = state[2];
            double vy = state[3];

            finalX = x;
            finalY = y;

            double distance = course.distanceToTarget(x, y);
            if (distance < closestDistance) {
                closestDistance = distance;
                missXAtClosest = targetX - x;
                missYAtClosest = targetY - y;
            }

            if (course.isWater(x, y)) { inWater = true; break; }

            if (Math.abs(x) > COURSE_BOUND || Math.abs(y) > COURSE_BOUND) {
                outOfBounds = true; break;}

            double speed = Math.hypot(vx, vy);
            if (speed < SPEED_THRESHOLD) { // ball stopped on a flat enough slope
                double slopeMagnitude = Math.hypot(course.dhdx(x, y), course.dhdy(x, y));
                if (course.getMiuS(x, y) > slopeMagnitude) break; // static friction holds
            }

            state = stepOnce(ode, solver, state, t);
            t += STEP_SIZE;
        }

        if (closestDistance == Double.MAX_VALUE) {
            closestDistance = course.distanceToTarget(finalX, finalY);
            missXAtClosest  = targetX - finalX;
            missYAtClosest  = targetY - finalY;
        }

        return new BotTrialResult(
                finalX, finalY, inWater, outOfBounds,
                closestDistance, missXAtClosest, missYAtClosest);
    }

    private static double[] stepOnce(GolfODE ode, Solver solver, double[] state, double t) {

        double[][] solution = solver.solve(ode, state, t, t + STEP_SIZE, STEP_SIZE);
        double[] lastRow = solution[solution.length - 1];
        double[] next = new double[state.length];
        
        for (int i = 0; i < state.length; i++) next[i] = lastRow[i + 1];

        return next;
    }
}
