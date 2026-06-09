package ui.game.scene;

/*
 * Controls the 3D camera
 * Sets the camera position and handles mouse scroll zooming
 */

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.PickResult;
import javafx.scene.transform.Rotate;

final class GameCameraController {
    private final SubScene scene;
    private final Group rootGroup;
    private final Group worldGroup;
    private final DoubleProperty angleY;

    private PerspectiveCamera camera;
    private double zoomMultiplier = 1.0;
    private double cameraFocusX;
    private double cameraFocusZ;
    private double cameraMaxDim;

    GameCameraController(SubScene scene, Group rootGroup, Group worldGroup, DoubleProperty angleY) {
        this.scene = scene;
        this.rootGroup = rootGroup;
        this.worldGroup = worldGroup;
        this.angleY = angleY;
    }

    void configureForTerrain(TerrainMeshBuilder.TerrainBuildResult terrain) {
        if (camera != null) {
            rootGroup.getChildren().remove(camera);
        }
        worldGroup.getTransforms().clear();

        cameraFocusX = terrain.centerX();
        cameraFocusZ = terrain.centerZ();
        cameraMaxDim = terrain.maxDim();

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(cameraMaxDim * 5.0);
        updateCameraZoom();
        camera.getTransforms().add(new Rotate(-35, Rotate.X_AXIS));

        scene.setCamera(camera);
        rootGroup.getChildren().add(camera);

        Rotate yRotate = new Rotate(0, terrain.centerX(), 0, terrain.centerZ(), Rotate.Y_AXIS);
        worldGroup.getTransforms().add(yRotate);
        yRotate.angleProperty().bind(angleY);

        scene.setOnScroll(event -> {
            Point3D zoomAnchor = getWorldPointUnderCursor(event);
            double oldZoomMultiplier = zoomMultiplier;
            double zoomStep = event.getDeltaY() > 0 ? 0.9 : 1.1;
            zoomMultiplier = CourseMaterials.clamp(zoomMultiplier * zoomStep, 0.35, 2.5);

            if (zoomAnchor != null && zoomMultiplier != oldZoomMultiplier) {
                double zoomRatio = zoomMultiplier / oldZoomMultiplier;
                cameraFocusX = zoomAnchor.getX() - ((zoomAnchor.getX() - cameraFocusX) * zoomRatio);
                cameraFocusZ = zoomAnchor.getZ() - ((zoomAnchor.getZ() - cameraFocusZ) * zoomRatio);
            }

            updateCameraZoom();
            event.consume();
        });
    }

    private void updateCameraZoom() {
        if (camera == null) return;

        camera.setTranslateX(cameraFocusX);
        camera.setTranslateZ(cameraFocusZ - (cameraMaxDim * 1.2 * zoomMultiplier));
        camera.setTranslateY(-(cameraMaxDim * 0.8 * zoomMultiplier));
    }

    private Point3D getWorldPointUnderCursor(javafx.scene.input.ScrollEvent event) {
        PickResult pickResult = event.getPickResult();
        if (pickResult == null || pickResult.getIntersectedNode() == null || pickResult.getIntersectedPoint() == null) {
            return null;
        }

        Point3D scenePoint = pickResult.getIntersectedNode().localToScene(pickResult.getIntersectedPoint());
        return worldGroup.sceneToLocal(scenePoint);
    }
}
