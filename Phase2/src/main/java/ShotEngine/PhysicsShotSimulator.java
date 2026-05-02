package ShotEngine;

import GolfCourseData.GolfCourse;
import Solvers.Solver;
import Systems.GolfODE;

public class PhysicsShotSimulator implements ShotSimulator {

    private static final double stepsize       = 0.01;      // integration step size in seconds
    private static final double speedthreshold = 0.01;     // m/s below this we check if ball is at rest
    private static final double coursebound    = 500.0;     // course boundary in metres
    private static final int    maxsteps       = 1000000; // safety cap to prevent infinite loops

    private final GolfCourse course;
    private final Solver solver;

    public PhysicsShotSimulator(GolfCourse course, Solver solver) {
        this.course = course;
        this.solver = solver;
    }

    @Override
    public ShotSimulation simulate(double v0x, double v0y) {
        // Read starting position and friction coefficients from the course
        double[] start     = course.getStartPosition(); // [x0, y0, z0]
        double[] frictions = course.getFrictions();     // [muK, muS, ...]

        // GolfODE needs a CourseProfile build one that delegates to GolfCourse
        GolfCourse profile = new GolfCourse(frictions[0], frictions[1]);
        GolfODE ode = new GolfODE(profile);

        // Initial state vector: [x, y, vx, vy]
        double[] state = { start[0], start[1], v0x, v0y };
        double t = 0.0;

        for (int step = 0; step < maxsteps; step++) {
            //System.out.println("Check!");

            double x  = state[0];
            double y  = state[1];
            double vx = state[2];
            double vy = state[3];

            // 1. Water use GolfODE's CourseProfile as the single source of truth
            if (ode.getCourse().isWater(x, y)) {
                return ShotSimulation.landedInWater(x, y);
            }

            // 2. Out of bounds
            if (Math.abs(x) > coursebound || Math.abs(y) > coursebound) {
                return ShotSimulation.wentOutOfBounds(x, y);
            }

            // 3. Ball at rest: use GolfODE's CourseProfile for slope and friction values
            double speed = Math.sqrt(vx * vx + vy * vy);
            if (speed < speedthreshold) {
                double hx             = ode.getCourse().dhdx(x, y);
                double hy             = ode.getCourse().dhdy(x, y);
                double slopeMagnitude = Math.sqrt(hx * hx + hy * hy);

                if (ode.getCourse().getMiuS() > slopeMagnitude) {
                    return ShotSimulation.stoppedAt(x, y); // static friction holds
                }
                // slope too steep ball starts sliding, let the ODE continue
            }

            // Advance one timestep
            state = stepOnce(ode, state, t);

            //Debugging ~Stan
            //System.out.println("T: " + t);

            t += stepsize;
        }

        // Safety fallback: return wherever the ball is after maxsteps
        return ShotSimulation.stoppedAt(state[0], state[1]);
    }

    // Advances the state by one step using whichever solver was injected
    private double[] stepOnce(GolfODE ode, double[] state, double t) {
        //double[][] solution = solver.solveBall(ode, state, stepsize); // changed the solving method to solveBall
        double[][] solution = solver.solve(ode, state, t, t+stepsize, stepsize); //Using this one because Euler/RK4 and Verlet wont compile otherwise. Should not change functionality

        // solve() rows are [t, x, y, vx, vy]
        double[] lastRow   = solution[solution.length - 1];
        double[] nextState = new double[state.length];
        for (int i = 0; i < state.length; i++) {
            nextState[i] = lastRow[i + 1];
        }
        return nextState;
    }
}
