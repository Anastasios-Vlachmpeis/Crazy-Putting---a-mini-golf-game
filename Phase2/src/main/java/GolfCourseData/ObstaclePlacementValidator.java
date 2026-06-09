package GolfCourseData;

/*
 * Checks if an obstacle can be placed
 * Checks water, course borders, and tree/wall spacing
 */

import java.util.List;

import GolfCourseData.Obstacles.Tree;
import GolfCourseData.Obstacles.Wall;

final class ObstaclePlacementValidator {
    private final GolfCourse course;
    private final List<Tree> trees;
    private final List<Wall> walls;

    ObstaclePlacementValidator(GolfCourse course, List<Tree> trees, List<Wall> walls) {
        this.course = course;
        this.trees = trees;
        this.walls = walls;
    }

    boolean canPlaceSandPit(double centerX, double centerY, double radius) {
        return isAreaDry(centerX, centerY, radius);
    }

    boolean canPlaceTree(double centerX, double centerY, double radius) {
        Tree tree = new Tree(centerX, centerY, radius);
        return isCircleWithinBounds(centerX, centerY, tree.getTrunkRadius())
            && isAreaDry(centerX, centerY, tree.getCollisionRadius())
            && isTreeTrunkClearOfWalls(tree);
    }

    boolean canPlaceWall(double startX, double startY, double endX, double endY, double thickness) {
        Wall wall = new Wall(startX, startY, endX, endY, thickness, 1.0);
        if (!isWallClearOfTreeTrunks(wall)) {
            return false;
        }

        int samples = Math.max(2, (int) Math.ceil(wall.getLength() / Math.max(0.25, thickness)));
        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            double x = startX + (endX - startX) * t;
            double y = startY + (endY - startY) * t;
            if (!isAreaDry(x, y, thickness / 2.0)) {
                return false;
            }
        }
        return true;
    }

    private boolean isTreeTrunkClearOfWalls(Tree tree) {
        for (Wall wall : walls) {
            double minClearance = tree.getTrunkRadius() + wall.getThickness() / 2.0;
            if (wall.distanceToSegment(tree.getCenterX(), tree.getCenterY()) <= minClearance) {
                return false;
            }
        }
        return true;
    }

    private boolean isWallClearOfTreeTrunks(Wall wall) {
        for (Tree tree : trees) {
            double minClearance = tree.getTrunkRadius() + wall.getThickness() / 2.0;
            if (wall.distanceToSegment(tree.getCenterX(), tree.getCenterY()) <= minClearance) {
                return false;
            }
        }
        return true;
    }

    private boolean isCircleWithinBounds(double centerX, double centerY, double radius) {
        double[] size = course.getSize();
        return centerX - radius >= size[0]
            && centerX + radius <= size[1]
            && centerY - radius >= size[2]
            && centerY + radius <= size[3];
    }

    private boolean isAreaDry(double centerX, double centerY, double radius) {
        if (course.isWater(centerX, centerY)) {
            return false;
        }

        int samples = 16;
        for (int i = 0; i < samples; i++) {
            double angle = (2.0 * Math.PI * i) / samples;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            if (course.isWater(centerX + cos * radius, centerY + sin * radius)) {
                return false;
            }

            double innerRadius = radius * 0.5;
            if (course.isWater(centerX + cos * innerRadius, centerY + sin * innerRadius)) {
                return false;
            }
        }

        return true;
    }
}
