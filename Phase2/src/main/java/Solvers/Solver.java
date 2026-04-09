package Solvers;

import Systems.ODE;

public interface Solver {
    public double[][] solve(ODE equation, double[] y0, double tStart, double tEnd, double h);

    public default double[] storeRow(double t, double[] y) { 
        double[] row = new double[y.length + 1];
        row[0] = t;
        for (int i = 0; i < y.length; i++) {
            row[i + 1] = y[i]; 
        }
        return row;
    }
}
