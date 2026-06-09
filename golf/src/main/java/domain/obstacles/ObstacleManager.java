package domain.obstacles;

/*
 * Manages the course obstacles
 * It adds, clears, returns, and finds sand pits, trees, and walls.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import domain.course.GolfCourse;
import domain.obstacles.ObstacleObjects;
import domain.obstacles.Sand;
import domain.obstacles.Tree;
import domain.obstacles.Wall;

public final class ObstacleManager {
    private final List<Sand> sandPits;
    private final List<Tree> trees;
    private final List<Wall> walls;
    private final ObstaclePlacementValidator placementValidator;

    public ObstacleManager(GolfCourse course, List<Sand> sandPits, List<Tree> trees, List<Wall> walls) {
        this.sandPits = sandPits;
        this.trees = trees;
        this.walls = walls;
        this.placementValidator = new ObstaclePlacementValidator(course, trees, walls);
    }

    public boolean addSandPit(double centerX, double centerY, double radius) {
        if (!canPlaceSandPit(centerX, centerY, radius)) {
            return false;
        }
        sandPits.add(new Sand(centerX, centerY, radius));
        return true;
    }

    public boolean addTree(double centerX, double centerY, double radius) {
        if (!canPlaceTree(centerX, centerY, radius)) {
            return false;
        }
        trees.add(new Tree(centerX, centerY, radius));
        return true;
    }

    public boolean addWall(double startX, double startY, double endX, double endY, double thickness, double height) {
        if (!canPlaceWall(startX, startY, endX, endY, thickness)) {
            return false;
        }
        walls.add(new Wall(startX, startY, endX, endY, thickness, height));
        return true;
    }

    public boolean canPlaceSandPit(double centerX, double centerY, double radius) {
        return placementValidator.canPlaceSandPit(centerX, centerY, radius);
    }

    public boolean canPlaceTree(double centerX, double centerY, double radius) {
        return placementValidator.canPlaceTree(centerX, centerY, radius);
    }

    public boolean canPlaceWall(double startX, double startY, double endX, double endY, double thickness) {
        return placementValidator.canPlaceWall(startX, startY, endX, endY, thickness);
    }

    public void removeObstaclesInWater() {
        sandPits.removeIf(sand -> !canPlaceSandPit(sand.getCenterX(), sand.getCenterY(), sand.getRadius()));
        trees.removeIf(tree -> !canPlaceTree(tree.getCenterX(), tree.getCenterY(), tree.getRadius()));
        walls.removeIf(wall -> !canPlaceWall(
            wall.getStartX(), wall.getStartY(), wall.getEndX(), wall.getEndY(), wall.getThickness()));
    }

    public void clearSandPits() {
        sandPits.clear();
    }

    public void clearTrees() {
        trees.clear();
    }

    public void clearWalls() {
        walls.clear();
    }

    public List<Sand> getSandPits() {
        return Collections.unmodifiableList(sandPits);
    }

    public List<Tree> getTrees() {
        return Collections.unmodifiableList(trees);
    }

    public List<Wall> getWalls() {
        return Collections.unmodifiableList(walls);
    }

    public List<ObstacleObjects> getObstacles() {
        List<ObstacleObjects> all = new ArrayList<>();
        all.addAll(sandPits);
        all.addAll(trees);
        all.addAll(walls);
        return Collections.unmodifiableList(all);
    }

    public boolean isSand(double x, double y) {
        for (Sand sand : sandPits) {
            if (sand.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTree(double x, double y) {
        return getTreeAt(x, y) != null;
    }

    public boolean isWall(double x, double y) {
        return getWallAt(x, y) != null;
    }

    public Tree getTreeAt(double x, double y) {
        for (Tree tree : trees) {
            if (tree.contains(x, y)) {
                return tree;
            }
        }
        return null;
    }

    public Wall getWallAt(double x, double y) {
        for (Wall wall : walls) {
            if (wall.contains(x, y)) {
                return wall;
            }
        }
        return null;
    }
}
