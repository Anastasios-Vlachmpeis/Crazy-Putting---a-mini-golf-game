package Bots;

import java.util.ArrayList;
import java.util.Random;
import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulatorV2;
import Systems.GolfODE;
import Solvers.RungeKuttaSolver;
import Solvers.Solver;

public class HillBot extends GolfBot {

    private static final double MAX_SPEED = 5.0;
    private final Random random = new Random();

    public HillBot(GolfCourse course, Solver solver) {
        super(course, solver);
    }

    // n is total nr of neighbours
    private ArrayList<Neighbor> getNeighbors(double vx, double vy, int n) {
        ArrayList<> neighbors = new ArrayList<Neighbor>();
        for (int i = 0; i < n; i++) {
            neighbors.add(new Neighbor(vx, vy, i, n));
        }
    }

    public double[] shoot() {
        System.out.println("start HillBot");
        double[] target = course.getTargetXYR();
        double tRadius = target[2];
        double bestVx = 0;
        double bestVy = 0;
        double distanceToTarget = Double.MAX_VALUE;
        double bestDistance = Double.MAX_VALUE;

        GolfODE golfODE = new GolfODE(course);
        ShotSimulatorV2 simulator = new ShotSimulatorV2();

        int i = 0;
        while (distanceToTarget > tRadius) {
            double vx = 0;
            double vy = 0;
            Arraylist<> neighbors = getNeighbors(vx, vy, 16);

            for (Neighbor neigbor : neighbors) {
                vx = neigbor.getVelocity[0];
                vy = neigbor.getVelocity[1];
                double[] startState = { course.getStartPosition()[0], course.getStartPosition()[1], vx, vy };
                double[][] fullShotTrajectory = simulator.schoot(golfODE, new RungeKuttaSolver(), startState, 0.01);
                double[] finalState = fullShotTrajectory[fullShotTrajectory.length - 1];
                double finalX = finalState[1];
                double finalY = finalState[2];
                distanceToTarget = course.distanceToTarget(finalX, finalY);
            }

            double speed = Math.sqrt(vx * vx + vy * vy);

            if (speed > MAX_SPEED) {
                vx = vx / speed * MAX_SPEED;
                vy = vy / speed * MAX_SPEED;
            }

            if (distanceToTarget < bestDistance) {
                bestDistance = distanceToTarget;
                bestVx = vx;
                bestVy = vy;
            }

            if (distanceToTarget <= tRadius)
                break;
            i++;
        }
        System.out.println("shot nr " + (i + 1));
        System.out.println(" bestVx = " + bestVx + " bestVy = " + bestVy + " bestDistance = " + bestDistance);

        return new double[] { bestVx, bestVy };
    }
}