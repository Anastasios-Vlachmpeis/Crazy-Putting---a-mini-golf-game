package ShotEngine;

import java.util.ArrayList;
import java.util.Arrays;

import GolfCourseData.GolfCourse;
import Solvers.Solver;
import Systems.GolfODE;

public class ShotSimulatorV2 {
    
    //state {x, y, vx, vy}
    //h stepsize
    public double[][] schoot(GolfODE equation, Solver solver, double[] y0, double h){

        GolfCourse course = equation.getCourse();
        double[] targetValues = course.getTargetXYR();
        
        double t = 0.0;
        double[] state = Arrays.copyOf(y0, y0.length);

        //double[][] solution = new double[MAX_STEPS][5]; //made space for 1000 iterations
        ArrayList<double[]> trajectoryList = new ArrayList<>();
        // Store initial position before movement starts
        trajectoryList.add(solver.storeRow(t, state));

        double miuS = course.getMiuS();

        int MAX_STEPS = 5_000;//safety cap
        for (int k = 0; k < MAX_STEPS; k++){
            double speed = Math.sqrt(state[2]*state[2]+state[3]*state[3]);

            double hx = course.dhdx(state[0],state[1]);
            double hy = course.dhdy(state[0],state[1]);
            double slopeMagnitude = Math.sqrt(hx * hx + hy * hy);

            if (speed < 0.01 && miuS > slopeMagnitude) {//speed threshhold of 0.01m/s
                state[2] = 0;
                state[3] = 0;
                trajectoryList.add(solver.storeRow(t, state));
                //solution[k++] = storeRow(t, state);
                break;
            }

            // ball is in the hole
            double dx = state[0] - targetValues[0];
            double dy = state[1] - targetValues[1];
            double distanceTarget = Math.sqrt(dx * dx + dy * dy);
            if (distanceTarget < 0.75 * targetValues[2]) { 
                break; 
            }

            //continue if ball did not stop
            state = solver.iteration(equation, t, state, h); 
            t += h;
            
            trajectoryList.add(solver.storeRow(t, state));
            if (course.isWater(state[0], state[1])) {
                break;
            }
        }
        //Convert to simple array
        double[][] solution = trajectoryList.toArray(new double[0][]);
        return solution;
    }
}
