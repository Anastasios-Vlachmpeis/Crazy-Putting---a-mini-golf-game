package ui.game.scene;

/*
 * Draws the 3D obstacles
 * Creates sand patches, trees, and walls for the current course
 */

import engine.GameManager;
import domain.course.GolfCourse;
import domain.obstacles.Sand;
import domain.obstacles.Tree;
import domain.obstacles.Wall;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

final class ObstacleRenderer3D {
    private ObstacleRenderer3D() {
    }

    static Group build(GameManager gameManager) {
        Group obstacleGroup = new Group();
        GolfCourse course = gameManager.getCourse();
        double[] courseSize = course.getSize();

        PhongMaterial sandMat = CourseMaterials.createSandMaterial();
        PhongMaterial trunkMat = CourseMaterials.createLitMaterial(Color.web("#7a4c24"));
        PhongMaterial leafMat = CourseMaterials.createLitMaterial(Color.web("#1f6b3a"));
        PhongMaterial wallMat = new PhongMaterial(Color.web("#3f3f3f"));

        for (Sand sand : course.getSandPits()) {
            MeshView sandPatch = createSandPatch(gameManager, sand);
            sandPatch.setMaterial(sandMat);
            obstacleGroup.getChildren().add(sandPatch);
        }

        for (Tree tree : course.getTrees()) {
            double height = gameManager.getTerrainHeight(tree.getCenterX(), tree.getCenterY());
            double canopyRadius = Math.max(0.35, tree.getRadius());
            double trunkHeight = Math.max(2.0, canopyRadius * 0.75);

            Cylinder trunk = new Cylinder(tree.getTrunkRadius(), trunkHeight);
            trunk.setTranslateX(tree.getCenterX());
            trunk.setTranslateZ(tree.getCenterY());
            trunk.setTranslateY(-height - trunkHeight / 2.0);
            trunk.setMaterial(trunkMat);

            Sphere canopy = new Sphere(canopyRadius);
            canopy.setTranslateX(tree.getCenterX());
            canopy.setTranslateZ(tree.getCenterY());
            canopy.setTranslateY(-height - trunkHeight - canopyRadius);
            canopy.setMaterial(leafMat);

            obstacleGroup.getChildren().addAll(trunk, canopy);
        }

        for (Wall wall : course.getWalls()) {
            Group wallNode = createClippedWall(gameManager, wall, courseSize, wallMat);
            if (wallNode != null) {
                obstacleGroup.getChildren().add(wallNode);
            }
        }

        return obstacleGroup;
    }

    private static Group createClippedWall(GameManager gameManager, Wall wall, double[] courseSize, PhongMaterial wallMat) {
        double[] clippedLine = clipLineToCourseBounds(
            wall.getStartX(),
            wall.getStartY(),
            wall.getEndX(),
            wall.getEndY(),
            courseSize
        );
        if (clippedLine == null) {
            return null;
        }

        double startX = clippedLine[0];
        double startY = clippedLine[1];
        double endX = clippedLine[2];
        double endY = clippedLine[3];
        double dx = endX - startX;
        double dy = endY - startY;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.001) {
            return null;
        }

        int samples = Math.max(2, (int) Math.ceil(length / 0.5));
        double groundHeight = Double.POSITIVE_INFINITY;
        double angleDegrees = -Math.toDegrees(Math.atan2(dy, dx));

        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            double sampleX = startX + dx * t;
            double sampleY = startY + dy * t;
            groundHeight = Math.min(groundHeight, gameManager.getTerrainHeight(sampleX, sampleY));
        }

        double centerX = (startX + endX) / 2.0;
        double centerY = (startY + endY) / 2.0;
        Box wallNode = new Box(length, wall.getHeight(), wall.getThickness());
        wallNode.setMaterial(wallMat);
        wallNode.setTranslateX(centerX);
        wallNode.setTranslateY(-groundHeight - wall.getHeight() / 2.0);
        wallNode.setTranslateZ(centerY);
        wallNode.setRotationAxis(Rotate.Y_AXIS);
        wallNode.setRotate(angleDegrees);

        Group wallGroup = new Group();
        wallGroup.getChildren().add(wallNode);
        return wallGroup;
    }

    private static double[] clipLineToCourseBounds(double startX, double startY, double endX, double endY, double[] courseSize) {
        double dx = endX - startX;
        double dy = endY - startY;
        double minT = 0.0;
        double maxT = 1.0;

        double[] p = {-dx, dx, -dy, dy};
        double[] q = {
            startX - courseSize[0],
            courseSize[1] - startX,
            startY - courseSize[2],
            courseSize[3] - startY
        };

        for (int i = 0; i < p.length; i++) {
            if (p[i] == 0.0) {
                if (q[i] < 0.0) {
                    return null;
                }
                continue;
            }

            double t = q[i] / p[i];
            if (p[i] < 0.0) {
                minT = Math.max(minT, t);
            } else {
                maxT = Math.min(maxT, t);
            }

            if (minT > maxT) {
                return null;
            }
        }

        return new double[]{
            startX + minT * dx,
            startY + minT * dy,
            startX + maxT * dx,
            startY + maxT * dy
        };
    }

    private static MeshView createSandPatch(GameManager gameManager, Sand sand) {
        int rings = 8;
        int segments = 48;
        double liftAboveTerrain = 0.12;
        TriangleMesh mesh = new TriangleMesh();
        double[] courseSize = gameManager.getCourse().getSize();

        double centerHeight = gameManager.getTerrainHeight(sand.getCenterX(), sand.getCenterY());
        mesh.getPoints().addAll(
            (float) sand.getCenterX(),
            (float) (-centerHeight - liftAboveTerrain),
            (float) sand.getCenterY()
        );
        mesh.getTexCoords().addAll(0.5f, 0.5f);

        for (int ring = 1; ring <= rings; ring++) {
            double ringRadius = sand.getRadius() * ring / rings;
            for (int segment = 0; segment < segments; segment++) {
                double angle = (2.0 * Math.PI * segment) / segments;
                double angleCos = Math.cos(angle);
                double angleSin = Math.sin(angle);
                double x = CourseMaterials.clamp(sand.getCenterX() + angleCos * ringRadius, courseSize[0], courseSize[1]);
                double z = CourseMaterials.clamp(sand.getCenterY() + angleSin * ringRadius, courseSize[2], courseSize[3]);
                double height = gameManager.getTerrainHeight(x, z);

                mesh.getPoints().addAll(
                    (float) x,
                    (float) (-height - liftAboveTerrain),
                    (float) z
                );
                float textureU = (float) (0.5 + angleCos * ring / (2.0 * rings));
                float textureV = (float) (0.5 + angleSin * ring / (2.0 * rings));
                mesh.getTexCoords().addAll(textureU, textureV);
            }
        }

        for (int segment = 0; segment < segments; segment++) {
            int nextSegment = (segment + 1) % segments;
            int current = sandMeshIndex(1, segment, segments);
            int next = sandMeshIndex(1, nextSegment, segments);
            mesh.getFaces().addAll(0, 0, current, current, next, next);
        }

        for (int ring = 2; ring <= rings; ring++) {
            for (int segment = 0; segment < segments; segment++) {
                int nextSegment = (segment + 1) % segments;

                int innerCurrent = sandMeshIndex(ring - 1, segment, segments);
                int innerNext = sandMeshIndex(ring - 1, nextSegment, segments);
                int outerCurrent = sandMeshIndex(ring, segment, segments);
                int outerNext = sandMeshIndex(ring, nextSegment, segments);

                mesh.getFaces().addAll(innerCurrent, innerCurrent, outerCurrent, outerCurrent, innerNext, innerNext);
                mesh.getFaces().addAll(innerNext, innerNext, outerCurrent, outerCurrent, outerNext, outerNext);
            }
        }

        MeshView sandPatch = new MeshView(mesh);
        sandPatch.setCullFace(CullFace.NONE);
        return sandPatch;
    }

    private static int sandMeshIndex(int ring, int segment, int segments) {
        return 1 + ((ring - 1) * segments) + segment;
    }
}
