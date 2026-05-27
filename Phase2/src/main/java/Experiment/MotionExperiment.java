package Experiment;

import Physics.Ball;
//import Physics.CourseProfile;
import GolfCourseData.*;
import Solvers.RungeKuttaSolver;

public class MotionExperiment {

    public static void main(String[] args) {
        double[] y0 = new double[] { 0, 0, 4, -4 };
        Ball ball = new Ball(y0);
        //CourseProfile courseSettings = new CourseProfile(0.07, 0.15);
        GolfCourse courseSettings = new GolfCourse(0.07, 0.15);
        //GolfODE course = new GolfODE(courseSettings);
        RungeKuttaSolver solver = new RungeKuttaSolver();

        //double[][] results = solver.solveBall(course, ball.getState(), 1);

        //for (double[] row : results) {
        //    System.out.println(Arrays.toString(row));
        //}
    }

}
