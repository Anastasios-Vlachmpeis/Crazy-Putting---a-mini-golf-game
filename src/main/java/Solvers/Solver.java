package Solvers;

import Systems.ODE;

public interface Solver {
    public double[][] solve(ODE equation, double[] y0, double tStart, double tEnd, double h);
}
