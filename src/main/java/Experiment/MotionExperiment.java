package Experiment;

import java.util.Arrays;

import Physics.Ball;
import Physics.CourseProfile;
import Solvers.RungeKuttaSolver;
import Systems.GolfODE;

public class MotionExperiment {
    public static void main(String[] args) {
        double[] y0 = new double[] { 3, 2, 4, 3 };
        Ball ball = new Ball(y0);
        CourseProfile courseSettings = new CourseProfile(0.07, 0.15);
        GolfODE course = new GolfODE(courseSettings);
        RungeKuttaSolver solver = new RungeKuttaSolver();

        double[][] results = solver.solveBall(course, ball.getState(), 0.5);

        // for (double[] row : results) {
        //     System.out.println(Arrays.toString(row));
        // }
    }

}
