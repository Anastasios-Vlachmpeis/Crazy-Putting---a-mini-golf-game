package Obstacles

public class Tree implements ObstacleObjects {
    private double centerX;
    private double centerY;
    private double radius;

    public Tree() {}
    
    public Tree(double centerX, double centerY, double radius) {
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
