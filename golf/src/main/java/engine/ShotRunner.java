package engine;

/*
 * Runs the shot simulation
 * Builds the physics ODE and asks the solver/simulator for the trajectory
 */

import domain.course.GolfCourse;
import physics.ShotSimulatorV2;
import solvers.Solver;
import physics.GolfODE;

final class ShotRunner {
    private final GolfCourse course;
    private final Solver solver;
    private final ShotSimulatorV2 simulator = new ShotSimulatorV2();

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
