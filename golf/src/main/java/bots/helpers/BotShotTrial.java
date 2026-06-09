package bots.helpers;

import domain.course.GolfCourse;
import physics.ShotSimulator;
import solvers.Solver;
import physics.GolfODE;

/**
 * This runs one simulated putt for the search bots (using ShotSimulator this time)
 * and tracks the closest the ball got to the hole 
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
        
        double[][] trajectory = new ShotSimulator().schoot(ode, solver, state, STEP_SIZE);

        double closestDistance = Double.MAX_VALUE;
        double missXAtClosest = 0.0;
        double missYAtClosest = 0.0;

        for (double[] r : trajectory) {

            double x = r[1];
            double y = r[2];
            double distance = Math.hypot(targetX-x, targetY-y);

            if (distance < closestDistance) {
                closestDistance = distance;
                missXAtClosest = targetX - x;
                missYAtClosest = targetY - y;
            }

        }

        double[] last = trajectory[trajectory.length-1];
        double finalX = last[1];
        double finalY = last[2];
    
        boolean inWater = course.isWater(finalX, finalY);
        double[] size = course.getSize();
        boolean outOfBounds = finalX<size[0] || finalX>size[1]|| finalY<size[2] || finalY>size[3];
    
        return new BotTrialResult(
                finalX, finalY, inWater, outOfBounds,
                closestDistance, missXAtClosest, missYAtClosest);
    }

}
