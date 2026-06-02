// FUNCTIONALITY TEST for THE SIMPLEBOT
package Experiment;

import Bots.*;
import GolfCourseData.*;
import ShotEngine.ShotSimulatorV2;
import Solvers.RungeKuttaSolver;
import Systems.GolfODE;

//Experiment to test the rule based bot on the generated course
public class BotCourseExperiment {

    //Max shots to prevent infinite loops
    private static final int MAX_SHOTS = 100;

    public static void main(String[] args) {
        GolfCourse course = new GolfCourse(0, 0);
        GolfODE golfODE = new GolfODE(course);
        ShotSimulatorV2 simulator = new ShotSimulatorV2();

        RungeKuttaSolver solver = new RungeKuttaSolver();
        //PhysicsShotSimulator shotSim = new PhysicsShotSimulator(course, solver);
        //SimpleBot bot = new SimpleBot(course, shotSim);
        SimpleBot bot = new SimpleBot(course, solver);

        double[] target = course.getTargetXYR();
        double holeR = target[2];

        System.out.println("Rule based bot experiment 1");
        System.out.printf("Starting position: (%.4f, %.4f)%n",course.getStartPosition()[0], course.getStartPosition()[1]);
        System.out.printf("Target position: (%.4f, %.4f, %.4f)%n%n", target[0], target[1], holeR);

        boolean finished = false;
        //Loop through the shots until the ball is in the hole or out of bounds or the max shots is reached
        for (int shot = 1; shot <= MAX_SHOTS; shot++) {
            double[] v = bot.shoot();
            double speed = Math.hypot(v[0], v[1]);
            if (speed < 1e-12) {
                System.out.printf("Shot %d: zero velocity — stopping.%n", shot);
                finished = true;
                break;
            }

            System.out.printf("Shot %d: vx=%.4f vy=%.4f |v|=%.4f m/s%n", shot, v[0], v[1], speed);
            /* 
            ShotSimulation result = shotSim.simulate(v[0], v[1]);
            double fx = result.finalX();
            double fy = result.finalY();
            */
            // Set up the starting state with your velocity inputs (v[0] and v[1])
            double[] startState = { course.getStartPosition()[0], course.getStartPosition()[1], v[0], v[1] };

            // Ask the solver to calculate the entire shot
            //double[][] fullShotTrajectory = solver.solveBall(ode, startState, 0.01);
            double[][] fullShotTrajectory = simulator.schoot(golfODE, new RungeKuttaSolver(), startState, 0.01);

            // Get the very last row of the array (where the ball stopped)
            double[] finalState = fullShotTrajectory[fullShotTrajectory.length - 1];

            // Extract the final X and Y coordinates
            // Note: Index 0 is time (t), Index 1 is X, Index 2 is Y
            double fx = finalState[1];
            double fy = finalState[2];
            double dist = course.distanceToTarget(fx, fy);

            System.out.printf("Lands at (%.4f, %.4f) dist to hole %.4f m%n", fx, fy, dist);
            /* 
            if (result.outOfBounds()) {
                System.out.println("Ball is out of bounds");
                finished = true;
                break;
            }
            */
            if (dist <= holeR) {
                System.out.println("GAME");
                finished = true;
                break;
            }

            //For when we add water
            // if (result.inWater()) {
            //     System.out.println("In water — round over.");
            //     finished = true;
            //     break;
            // }

            course.setBallPosition(fx, fy);
        }

        if (!finished) {
            System.out.println("Max shots reached without holing out or going out of bounds");
        }
    }
}
