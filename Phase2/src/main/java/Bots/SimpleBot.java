package Bots;

import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulator;


// Minimal rule-based bot that aims from start towards the target with a fixed speed cap
// Uses {I need to place CourseRelatedMethods here} for geometry and slope hints
//Will function properly when a ShotSimulator is wired

public class SimpleBot extends GolfBot {

    // max initial speed based on manual
    private static final double MAX_SPEED = 5.0;

    // A skew of the shot direction towards the slope direction
    private static final double SLOPE_AIM_SKEW = 0.15;

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

        // Keep this just in case we need it later. Will use an adjusted version
        // 

        // I'm introducing a bias using local slope
        // Direction of steepest increase of height
        // double[] slope = course.getDerivative(sx, sy);
        // double gx = slope[0];
        // double gy = slope[1];
        
        // double gLen = Math.hypot(gx, gy); // - gradient magnitude
        // if (gLen > 1e-12) { //can only skip if gradient is the 0 vector (perfectly flat terrain)
        //     gx/= gLen;
        //     gy/= gLen;
        //     double blend = 0.15;
        //     ux = (1.0 - blend) *ux + blend*gx;
        //     uy = (1.0 - blend)*uy + blend* gy;
        //     double uLen = Math.hypot(ux,uy);
        //     ux/= uLen;
        //     uy/= uLen;
        // }

        double[] dh = course.getDerivative(sx, sy);
        double steepness = Math.hypot(dh[0], dh[1]); // "steepness" of the surface
        if (steepness > 1e-12) { //same condition as before -> if surface flat, don't change

            double nx = -dh[0] / steepness;
            double ny = -dh[1] / steepness;
            double dot = nx * ux + ny * uy; //dot product of normal and unit vectors
            //build the "sideways breal", in other words the 'component of downhill'
            // that is orthogonal to the 'to-hole' direction 
            double px = nx - dot * ux;
            double py = ny - dot * uy;
            double pLen = Math.hypot(px, py);

            if (pLen > 1e-12) { //if the sideways break is meaningful enough (no 0)
                //normalize the sideways break
                px/= pLen;
                py/= pLen;
                //form weighted averages from the "to-hole" direction and the sideways break
                ux = (1.0 - SLOPE_AIM_SKEW) * ux + SLOPE_AIM_SKEW * px;
                uy = (1.0 - SLOPE_AIM_SKEW) * uy + SLOPE_AIM_SKEW * py;
            } else {
                //same here, just using the normal vector instead of the sideways break
                ux = (1.0 - SLOPE_AIM_SKEW) * ux + SLOPE_AIM_SKEW * nx;
                uy = (1.0 - SLOPE_AIM_SKEW) * uy + SLOPE_AIM_SKEW * ny;
            }
            double uLen = Math.hypot(ux, uy);
            //final normalization of the unit vector
            if (uLen > 1e-12) {
                ux/= uLen;
                uy/= uLen;
            }
        }

        //we calculate the speed of the shot
        double speed = Math.min(MAX_SPEED, len * 0.2); // ~ cap the speed at 5m/s - use a simple function where speed is less if closer to hole
        //and then the velocity vector
        double vx = ux * speed;
        double vy = uy * speed;

        //Won't simulate any shots for this bot, (yet)
        //as it's a simple bot that just aims and shoots.
        //Might add it to check for shots in water or collisions
        //with obstacles that might be introduced in the future
        //ShotSimulation simulation = shotSimulator().simulate(vx, vy);

        return new double[] {vx, vy};
    }
}
