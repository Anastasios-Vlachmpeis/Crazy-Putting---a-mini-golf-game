package GolfCourseData;

/*
 * Manages the course obstacles
 * It adds, clears, returns, and finds sand pits, trees, and walls.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import GolfCourseData.Obstacles.ObstacleObjects;
import GolfCourseData.Obstacles.Sand;
import GolfCourseData.Obstacles.Tree;
import GolfCourseData.Obstacles.Wall;

final class ObstacleManager {
    private final List<Sand> sandPits;
    private final List<Tree> trees;
    private final List<Wall> walls;
    private final ObstaclePlacementValidator placementValidator;

    ObstacleManager(GolfCourse course, List<Sand> sandPits, List<Tree> trees, List<Wall> walls) {
        this.sandPits = sandPits;
        this.trees = trees;
        this.walls = walls;
        this.placementValidator = new ObstaclePlacementValidator(course, trees, walls);
    }

    boolean addSandPit(double centerX, double centerY, double radius) {
        if (!canPlaceSandPit(centerX, centerY, radius)) {
            return false;
        }
        sandPits.add(new Sand(centerX, centerY, radius));
        return true;
    }

    boolean addTree(double centerX, double centerY, double radius) {
        if (!canPlaceTree(centerX, centerY, radius)) {
            return false;
        }
        trees.add(new Tree(centerX, centerY, radius));
        return true;
    }

    boolean addWall(double startX, double startY, double endX, double endY, double thickness, double height) {
        if (!canPlaceWall(startX, startY, endX, endY, thickness)) {
            return false;
        }
        walls.add(new Wall(startX, startY, endX, endY, thickness, height));
        return true;
    }

    boolean canPlaceSandPit(double centerX, double centerY, double radius) {
        return placementValidator.canPlaceSandPit(centerX, centerY, radius);
    }

    boolean canPlaceTree(double centerX, double centerY, double radius) {
        return placementValidator.canPlaceTree(centerX, centerY, radius);
    }

    boolean canPlaceWall(double startX, double startY, double endX, double endY, double thickness) {
        return placementValidator.canPlaceWall(startX, startY, endX, endY, thickness);
    }

    void removeObstaclesInWater() {
        sandPits.removeIf(sand -> !canPlaceSandPit(sand.getCenterX(), sand.getCenterY(), sand.getRadius()));
        trees.removeIf(tree -> !canPlaceTree(tree.getCenterX(), tree.getCenterY(), tree.getRadius()));
        walls.removeIf(wall -> !canPlaceWall(
            wall.getStartX(), wall.getStartY(), wall.getEndX(), wall.getEndY(), wall.getThickness()));
    }

    void clearSandPits() {
        sandPits.clear();
    }

    void clearTrees() {
        trees.clear();
    }

    void clearWalls() {
        walls.clear();
    }

    List<Sand> getSandPits() {
        return Collections.unmodifiableList(sandPits);
    }

    List<Tree> getTrees() {
        return Collections.unmodifiableList(trees);
    }

    List<Wall> getWalls() {
        return Collections.unmodifiableList(walls);
    }

    List<ObstacleObjects> getObstacles() {
        List<ObstacleObjects> all = new ArrayList<>();
        all.addAll(sandPits);
        all.addAll(trees);
        all.addAll(walls);
        return Collections.unmodifiableList(all);
    }

    boolean isSand(double x, double y) {
        for (Sand sand : sandPits) {
            if (sand.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    boolean isTree(double x, double y) {
        return getTreeAt(x, y) != null;
    }

    boolean isWall(double x, double y) {
        return getWallAt(x, y) != null;
    }

    Tree getTreeAt(double x, double y) {
        for (Tree tree : trees) {
            if (tree.contains(x, y)) {
                return tree;
            }
        }
        return null;
    }

    Wall getWallAt(double x, double y) {
        for (Wall wall : walls) {
            if (wall.contains(x, y)) {
                return wall;
            }
        }
        return null;
    }
}
