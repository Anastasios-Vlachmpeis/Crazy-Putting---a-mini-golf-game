package Experiment;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import Solvers.EulerSolver;
import Systems.ODE;

// Same structure as RungeKuttaExperiment, but uses the Euler solver and writes to euler_results.csv
public class EulerExperiment {
    public static void main(String[] args) {

        EulerSolver solver = new EulerSolver();
        ODE ode = new ExponentialODE(); // dy/dt = y, exact solution: y(t) = e^t

        double[] y0 = {1.0};    // initial condition y(0) = 1
        double tStart = 0.0;
        double tEnd = 1.0;
        double[] stepSizes = {0.5, 0.2, 0.1, 0.05, 0.025, 0.01, 0.005, 0.001};
        int repeats = 1000;

        try (PrintWriter writer = new PrintWriter(new FileWriter("euler_results.csv"))) {
            writer.println("h,steps,numerical,exact,error,runtime_ms");

            for (double h : stepSizes) {
                double[][] results = solver.solve(ode, y0, tStart, tEnd, h);

                // Euler stores [time, state] per row, so index [1] is used instead of [0] as in RK4
                double numerical = results[results.length - 1][1];
                double exact = Math.E;
                double error = Math.abs(numerical - exact);
                int steps = (int) ((tEnd - tStart) / h);

                long start = System.nanoTime();
                for (int i = 0; i < repeats; i++) {
                    solver.solve(ode, y0, tStart, tEnd, h);
                }
                long end = System.nanoTime();
                double runtimeMs = (end - start) / 1_000_000.0 / repeats;

                writer.printf(java.util.Locale.US,
                    "%.6f,%d,%.10f,%.10f,%.10f,%.10f%n",
                    h, steps, numerical, exact, error, runtimeMs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}