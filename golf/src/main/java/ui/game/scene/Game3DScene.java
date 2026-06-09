package ui.game.scene;

/*
 * Main 3D game scene
 * Sets up the scene groups and calls helper classes for terrain, camera, obstacles, and game objects
 */

import engine.GameManager;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class Game3DScene extends SubScene {

    private final GameManager gameManager;
    private final Group rootGroup;
    private final Group worldGroup;
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    private Group terrainGroup;
    private Group obstacleGroup;
    private GameCameraController cameraController;
    private GameObjectRenderer gameObjectRenderer;
    private AimingShotController aimingShotController;

    public Game3DScene(GameManager gameManager, double width, double height) {
        super(new Group(), width, height, true, SceneAntialiasing.BALANCED);
        this.gameManager = gameManager;
        this.rootGroup = (Group) this.getRoot();
        this.worldGroup = new Group();

        this.rootGroup.getChildren().add(worldGroup);
        this.setFill(Color.web("#87CEEB"));

        setupLighting();
        cameraController = new GameCameraController(this, rootGroup, worldGroup, angleY);
        buildSmoothTerrainAndCamera();
        buildGameObjects();
    }

    private void setupLighting() {
        AmbientLight ambient = new AmbientLight(Color.color(0.6, 0.6, 0.6));
        PointLight sunLight = new PointLight(Color.WHITE);
        sunLight.setTranslateX(0);
        sunLight.setTranslateY(-300);
        sunLight.setTranslateZ(0);
        worldGroup.getChildren().addAll(ambient, sunLight);
    }

    private void buildSmoothTerrainAndCamera() {
        if (terrainGroup != null) {
            worldGroup.getChildren().remove(terrainGroup);
        }

        TerrainMeshBuilder.TerrainBuildResult terrain = TerrainMeshBuilder.build(gameManager);
        terrainGroup = terrain.terrainGroup();
        worldGroup.getChildren().add(terrainGroup);
        cameraController.configureForTerrain(terrain);
    }

    public void refreshCourseGeometry() {
        buildSmoothTerrainAndCamera();
        buildObstacleObjects();
    }

    private void buildGameObjects() {
        gameObjectRenderer = new GameObjectRenderer(gameManager);
        gameObjectRenderer.addTo(worldGroup);

        AimingArrow aimingArrow = new AimingArrow(gameManager, gameObjectRenderer.getPlayerBall());
        worldGroup.getChildren().add(aimingArrow.getView());
        aimingShotController = new AimingShotController(this, gameManager, angleY, aimingArrow);

        buildObstacleObjects();
    }

    private void buildObstacleObjects() {
        if (obstacleGroup != null) {
            worldGroup.getChildren().remove(obstacleGroup);
        }

        obstacleGroup = ObstacleRenderer3D.build(gameManager);
        worldGroup.getChildren().add(obstacleGroup);
    }

    public void updatePlayerBall(double x, double y, double h) {
        gameObjectRenderer.updatePlayerBall(x, y, h);
    }

    public void updateBotBall(double x, double y, double h) {
        gameObjectRenderer.updateBotBall(x, y, h);
    }

    public Timeline createDropInAnimation(
        boolean playerBallActive,
        double holeX,
        double holeY,
        double holeHeight,
        Runnable onFinished
    ) {
        return gameObjectRenderer.createDropInAnimation(playerBallActive, holeX, holeY, holeHeight, onFinished);
    }

    public Timeline createOutOfBoundsFallAnimation(
        boolean playerBallActive,
        double edgeX,
        double edgeY,
        double edgeHeight,
        double directionX,
        double directionY,
        Runnable onFinished
    ) {
        return gameObjectRenderer.createOutOfBoundsFallAnimation(
            playerBallActive,
            edgeX,
            edgeY,
            edgeHeight,
            directionX,
            directionY,
            onFinished
        );
    }

    public void setMultiplayerVisibility(boolean isMultiplayer) {
        gameObjectRenderer.setMultiplayerVisibility(isMultiplayer);
    }

    public void renderFlagPosition(double physX, double physY, double physHeight) {
        gameObjectRenderer.renderFlagPosition(physX, physY, physHeight);
    }

    public void setShotHandler(Consumer<double[]> shotHandler) {
        if (aimingShotController != null) {
            aimingShotController.setShotHandler(shotHandler);
        }
    }

    public void setVelocityPreviewHandler(Consumer<double[]> velocityPreviewHandler) {
        if (aimingShotController != null) {
            aimingShotController.setVelocityPreviewHandler(velocityPreviewHandler);
        }
    }
}
