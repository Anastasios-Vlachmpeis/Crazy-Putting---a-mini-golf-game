/*
package Obstacles;
// Circular water hazard object.
// A ball inside this circle is considered to be in water and receives the water penalty.
/
public class Water implements ObstacleObjects {

    private double centerX;
    private double centerY;
    private double radius;
    private double displayHeight = -0.25;

    //Required for Gson JSON loading.
    public Water() {}

    // Creates a circular water hazard.
    public Water(double centerX, double centerY, double radius) {
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
        return true;
    }

    @Override
    public double getKineticFriction(double defaultMiuK) {
        return defaultMiuK;
    }

    @Override
    public double getStaticFriction(double defaultMiuS) {
        return defaultMiuS;
    }

    @Override
    public double getDisplayHeight(double terrainHeight) {
        return displayHeight;
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
}
*/
