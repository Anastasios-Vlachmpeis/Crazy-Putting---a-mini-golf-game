package GolfCourseData;

/*
 * Stores the course borders
 * Easier than always using a double array with minX, maxX, minY, maxY
 */

import java.util.Arrays;

public final class CourseBounds {
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    private CourseBounds(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public static CourseBounds of(double minX, double maxX, double minY, double maxY) {
        return new CourseBounds(minX, maxX, minY, maxY);
    }

    public static CourseBounds fromArray(double[] values) {
        if (values == null || values.length < 4) {
            throw new IllegalArgumentException("Course bounds must contain minX, maxX, minY, maxY.");
        }
        return new CourseBounds(values[0], values[1], values[2], values[3]);
    }

    public double minX() {
        return minX;
    }

    public double maxX() {
        return maxX;
    }

    public double minY() {
        return minY;
    }

    public double maxY() {
        return maxY;
    }

    public boolean contains(double x, double y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public double[] toArray() {
        return Arrays.copyOf(new double[] {minX, maxX, minY, maxY}, 4);
    }
}
