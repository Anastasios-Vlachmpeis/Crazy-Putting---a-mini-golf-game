package Bots;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulatorV2;
import Systems.GolfODE;
import Solvers.RungeKuttaSolver;
import Solvers.Solver;

public class HillBot extends GolfBot {

    private static final double MAX_SPEED = 5.0;
    private final Random random = new Random();
    private double iterations = 0;

    public HillBot(GolfCourse course, Solver solver) {
        super(course, solver);
    }

    public double getIterations() {
        return iterations;
    }

    // n is total nr of neighbours, impact is a scalar that multiplies the difference (small impact small change, big impact big change)
    private ArrayList<Neighbor> getNeighbors(double vx, double vy, double impact, int n) {
        ArrayList<Neighbor> neighbors = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            neighbors.add(new Neighbor(vx, vy, impact, i, n));
        }
        return neighbors;
    }

    public double[] shoot() {
        System.out.println("start HillBot");
        iterations = 0;
        double[] target = course.getTargetXYR();
        double tRadius = target[2];
        double bestVx = 0;
        double bestVy = 0;
        double distanceToTarget = Double.MAX_VALUE;
        double bestDistance = Double.MAX_VALUE;

        GolfODE golfODE = new GolfODE(course);
        ShotSimulatorV2 simulator = new ShotSimulatorV2();

        double impact = 1;
        double minImpact = 0.001; // had to add this otherwise it would take forever
        int i = 0;
        while (distanceToTarget > tRadius) {
            boolean hasImproved = false;

            ArrayList<Neighbor> neighbors = getNeighbors(bestVx, bestVy, impact, 32);

            for (Neighbor neigbor : neighbors) {
                double vx = neigbor.getVelocity()[0];
                double vy = neigbor.getVelocity()[1];
                
                // make the velocities smaller if they are over the spped limit
                double speed = Math.sqrt(vx * vx + vy * vy);
                if (speed > MAX_SPEED) {
                    vx = vx / speed * MAX_SPEED;
                    vy = vy / speed * MAX_SPEED;
                }

                // get the simulation result of the neighbor
                double[] startState = { course.getStartPosition()[0], course.getStartPosition()[1], vx, vy };
                double[][] fullShotTrajectory = simulator.schoot(golfODE, new RungeKuttaSolver(), startState, 0.01);
                double[] finalState = fullShotTrajectory[fullShotTrajectory.length - 1];
                double finalX = finalState[1];
                double finalY = finalState[2];
                distanceToTarget = course.distanceToTarget(finalX, finalY);

                // keep the best neighbor
                if (distanceToTarget < bestDistance) {
                    bestDistance = distanceToTarget;
                    bestVx = vx;
                    bestVy = vy;
                    hasImproved = true;
                }

                // break if its in the hole
                if (distanceToTarget <= tRadius) break;
            }

            // System.err.println("Shot " + i);
            // System.out.println("Best vx: " + bestVx + " bestVy: " + bestVy + " distance: " + bestDistance);

            // if none of the neighbours are better than the original shot, cut the impact and try again
            if (!hasImproved) {
                impact /= 2.0; // cut the impact so it finds neighbors closer to the main shot
                // System.out.println("cut impact");

                // too small impact, wont make a diference anymore
                if (impact < minImpact) {
                    // System.out.println("no good neighbor found");
                    break;
                }
            }
            i++;
        }
        iterations = i;
        // System.out.println("iteration " + (i + 1));
        // System.out.println(" bestVx = " + bestVx + " bestVy = " + bestVy + " bestDistance = " + bestDistance);

        return new double[] { bestVx, bestVy };
    }
}
