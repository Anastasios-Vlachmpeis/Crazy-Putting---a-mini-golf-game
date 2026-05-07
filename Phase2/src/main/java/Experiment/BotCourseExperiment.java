// FUNCTIONALITY TEST for THE SIMPLEBOT



package Experiment;

import Bots.SimpleBot;
//import GolfCourseData.GeneratedCourse;
import GolfCourseData.GolfCourse;
import ShotEngine.PhysicsShotSimulator;
import ShotEngine.ShotSimulation;
import Solvers.RungeKuttaSolver;

//Experiment to test the rule based bot on the generated course
public class BotCourseExperiment {

    //Max shots to prevent infinite loops
    private static final int MAX_SHOTS = 100;

    public static void main(String[] args) {
        //GeneratedCourse gen = new GeneratedCourse();
        //double miuK = gen.courseData[0][0];
        //double miuS = gen.courseData[0][1];
        GolfCourse course = new GolfCourse(0, 0);

        RungeKuttaSolver solver = new RungeKuttaSolver();
        PhysicsShotSimulator shotSim = new PhysicsShotSimulator(course, solver);
        SimpleBot bot = new SimpleBot(course, shotSim);

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

            ShotSimulation result = shotSim.simulate(v[0], v[1]);
            double fx = result.finalX();
            double fy = result.finalY();
            double dist = course.distanceToTarget(fx, fy);

            System.out.printf("Lands at (%.4f, %.4f) dist to hole %.4f m%n", fx, fy, dist);

            if (result.outOfBounds()) {
                System.out.println("Ball is out of bounds");
                finished = true;
                break;
            }
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
