public class Solver {
    public void solve(ODE equation, double y0, double tStart, double tEnd, double numberOfSamples){
        double stepSize = 1/numberOfSamples; //Convert to Delta t
        double y = y0;
        //Here we calculate t(n+1) from t(n)

        //Print start value's
        System.out.printf("Time: %.2f | Value: %.2f%n", (tStart), y0);
        for (double t = tStart; t < (tEnd-stepSize); t += stepSize) {
            // Call the interface method
            double slope = equation.getDerivative(t);
            y = y + (stepSize * slope * y);

            
            //System.out.println("Time: " + time + "|Value: " + y);
            // %.2f rounds the display to 2 decimal places
            System.out.printf("Time: %.2f | Value: %.2f%n", (t + stepSize), y);
        }
    }
}
