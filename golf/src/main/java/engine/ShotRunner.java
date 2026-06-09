package engine;

/*
 * Runs the shot simulation
 * Builds the physics ODE and asks the solver/simulator for the trajectory
 */

import domain.course.GolfCourse;
import physics.ShotSimulator;
import solvers.Solver;
import physics.GolfODE;

final class ShotRunner {
    private final GolfCourse course;
    private final Solver solver;
    private final ShotSimulator simulator = new ShotSimulator();

    ShotRunner(GolfCourse course, Solver solver) {
        this.course = course;
        this.solver = solver;
    }

    double[][] runShot(double currentBallX, double currentBallY, double vx, double vy) {
        double[] startState = { currentBallX, currentBallY, vx, vy };
        GolfODE physicsEngine = new GolfODE(course);
        return simulator.schoot(physicsEngine, solver, startState, course.getStepSize());
    }
}
