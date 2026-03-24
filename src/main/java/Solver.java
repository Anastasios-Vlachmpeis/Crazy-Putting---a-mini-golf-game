import java.util.Arrays;

public class Solver {
    public void integrate(ODE equation, double[] y0, double tStart, double tEnd, double samplesPerSecond){
        double stepSize = 1/samplesPerSecond; //Convert to Delta t
        double[] y = Arrays.copyOf(y0, y0.length);// Make a copy, don't touch the original array
        //Here we calculate t(n+1) from t(n)

        //Print start values
        System.out.printf("Time: %.2f seconds | ", tStart);
        for (int i = 0; i < y0.length; i++) {
            System.out.printf(" Value: %.2f", y0[i]);
        }
        for (double t = tStart; t < (tEnd-stepSize); t += stepSize) {
            // Call the interface method
            double[] slope = equation.getDerivative(t, y);
            //Eulers method: new_y = old_y + difference
            for (int i = 0; i < y.length; i++) {
                y[i] = y[i] + (stepSize * slope[i]);
            }

            //Print current values
            System.out.printf("%nTime: %.2f seconds | ", (t + stepSize));
            for (int i = 0; i < y.length; i++) {
                System.out.printf(" Value: %.2f", y[i]);
            }
        }
    }
}
