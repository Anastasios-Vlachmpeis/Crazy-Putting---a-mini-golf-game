package Solvers;

import java.util.Arrays;

import Systems.GolfODE;
import Systems.ODE;

public class VerletSolver implements Solver {
    @Override
    public double[][] solve(ODE equation, double[] y0, double tStart, double tEnd, double h) {
        double stepSize = h;
        double[] y = Arrays.copyOf(y0, y0.length); // copy so we don't work with the original array

        java.util.ArrayList<double[]> results = new java.util.ArrayList<>();
        results.add(storeRow(tStart, y)); // store the initial state before we start

        // we compute the derivative once before the loop starts
        // (so at the end of each step we already have the derivative for the next one)
        double[] slope = equation.getDerivative(tStart, y);

        for (double t = tStart; t < tEnd; t += stepSize) {

            double step = Math.min(stepSize, tEnd - t); // if the last step goes past tEnd we shrink it

            // [x, y, vx, vy]    first half = positions, second half = velocities
            int n = y.length / 2;

            // half-step velocity
            double[] vHalf = new double[n]; // we first move the velocity halfway using the current acceleration
            for (int i = 0; i < n; i++) {
                vHalf[i] = y[n + i] + 0.5 * step * slope[n + i];
            }

            // full position update
            double[] yNext = new double[y.length]; // now we use that half-step velocity to move the position a full step
            for (int i = 0; i < n; i++) {
                yNext[i]     = y[i] + step * vHalf[i];
                yNext[n + i] = vHalf[i]; // temporarily storing vHalf in the velocity slots, step 4 will fix it.
            }

            // new acceleration at the new position
            double[] slopeNext = equation.getDerivative(t + step, yNext);

            // correct the velocity using the new acceleration. v_half moves position and now we finish velocity (with aceleration at new position)
            // v_next = v_half + (h/2) * a_next
            // half step before, half step after is what makes Verlet more energy efficient 

            for (int i = 0; i < n; i++) {
                yNext[n + i] = vHalf[i] + 0.5 * step * slopeNext[n + i];
            }

            y = yNext;
            slope = slopeNext; // this for reusing it in the next iteration

            results.add(storeRow(t + step, y));

            // System.out.println(t + " x   " + y[0]);
            // System.out.println(t + "  y  " + y[1]);
        }

        return results.toArray(new double[0][]);
    }

    //placeholder to make it compile
    public double[][] solveBall(GolfODE equation, double[] y0, double h){
        return new double[][] {{0},{0}}; 
    }
}