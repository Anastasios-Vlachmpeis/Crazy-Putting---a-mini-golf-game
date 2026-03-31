import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// Runs the RK4 solver across multiple step sizes and saves the accuracy and runtime results to a CSV file.
public class RungeKuttaExperiment {
    public static void main(String[] args) {

        RungeKuttaSolver solver = new RungeKuttaSolver();
        ODE ode = new ExponentialODE();     // dy/dt = y, exact solution: y(t) = e^t

        double[] y0 = {1.0};    // initial condition y(0) = 1
        double tStart = 0.0;    // integration start time
        double tEnd = 1.0;  // integration end time
        double[] stepSizes = {0.5, 0.2, 0.1, 0.05, 0.025, 0.01, 0.005, 0.001}; // step sizes to test
        int repeats = 1000; // repeat each solve to get a stable average runtime

        // write results to CSV file, automatically closed after the try block
        try (PrintWriter writer = new PrintWriter(new FileWriter("rk_results.csv"))) {
            writer.println("h,steps,numerical,exact,error,runtime_ms"); // write header

            // for-loop iterates over each step size, runs the RK4 solver to compute the numerical solution,
            // calculates the error against the exact value and the average runtime,
            // then writes all of that as one row to the CSV file.
            for (double h : stepSizes) {
                double[][] results = solver.solve(ode, y0, tStart, tEnd, h);

                // RK4 solver stores only state values (no time column), so index [0] is y at the final step
                double numerical = results[results.length - 1][0];
                double exact = Math.E;  // exact solution y(1) = e
                double error = Math.abs(numerical - exact);     // absolute global error
                int steps = (int) ((tEnd - tStart) / h);    // number of steps taken

                // measure average runtime over 'repeats' runs
                long start = System.nanoTime();
                for (int i = 0; i < repeats; i++) {
                    solver.solve(ode, y0, tStart, tEnd, h);
                }
                long end = System.nanoTime();
                double runtimeMs = (end - start) / 1_000_000.0 / repeats; // convert to ms

                // write one row to the CSV for this step size
                writer.printf(java.util.Locale.US,
                    "%.6f,%d,%.10f,%.10f,%.10f,%.10f%n",
                    h, steps, numerical, exact, error, runtimeMs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}