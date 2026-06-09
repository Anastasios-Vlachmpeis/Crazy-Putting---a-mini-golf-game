package bots.helpers;

/**
 * Result of a bot physics trial, including closest-approach data needed by search bots.
 */
public final class BotTrialResult {

    public final double finalX;
    public final double finalY;
    public final boolean inWater;
    public final boolean outOfBounds;
    public final double closestDistance;
    public final double missXAtClosest;
    public final double missYAtClosest;

    public BotTrialResult(
            double  finalX, double  finalY, boolean inWater, boolean outOfBounds,
            double  closestDistance, double  missXAtClosest, double  missYAtClosest) {

        this.finalX = finalX;
        this.finalY = finalY;
        this.inWater = inWater;
        this.outOfBounds = outOfBounds;
        this.closestDistance = closestDistance;
        this.missXAtClosest = missXAtClosest;
        this.missYAtClosest = missYAtClosest;
    }

    public double stopDistance(double targetX, double targetY) {
        return Math.hypot(finalX - targetX, finalY - targetY);
    }
}
