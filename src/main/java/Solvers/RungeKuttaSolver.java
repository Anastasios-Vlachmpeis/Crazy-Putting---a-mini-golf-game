package Solvers;

import java.util.Arrays;

import Systems.ODE;

public class RungeKuttaSolver implements Solver {

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

    public double[][] solveBall(ODE equation, double[] y0, double h) {
        double[][] solution = new double[y0.length][y0.length + 1]; // [nr of steps + original state][t, y0, y1, ...]

        double t = 0.0;
        double[] y = Arrays.copyOf(y0, y0.length);// Make a copy, don't touch the original array
        
        // store and print initial state
        solution[0] = storeRow(t, y);
        System.out.println(Arrays.toString(solution[0]));

        int k = 1;
        while ((Math.abs(y[0]) > 0.001 || Math.abs(y[1]) > 0.001 ||
            Math.abs(y[2]) > 0.001 || Math.abs(y[3]) > 0.001)
            && y[0] >= 0 && y[0] <= 1000
            && y[1] >= 0 && y[1] <= 1000) {

            y = iteration(equation, t, y, h);
            t += h;

            System.out.println(k + Arrays.toString(y));
            if (k >= solution.length) {
                solution = doubleArray(solution);
            }

            solution[k++] = storeRow(t, y);
        }

        return solution;
    }

    public double[][] doubleArray(double[][] arr) {
        double[][] newArr = new double[arr.length * 2][arr[0].length];

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                newArr[i][j] = arr[i][j];
            }
        }

        return newArr;
    }
}
