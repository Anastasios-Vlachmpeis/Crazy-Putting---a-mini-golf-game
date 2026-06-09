package experiments;

import solvers.VerletSolver;
import physics.ODE;

// test to check how accurate the verlet solver is
// we use the harmonic oscillator (x'' + x = 0)  to test because it has an exact solution

public class VerletExperiment {
    public static void main(String[] args) {

        VerletSolver solver = new VerletSolver();
        ODE ode = (t, y) -> new double[]{ y[1], -y[0] }; // x'' + x = 0, exact: cos(t)
                                                         // since we know that x(t) = cos(t), we can directly measure the error
        double[] y0 = {1.0, 0.0}; // x(0)=1, vx(0)=0
        double tStart = 0.0;
        double tEnd = 1.0;
        double exact = Math.cos(tEnd);

        double[] stepSizes = {0.5, 0.1, 0.05, 0.01, 0.001};

        System.out.println("h          error");
        System.out.println("------------------");

        for (double h : stepSizes) {
            double[][] result = solver.solve(ode, y0, tStart, tEnd, h);
            double numerical = result[result.length - 1][1]; // x at tEnd
            double error = Math.abs(numerical - exact);
            System.out.printf("h=%.4f    error=%.2e%n", h, error);
        }

        // Error shrinks as h gets smaller. Proves that the solver works.
    }
}
