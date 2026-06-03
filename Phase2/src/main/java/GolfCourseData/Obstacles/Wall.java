package GolfCourseData.Obstacles;

/**
 * Solid wall obstacle for the 3D course.
 *
 * The wall is represented as a thick line segment in the x-y plane. It can be
 * drawn as a raised object in the 3D view and used as a solid collision object
 * during shot simulation.
 */
public class Wall implements ObstacleObjects {

    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private double thickness;
    private double height;

    public Wall() {}

    /**
     * Creates a wall between two points.
     *
     * @param startX x-coordinate of the first endpoint
     * @param startY y-coordinate of the first endpoint
     * @param endX x-coordinate of the second endpoint
     * @param endY y-coordinate of the second endpoint
     * @param thickness horizontal wall thickness
     * @param height visual wall height above the local terrain
     */
    public Wall(double startX, double startY, double endX, double endY, double thickness, double height) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.thickness = Math.max(0.01, thickness);
        this.height = Math.max(0.01, height);
    }

    @Override
    public boolean contains(double x, double y) {
        return distanceToSegment(x, y) <= thickness / 2.0;
    }

    /**
     * Distance from point (x, y) to this wall's center line segment.
     */
    public double distanceToSegment(double x, double y) {
        double dx = endX - startX;
        double dy = endY - startY;
        double lengthSquared = dx * dx + dy * dy;

        if (lengthSquared == 0.0) {
            double px = x - startX;
            double py = y - startY;
            return Math.sqrt(px * px + py * py);
        }

        double t = ((x - startX) * dx + (y - startY) * dy) / lengthSquared;
        t = Math.max(0.0, Math.min(1.0, t));

        double closestX = startX + t * dx;
        double closestY = startY + t * dy;
        double px = x - closestX;
        double py = y - closestY;
        return Math.sqrt(px * px + py * py);
    }

    @Override
    public boolean isWater() {
        return false;
    }

    @Override
    public boolean isWall() {
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
        return terrainHeight + height;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public double getThickness() {
        return thickness;
    }

    public double getHeight() {
        return height;
    }

    public double getCenterX() {
        return (startX + endX) / 2.0;
    }

    public double getCenterY() {
        return (startY + endY) / 2.0;
    }

    public double getLength() {
        double dx = endX - startX;
        double dy = endY - startY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double getAngleDegrees() {
        return Math.toDegrees(Math.atan2(endY - startY, endX - startX));
    }
}
