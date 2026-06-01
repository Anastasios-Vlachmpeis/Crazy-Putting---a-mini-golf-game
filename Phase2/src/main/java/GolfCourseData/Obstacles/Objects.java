package Obstacles;

/**
 * Common interface for all course objects/obstacles.
 *
 * Implementations decide whether a coordinate belongs to the object and how the
 * object should affect the ball. This keeps water, sand, and future objects
 * interchangeable for collision detection and physics updates.
 */
public interface Objects {

    /**
     * @return true when the coordinate lies inside this object.
     */
    boolean contains(double x, double y);

    /**
     * @return true for hazards that count as water and cause a penalty.
     */
    boolean isWater();

    /**
     * Returns the kinetic friction at this object.
     * Water normally returns the default because the ball is stopped by collision.
     */
    double getKineticFriction(double defaultMiuK);

    /**
     * Returns the static friction at this object.
     * Water normally returns the default because the ball is stopped by collision.
     */
    double getStaticFriction(double defaultMiuS);

    /**
     * Height used for drawing. Objects that do not change the terrain can return terrainHeight.
     */
    double getDisplayHeight(double terrainHeight);
}
