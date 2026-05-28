package ShotEngine;

import GolfCourseData.GolfCourse;
import Solvers.RungeKuttaSolver;
import Systems.GolfODE;

public class EXAMPLE {
//Example on how to fire a shot
    public void shootEXAMPLE(GolfCourse course) {
        GolfODE golfODE = new GolfODE(course);
        ShotSimulatorV2 simulator = new ShotSimulatorV2();
        
        // Define input configurations
        double[] startState = {0.0, 0.0, 14.5, 8.2}; // [x, y, vx, vy]
        double stepSize = 0.01;

        // CHOOSE YOUR ENGINE ON THE FLY BY PASSING IT DIRECTLY:
        double[][] solution = simulator.schoot(golfODE, new RungeKuttaSolver(), startState, stepSize);
        // double[][] solution = simulator.schoot(golfODE, new EulerSolver(), startState, stepSize);
        // double[][] solution = simulator.schoot(golfODE, new VerletSolver(), startState, stepSize);
    }
    
}
