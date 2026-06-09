package AdvancedGUI.GameModules.Scene;

/*
 * Builds the 3D terrain
 * Creates the grass mesh and the flat water plane
 */

import GameEngine.GameManager;
import GolfCourseData.GolfCourse;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

final class TerrainMeshBuilder {
    private TerrainMeshBuilder() {
    }

    static TerrainBuildResult build(GameManager gameManager) {
        Group terrainGroup = new Group();
        GolfCourse course = gameManager.getCourse();
        double[] size = course.getSize();

        double minX = size[0];
        double maxX = size[1];
        double minY = size[2];
        double maxY = size[3];

        double gameWidth = maxX - minX;
        double gameHeight = maxY - minY;
        double maxDim = Math.max(gameWidth, gameHeight);

        if (maxDim <= 0) {
            maxDim = 40.0;
            minX = -20;
            maxX = 20;
            minY = -20;
            maxY = 20;
            gameWidth = 40;
            gameHeight = 40;
        }

        double step = Math.max(0.5, maxDim / 200.0);
        int cols = (int) (gameWidth / step) + 1;
        int rows = (int) (gameHeight / step) + 1;
        TriangleMesh mesh = new TriangleMesh();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double x = minX + (c * step);
                double z = minY + (r * step);
                double elevation = gameManager.getTerrainHeight(x, z);

                mesh.getPoints().addAll((float) x, (float) -elevation, (float) z);
                mesh.getTexCoords().addAll((float) CourseMaterials.getHeightShadeTextureX(elevation), 0.5f);
            }
        }

        for (int r = 0; r < rows - 1; r++) {
            for (int c = 0; c < cols - 1; c++) {
                int tl = r * cols + c;
                int tr = tl + 1;
                int bl = (r + 1) * cols + c;
                int br = bl + 1;

                mesh.getFaces().addAll(tl, tl, bl, bl, tr, tr);
                mesh.getFaces().addAll(tr, tr, bl, bl, br, br);
            }
        }

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(CourseMaterials.createGrassMaterial());
        meshView.setCullFace(CullFace.NONE);
        terrainGroup.getChildren().add(meshView);

        Box waterPlane = new Box(gameWidth, 0.1, gameHeight);
        waterPlane.setTranslateX((minX + maxX) / 2.0);
        waterPlane.setTranslateZ((minY + maxY) / 2.0);
        waterPlane.setTranslateY(0.1);

        PhongMaterial waterMat = new PhongMaterial(Color.color(0.1, 0.5, 0.9, 1.0));
        waterPlane.setMaterial(waterMat);
        terrainGroup.getChildren().add(waterPlane);

        return new TerrainBuildResult(
            terrainGroup,
            (minX + maxX) / 2.0,
            (minY + maxY) / 2.0,
            maxDim
        );
    }

    record TerrainBuildResult(Group terrainGroup, double centerX, double centerZ, double maxDim) {
    }
}
