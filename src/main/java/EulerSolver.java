import java.util.Arrays;

public class EulerSolver {
    public double[][] integrate(ODE equation, double[] y0, double tStart, double tEnd, double h){   // Now we're storing the values from solver instead of printing them
        double stepSize = h; //Since it's just h in the GUI
        double[] y = Arrays.copyOf(y0, y0.length);// Make a copy, don't touch the original array
        //Here we calculate t(n+1) from t(n)

        java.util.ArrayList<double[]> results = new java.util.ArrayList<>();

        //Print start values
        // System.out.printf("Time: %.2f seconds | ", tStart);
        // for (int i = 0; i < y0.length; i++) {
        //     System.out.printf(" Value: %.2f", y0[i]);
        // }
        results.add(storeRow(tStart, y)); // We store the first point
        for (double t = tStart; t < (tEnd-stepSize); t += stepSize) {
            // Call the interface method
            double[] slope = equation.getDerivative(t, y);
            //Eulers method: new_y = old_y + difference
            for (int i = 0; i < y.length; i++) {
                y[i] = y[i] + (stepSize * slope[i]);
            }

            results.add(storeRow(t + stepSize, y)); // Here we store each new point

            //Print current values
            // System.out.printf("%nTime: %.2f seconds | ", (t + stepSize));
            // for (int i = 0; i < y.length; i++) {
            //     System.out.printf(" Value: %.2f", y[i]);
            // }
        }
        return results.toArray(new double[0][]);
    }

    public double[] storeRow(double t, double [] y) { // Helper method that packages time t and current state y (needed for the charts)
        double[] row = new double[y.length + 1];
        row[0] = t;
        for (int i = 0; i < y.length; i++) {
            row[i + 1] = y[i]; 
        }
        return row;
    }
}
