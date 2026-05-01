package Bots;

import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulator;


// Rule-based bot, with the following rules:
// 1 ~ If the ball is already inside the scoring radius, we don't need to shoot
// 2 ~ If we are relatively close to the hole, we decrease our speed
// 3 ~ If steepnes is meaningful enough, we adjust the speed based on the slope along the putt
// 4 ~ If velocity magnitude still exceeds v_max, we scale vx vy down to v_max

public class SimpleBot extends GolfBot {

    // max initial speed based on manual
    private static final double MAX_SPEED = 5.0;

    // A skew of the shot direction towards the slope direction
    private static final double SLOPE_AIM_SKEW = 0.15;

    // When the distance-to-hole is under this multiple of target radius, we decrease our speed
    private static final double NEAR_HOLE_RING_MULTI = 3.0;

    // Skews for speed based on the slope in the direction of the shot
    private static final double UPHILL_SPEED_SKEW = 1.15;
    private static final double DOWNHILL_SPEED_SKEW = 0.92;

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
        double r = target[2];

        //we calculate the direction vector from start to target
        double dx = tx - sx;
        double dy = ty - sy;
        double len = Math.hypot(dx, dy);

        // Rule 0 ~ If distance to target is very much zero, we don't need to shoot
        if (len < 1e-12) { //if start and target are the same point
            return new double[] {0.0, 0.0};//we return a 0 vector
        }

        // Rule 1 ~ If the ball is already inside the scoring radius, we don't need to shoot
        if (len <= r) {
            return new double[] {0.0, 0.0};
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

        double nx = 0.0;
        double ny = 0.0;
        boolean haveDownhill = false;

        double[] dh = course.getDerivative(sx, sy);
        double steepness = Math.hypot(dh[0], dh[1]); // "steepness" of the surface

        // "Aim" rule (gets its own category cause it's a touch more complex than the others)
        // ~ If the green is not flat here, then we skew our aim using downhill direction and lateral break,
        // otherwise we leave our aim toward hole unchanged
        if (steepness > 1e-12) { //same condition as before -> if surface flat, don't change
            haveDownhill = true; //we now know there is a downhill direction
            nx = -dh[0] / steepness;
            ny = -dh[1] / steepness;
            double dot = nx * ux + ny * uy; //dot product of downhill and to-hole
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
        double speed = Math.min(MAX_SPEED, len * 0.2); 

        // Rule 2 ~ If we are relatively close to the hole,
        // we soften speed so our putt is "gentler"
        if (r > 1e-12 && len < NEAR_HOLE_RING_MULTI * r) {
            speed *= len / (NEAR_HOLE_RING_MULTI * r);
        }

        // Rule 3 ~ If steepnes is meaningful enough,
        // we adjust the speed based on the slope along the putt      
        if (haveDownhill) {
            double along = nx * ux + ny * uy; 
            if (along < -1e-9) {
                speed *= UPHILL_SPEED_SKEW;
            } else if (along > 1e-9) {
                speed *= DOWNHILL_SPEED_SKEW;
            }
        }

        speed = Math.min(MAX_SPEED, speed); //final capping of the speed

        double vx = ux * speed;
        double vy = uy * speed;

        // Rule 4 ~ If velocity magnitude still exceeds v_max,
        // we scale vx vy down to v_max
        double vMag = Math.hypot(vx, vy);
        if (vMag > MAX_SPEED && vMag > 1e-12) {
            double s = MAX_SPEED / vMag;
            vx *= s;
            vy *= s;
        }

        //Won't simulate any shots for this bot, (yet)
        //as it's a simple bot that just aims and shoots.
        //Might add it to check for shots in water or collisions
        //with obstacles that might be introduced in the future
        //ShotSimulation simulation = shotSimulator().simulate(vx, vy);

        return new double[] {vx, vy};
    }
}
