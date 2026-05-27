package ShotEngine;

/**
Immutable result of a single shot simulation.
@param finalX      x-coordinate where the ball stopped
@param finalY      y-coordinate where the ball stopped
@param inWater     true if ball ended in water
@param outOfBounds true if ball ended out of bounds
 */
/*
public record ShotSimulation(double finalX, double finalY, boolean inWater, boolean outOfBounds) {

    //ball ended normally
    public static ShotSimulation stoppedAt(double x, double y) {
        //System.out.println("Stopped at: " + x + ", "+ y);
        return new ShotSimulation(x, y, false, false);
    }

    //ball ended in water
    public static ShotSimulation landedInWater(double x, double y) {
        return new ShotSimulation(x, y, true, false);
    }

    //ball ended out of bounds
    public static ShotSimulation wentOutOfBounds(double x, double y) {
        return new ShotSimulation(x, y, false, true);
    }
}
*/
