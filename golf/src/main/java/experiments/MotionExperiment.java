package experiments;

import domain.course.*;
import physics.ShotSimulator;
import solvers.RungeKuttaSolver;
import physics.GolfODE;

public class MotionExperiment {

    public static void main(GolfCourse course) {
        double[] y0 = new double[] { 0, 0, 4, -4 };
        Ball ball = new Ball(y0);

        //double[][] results = solver.solveBall(course, ball.getState(), 1);
        GolfODE golfODE = new GolfODE(course);
        ShotSimulator simulator = new ShotSimulator();
        double[][] solution = simulator.schoot(golfODE, new RungeKuttaSolver(), ball.getState(), 1);

        //for (double[] row : results) {
        //    System.out.println(Arrays.toString(row));
        //}
    }

}
