package GolfCourseData.Obstacles;

/**
 * Circular sand pit object.
 * Sand does not cause a penalty, but increases kinetic and static friction while
 * the ball is inside the circle.
 */
public class Sand implements ObstacleObjects {

    private double centerX;
    private double centerY;
    private double radius;
    private final double miuK = 0.35;
    private final double miuS = 0.65;

    /** Required for Gson JSON loading. */
    public Sand() {}

    /** Creates a circular sand pit. */
    public Sand(double centerX, double centerY, double radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = Math.max(0.0, radius);
    }

    @Override
    public boolean contains(double x, double y) {
        double dx = x - centerX;
        double dy = y - centerY;
        return dx * dx + dy * dy <= radius * radius;
    }

    @Override
    public boolean isWater() {
        return false;
    }

    @Override
    public boolean isWall() {
        return false;
    }

    @Override
    public double getKineticFriction(double defaultMiuK) {
        return miuK > 0.0 ? miuK : defaultMiuK;
    }

    @Override
    public double getStaticFriction(double defaultMiuS) {
        return miuS > 0.0 ? miuS : defaultMiuS;
    }

    @Override
    public double getDisplayHeight(double terrainHeight) {
        return terrainHeight;
    }
    
    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getRadius() {
        return radius;
    }

    public double getSandMiuK() {
        return miuK;
    }

    public double getSandMiuS() {
        return miuS;
    }
}
