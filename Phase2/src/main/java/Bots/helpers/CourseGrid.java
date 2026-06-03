package Bots.helpers;

import GolfCourseData.GolfCourse;
import GolfCourseData.Obstacles.ObstacleObjects;

/**
 * Top down grid of the course used for pathfinding.
 * A cell is passable when it is inside the borders, not water and not too steep.
 */
public final class CourseGrid {

    public static final double DEFAULT_CELL_SIZE = 1.0;

    // slopes above this count as a wall (steep hills). can be tuned
    private static final double STEEPNESS_LIMIT = 1.0;

    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;
    private final double cellSize;
    private final int cols;
    private final int rows;
    private final boolean[][] passable;

    public CourseGrid(GolfCourse course) {
        this(course, DEFAULT_CELL_SIZE);
    }

    public CourseGrid(GolfCourse course, double cellSize) {
        double[] size = course.getSize();
        this.minX = size[0];
        this.maxX = size[1];
        this.minY = size[2];
        this.maxY = size[3];
        this.cellSize = cellSize;

        this.cols = Math.max(1, (int) Math.ceil((maxX - minX) / cellSize));
        this.rows = Math.max(1, (int) Math.ceil((maxY - minY) / cellSize));
        this.passable = new boolean[cols][rows];

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                double[] center = cellToWorld(col, row);
                passable[col][row] = isWalkable(course, center[0], center[1]);
            }
        }
    }

    public int getCols() { return cols; }
    public int getRows() { return rows; }
    public double getCellSize() { return cellSize; }

    public boolean isPassable(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return false;
        return passable[col][row];
    }

    public boolean isPassableWorld(double x, double y) {
        if (x < minX || x > maxX || y < minY || y > maxY) return false;
        int[] cell = worldToCell(x, y);
        return isPassable(cell[0], cell[1]);
    }

    // world coordinates to grid cell
    public int[] worldToCell(double x, double y) {
        int col = (int) Math.floor((x - minX) / cellSize);
        int row = (int) Math.floor((y - minY) / cellSize);
        col = clamp(col, 0, cols - 1);
        row = clamp(row, 0, rows - 1);
        return new int[] { col, row };
    }

    // grid cell to the world coordinates of its center
    public double[] cellToWorld(int col, int row) {
        double x = minX + (col + 0.5) * cellSize;
        double y = minY + (row + 0.5) * cellSize;
        return new double[] { x, y };
    }

    // decides if the ball can sit at this world point
    private static boolean isWalkable(GolfCourse course, double x, double y) {
        double[] size = course.getSize();
        if (x < size[0] || x > size[1] || y < size[2] || y > size[3]) return false;
        if (course.isWater(x, y)) return false;
        for (ObstacleObjects obstacle : course.getObstacles()) {
            if (obstacle.isWall() && obstacle.contains(x, y)) return false;
        }

        double steepness = Math.hypot(course.dhdx(x, y), course.dhdy(x, y));
        if (steepness > STEEPNESS_LIMIT) return false;

        return true;
    }

    private static int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}
