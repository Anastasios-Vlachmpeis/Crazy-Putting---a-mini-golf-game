package Solvers;

import java.util.Arrays;
import Systems.ODE;

public class EulerSolver implements Solver {
    @Override
    public double[][] solve(ODE equation, double[] y0, double tStart, double tEnd, double h){   // Now we're storing the values from solver instead of printing them
        double stepSize = h; //Since it's just h in the GUI
        double[] y = Arrays.copyOf(y0, y0.length);// Make a copy, don't touch the original array
        //Here we calculate t(n+1) from t(n)
    
        java.util.ArrayList<double[]> results = new java.util.ArrayList<>();

        results.add(storeRow(tStart, y)); // We store the first point
        for (double t = tStart; t < tEnd; t += stepSize) {
            // Call the interface method
            double[] slope = equation.getDerivative(t, y);
            //Eulers method: new_y = old_y + difference
            for (int i = 0; i < y.length; i++) {
                y[i] = y[i] + (stepSize * slope[i]);
            }

            results.add(storeRow(t + stepSize, y)); // Here we store each new point

            // %.2f rounds the display to 2 decimal places
            System.out.println(t + " x   " + y[0]);
            System.out.println(t + "  y  " + y[1]);
            System.out.println(t + "   z " + y[2]);
            // System.out.printf("TimeY0: %.2f seconds| Value: %.2f%n", (t + stepSize), y[0]);
            // System.out.printf("TimeY1: %.2f seconds | Value: %.2f%n", (t + stepSize), y[1]);
            // System.out.printf("TimeY2: %.2f seconds | Value: %.2f%n", (t + stepSize), y[2]);
        }
        return results.toArray(new double[0][]);
        
    }
}
