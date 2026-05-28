package Bots;

import java.util.Random;

import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulatorV2;
//import ShotEngine.ShotSimulation;
//import ShotEngine.ShotSimulator;
import Systems.GolfODE;
import Solvers.RungeKuttaSolver;
import Solvers.Solver;

public class MLBot extends GolfBot {

    private static final double MAX_SPEED = 5.0;
    private final Random random = new Random();
    /* 
    public MLBot(GolfCourse course, ShotSimulator shotSimulator) {
        super(course, shotSimulator);
    }
    */
    public MLBot(GolfCourse course, Solver solver) {
        super(course, solver);
    }

    public double[] shoot() {
        System.out.println("start MLBot");
        double[] target = course.getTargetXYR();
        double tRadius = target[2];
        double bestVx = 0;
        double bestVy = 0;
        double distanceToTarget = Double.MAX_VALUE;
        double bestDistance = Double.MAX_VALUE;

        GolfODE golfODE = new GolfODE(course);
        ShotSimulatorV2 simulator = new ShotSimulatorV2();

        double exploreChance = 0.2; // chance to explore the map randomly to hpefully get a better shot

        // usually takes between 1-10k shots to get result but sometimes more than 40k
        
        int i = 0;
        while (distanceToTarget > tRadius) {
            //System.out.println("shot nr " + (i + 1));

            double vx;
            double vy;

            // chance to explor e the map in a random direction rather than learn from the
            // best shot si far, in order to maybe find a better path
            // first shot doesnt have other shots to learn from so its also random
            if (i < 20 || random.nextDouble() < exploreChance) {
                double angle = random.nextDouble() * 2 * Math.PI; // get direction randomly based on a unit circle
                double speed = random.nextDouble() * MAX_SPEED; // get random speed, max <= 5

                vx = speed * Math.cos(angle);
                vy = speed * Math.sin(angle);

                //System.out.println("random exploration shot");
            } else { // learn from the best shot so far
                vx = bestVx + (random.nextDouble() * 2 - 1);  // change vx a little bit
                vy = bestVy + (random.nextDouble() * 2 - 1);// change vy a little but

                double speed = Math.sqrt(vx * vx + vy * vy);

                // if the speed is too big now, reduce it to the maximum allowed, keeping the ratio relatively the same
                if (speed > MAX_SPEED) {
                    vx = vx / speed * MAX_SPEED;
                    vy = vy / speed * MAX_SPEED;
                }

                //System.out.println("learned shot");
            }
            /* 
            ShotSimulation sim = shotSimulator.simulate(vx, vy); // simulate the shot

            // get results from simulation
            double finalX = sim.finalX();
            double finalY = sim.finalY();
            
            distanceToTarget = course.distanceToTarget(finalX, finalY);
            //System.out.println("distanceToHole = " + distanceToTarget);
            */

            double[] startState = { course.getStartPosition()[0], course.getStartPosition()[1], vx, vy };
            //Using newer and better optimized shotengine
            //double[][] fullShotTrajectory = solver.solveBall(ode, startState, 0.01);
            double[][] fullShotTrajectory = simulator.schoot(golfODE, new RungeKuttaSolver(), startState, 0.01);
            double[] finalState = fullShotTrajectory[fullShotTrajectory.length - 1];
            double finalX = finalState[1];
            double finalY = finalState[2];

            distanceToTarget = course.distanceToTarget(finalX, finalY);
            
            if (distanceToTarget < bestDistance) {
                bestDistance = distanceToTarget;
                bestVx = vx;
                bestVy = vy;
            }

            if (distanceToTarget <= tRadius) {
                //System.out.println("Found shot inside target radius");
                break;
            }
            i++;
        }
        System.out.println("shot nr " + (i + 1));
        System.out.println(" bestVx = " + bestVx + " bestVy = " + bestVy + " bestDistance = " + bestDistance);

        return new double[] { bestVx, bestVy };
    }
}