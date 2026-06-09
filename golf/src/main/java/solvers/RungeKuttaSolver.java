package solvers;

import java.util.Arrays;

import domain.course.GolfCourse;
//import physics.ShotSimulation;

import java.util.ArrayList;

import physics.*;

public class RungeKuttaSolver implements Solver {
    @Override
    public double[] iteration(ODE equation, double t, double[] y, double h) {

        double[] k1 = equation.getDerivative(t, y); // first derivative
        double[] yTemp = new double[y.length];
        for (int i = 0; i < y.length; i++) {
            yTemp[i] = y[i] + 0.5 * h * k1[i]; // need this for k2 = f(t + h/2, y + h * k1 * 0.5)
        }

        double[] k2 = equation.getDerivative(t + 0.5 * h, yTemp); // second derivative
        for (int i = 0; i < y.length; i++) {
            yTemp[i] = y[i] + 0.5 * h * k2[i]; // same for k3 as for k2
        }
        double[] k3 = equation.getDerivative(t + 0.5 * h, yTemp); // third derivative
        for (int i = 0; i < y.length; i++) {
            yTemp[i] = y[i] + h * k3[i]; // need this for k4 = f(t + h, y + h * k3)
        }
        double[] k4 = equation.getDerivative(t + h, yTemp); // fourth derivative

        double[] nextY = new double[y.length];
        for (int i = 0; i < y.length; i++) {
            nextY[i] = y[i] + (h / 6.0) * (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i]); // next values of each entry of y
        }

        return nextY;
    }
     
    //Used for experiments etc
    @Override
    public double[][] solve(ODE equation, double[] y0, double tStart, double tEnd, double h) {
        // formula for steps. i used ceil here in case tStart = 0, tEnd = 1, h = 0.3,
        // then (tEnd - tStart) / h) = 3.33. We cant do 3.33 steps so round up
        int steps = (int) Math.ceil((tEnd - tStart) / h);

        double[][] solution = new double[steps + 1][y0.length + 1]; // [nr of steps + original state][t, y0, y1, ...]

        double t = tStart;
        double[] y = Arrays.copyOf(y0, y0.length);// Make a copy, don't touch the original array

        // store and print initial state
        solution[0] = storeRow(t, y);

        for (int step = 1; step < solution.length; step++) { // do the solving
            y = iteration(equation, t, y, h);
            t += h;

            solution[step] = storeRow(t, y);
        }

        return solution;
    }
    /*
    //Striclty for the ball trajectory
    public double[][] solveBall(GolfODE equation, double[] y0, double h) {
        //ArrayList<double[]> solutionInList = new ArrayList<>(); // I tried something - did not work ~Stan
        double[][] solution = new double[y0.length][y0.length + 1];

        double[] targetValues = ((GolfODE) equation).getCourse().getTargetXYR();

        double t = 0.0;
        double[] y = Arrays.copyOf(y0, y0.length);

        //solutionInList.add(storeRow(t, y));
        solution[0] = storeRow(t, y);
        double miuS = equation.getCourse().getMiuS();

        int MAX_STEPS = 5_000;
        int k = 1;
        while (k < MAX_STEPS) { //don't make it dependent on size of the course since the size is dynamic 

            //System.out.println(k);
            double speed = Math.sqrt(y[2] * y[2] + y[3] * y[3]);

            double hx = equation.getCourse().dhdx(y[0], y[1]);
            double hy = equation.getCourse().dhdy(y[0], y[1]);

            double slopeMagnitude = Math.sqrt(hx * hx + hy * hy);

            // stopping point for the ball, if the speed is too small and the static
            // friction can hold the ball in place
            if (speed < 0.01 && miuS > slopeMagnitude) { // 0.01 is just a magic numbre here, idk what to put as a
                // stopping point for speed because it is a double and it will
                // almost never be 0, so i put a small number here

                // remove the balls velocity if it should stop
                y[2] = 0.0;
                y[3] = 0.0;

                if (k >= solution.length) {
                    solution = doubleArray(solution);
                }

                //solutionInList.add(storeRow(t, y));
                //solution[0] = storeRow(t, y);
                solution[k++] = storeRow(t, y);
                break; // stop the simulation
            }

            // ball is in the hole
            double distanceTarget = Math.sqrt((y[0]-targetValues[0])*(y[0]-targetValues[0])+(y[1]-targetValues[1])*(y[1]-targetValues[1]));
            //System.out.println("Distance to Hole: " + distanceTarget + " vs Required Radius: " + targetValues[2]);
            if (distanceTarget < 0.75 * targetValues[2]){
                break; // stop the simulation
            }

            // the ball didnt stop moving, go to next iteration
            y = iteration(equation, t, y, h);
            t += h;

            // double the array size if it doesnt fit. we should really transform this into
            // an arraylist to avoid this, but idk how to do one for 2d arrays
            if (k >= solution.length) {
                solution = doubleArray(solution);
            }
            
            //solutionInList.add(storeRow(t, y));
            //solution[0] = storeRow(t, y);
            solution[k++] = storeRow(t, y);
        }

        return trimArray(solution, k); // trim the array of the excess rows caused by doubleArray()
        //double[][] finalResult = solutionInList.toArray(new double[0][0]);
        //return (finalResult);
    }

    public double[][] doubleArray(double[][] arr) {
        //System.out.println("Doubling array started");
        double[][] newArr = new double[arr.length * 2][arr[0].length];

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                newArr[i][j] = arr[i][j];
            }
        }
        //System.out.println("Doubling array finished. Current length: " + newArr.length);
        return newArr;
    }

    public double[][] trimArray(double[][] arr, int usedRows) {
        double[][] trimmed = new double[usedRows][arr[0].length];

        for (int i = 0; i < usedRows; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                trimmed[i][j] = arr[i][j];
            }
        }

        return trimmed;
    }
    */
}
