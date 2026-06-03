package GolfCourseData.Obstacles;

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
        double collisionRadius = getCollisionRadius();
        return dx * dx + dy * dy <= collisionRadius * collisionRadius;
    }

    @Override
    public boolean isWater() {
        return false;
    }

    @Override
    public boolean isWall() {
        return true; // is no wall but still balls shouldn't pass through
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

    public double getTrunkRadius() {
        return Math.max(0.08, radius * 0.18);
    }

    public double getCollisionRadius() {
        return getTrunkRadius() + 0.25;
    }
}
