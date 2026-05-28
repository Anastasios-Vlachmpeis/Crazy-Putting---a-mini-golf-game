package Systems;

import GolfCourseData.GolfCourse;
//import Physics.*;
import Solvers.*;

public class GolfODE implements ODE {
    private final double G = 9.81; // gravitational acceleration

    private GolfCourse course;

    public GolfODE(GolfCourse course) {
        this.course = course;
    }

    public GolfCourse getCourse() {
        return course;
    }

    @Override
    public double[] getDerivative(double t, double[] y0) {
        double x = y0[0];
        double y = y0[1];
        double vx = y0[2];
        double vy = y0[3];

        double hx = course.dhdx(x, y); // slope with respect to x (aka downhill force in the direction of x)
        double hy = course.dhdy(x, y); // slope with respect to y

        double miuK = course.getMiuK(); // get the kinetif friction coeficient
        double miuS = course.getMiuS(); // and also the static one

        double speed = Math.sqrt((vx * vx) + (vy * vy)); // magnitude of the velocity vector (aka speed)
        double slopeMagnitude = Math.sqrt(hx * hx + hy * hy); // ball stays at rest if miuS is bigger than this

        double ax; // acceleration in x direction
        double ay; // in y direction

        // first check if the speed is 0 to not devide by 0 in the formula, speed is a double, it will almost never be 0, so pick a very small number
        if (speed > 0.0001) { 
            // ball is moving
            // -G * hx means if ball is going downhill, it accelerates. if uphill, ut
            // decelerates
            // miuK * G * vx / speed means the kinetic friction
            ax = -G * hx - miuK * G * vx / speed;
            ay = -G * hy - miuK * G * vy / speed;
        } else {
            // System.out.println("SlopeMag: " + slopeMagnitude + " miuS: " + miuS);

            // ball is at rest or almost at rest
            if (miuS > slopeMagnitude || slopeMagnitude == 0) { // static friction holds ball in place || no slope, so the ball cant have any acceleration
                ax = 0.0;
                ay = 0.0;
            } else {
                ax = -G * hx - miuK * G * hx / slopeMagnitude; // acelerate in the direction of the direction of the slope
                ay = -G * hy - miuK * G * hy / slopeMagnitude;
            }
        }

        return new double[] { vx, vy, ax, ay };
    }


}