package Bots;

import GolfCourseData.*;
import ShotEngine.ShotSimulator;


// Minimal rule-based bot that aims from start towards the target with a fixed speed cap
// Uses {I need to place CourseRelatedMethods here} for geometry and slope hints
//Will function properly when a ShotSimulator is wired

public class SimpleBot extends GolfBot {

    // max initial speed based on manual
    private static final double MAX_SPEED = 5.0;

    public SimpleBot(GolfCourse course) {
        super(course);
    }

    public SimpleBot(GolfCourse course, ShotSimulator shotSimulator) {
        super(course, shotSimulator);
    }

    public double[] chooseNextShot() {
        //coordinates of the start and target points
        double[] start = course.getStartPosition();
        double[] target = course.getTargetXYR();
        double sx = start[0];
        double sy = start[1];
        double tx = target[0];
        double ty = target[1];

        //we calculate the direction vector from start to target
        double dx = tx - sx;
        double dy = ty - sy;
        double len = Math.hypot(dx, dy);
        if (len < 1e-12) { //if start and target are the same point
            return new double[] {0.0, 0.0};//we return a 0 vector
        }

        double ux = dx/len;
        double uy = dy/len;

        // I'm considering introducing a bias using local slope
        //
        //Direction of steepest increase of height 
        // double[] slope = course.getDerivative(sx, sy);
        // double gx = slope[0];
        // double gy = slope[1];
        //
        // double gLen = Math.hypot(gx, gy); - gradient magnitude
        // if (gLen > 1e-12) { can only skip if gradient is the 0 vector (perfectly flat terrain)
        //     gx/= gLen;
        //     gy/= gLen;
        //     double blend = 0.15;
        //     ux = (1.0 - blend) *ux + blend*gx;
        //     uy = (1.0 - blend)*uy + blend* gy;
        //     double uLen = Math.hypot(ux,uy);
        //     ux/= uLen;
        //     uy/= uLen;
        // }

        //we calculate the speed of the shot
        double speed = Math.min(MAX_SPEED, len * 0.2); // ~ cap the speed at 5m/s - use a simple function where speed is less if closer to hole
        //and then the velocity vector
        double vx = ux * speed;
        double vy = uy * speed;

        // As soon as ShotSimulator is connected, we can simulate the shot and return the result
        // shotSimulator().ifPresent(sim -> sim.simulate(vx, vy));

        return new double[] {vx, vy};
    }
}
