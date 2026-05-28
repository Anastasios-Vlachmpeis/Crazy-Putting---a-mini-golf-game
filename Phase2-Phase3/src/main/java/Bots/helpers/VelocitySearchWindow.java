package Bots.helpers;

import java.util.ArrayList;

/**
 * Builds a grid of vx, vy speeds to try, aimed toward the hole
 */
public final class VelocitySearchWindow {

    private double vxStep = -1.0;
    private double vyStep = -1.0;
    private double minVx = 4.9;
    private double maxVx = -0.1;
    private double minVy = 4.9;
    private double maxVy = -0.1;

    public void orientForLayout(double startX, double startY, double targetX, double targetY) {
        vxStep = -1.0;
        vyStep = -1.0;
        minVx = 4.9;
        maxVx = -0.1;
        minVy = 4.9;
        maxVy = -0.1;

        double dx = targetX - startX;
        double dy = targetY - startY;

        if (dx< 0 && dy> 0) { flipXBounds(); }
        else if (dx> 0 && dy< 0) { flipYBounds(); }
        else if (dx< 0 && dy< 0) { flipXBounds(); flipYBounds(); }
    }

    // This builds candidates from this window's own bounds
    public ArrayList<double[]> buildCandidates(double maxSpeed) {
        return buildCandidates(vxStep, vyStep, minVx, maxVx, minVy, maxVy, maxSpeed);
    }

    // A static version of the above method that
    // builds candidates from arbitrary bounds
    public static ArrayList<double[]> buildCandidates(
            double vxStep, double vyStep, double minVx,  double maxVx,
            double minVy, double maxVy, double maxSpeed) {

        ArrayList<double[]> candidates = new ArrayList<>();

        for (double vx = minVx; vx>= maxVx; vx += vxStep) {
            for (double vy = minVy; vy>= maxVy; vy += vyStep) {

                if (Math.hypot(vx, vy)<= maxSpeed) {
                    candidates.add(new double[]{ vx, vy });
                }
            }
        }
        return candidates;
    }

    //Getters used by bots to seed their first runGridSearch call
    public double getVxStep() { return vxStep; }
    public double getVyStep() { return vyStep; }
    public double getMinVx() { return minVx; }
    public double getMaxVx() { return maxVx; }
    public double getMinVy() { return minVy; }
    public double getMaxVy() { return maxVy; }


    private void flipXBounds() {
        double left = minVx;
        double right = maxVx;
        minVx = -right;
        maxVx = -left;
    }

    private void flipYBounds() {
        double left = minVy;
        double right = maxVy;
        minVy = -right;
        maxVy = -left;
    }
}
