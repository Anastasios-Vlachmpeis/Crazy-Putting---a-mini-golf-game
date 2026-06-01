package AdvancedGUI.MainGUIModules;

import GameEngine.GameManager;
import GameEngine.GameState;
import java.util.function.Consumer;
import javafx.beans.property.DoubleProperty;
import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;

public class AimingShotController {

    private static final double MAX_SHOT_SPEED = 15.0;
    private static final double AIM_PIXELS_FOR_MAX_SPEED = 240.0;

    private final SubScene scene;
    private final GameManager gameManager;
    private final DoubleProperty cameraAngleY;
    private final AimingArrow aimingArrow;

    private Consumer<double[]> shotHandler;
    private boolean aimingShot = false;
    private double aimStartX;
    private double aimStartY;
    private double latestAimVx;
    private double latestAimVy;
    private double rotationStartX;
    private double rotationStartAngleY;

    public AimingShotController(
        SubScene scene,
        GameManager gameManager,
        DoubleProperty cameraAngleY,
        AimingArrow aimingArrow
    ) {
        this.scene = scene;
        this.gameManager = gameManager;
        this.cameraAngleY = cameraAngleY;
        this.aimingArrow = aimingArrow;

        attachMouseHandlers();
    }

    public void setShotHandler(Consumer<double[]> shotHandler) {
        this.shotHandler = shotHandler;
    }

    private void attachMouseHandlers() {
        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && gameManager.getCurrentState() == GameState.AIMING) {
                aimStartX = event.getSceneX();
                aimStartY = event.getSceneY();
                aimingShot = true;
                latestAimVx = 0.0;
                latestAimVy = 0.0;
                aimingArrow.update(0.0, 0.0);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                rotationStartX = event.getSceneX();
                rotationStartAngleY = cameraAngleY.get();
            }
        });

        scene.setOnMouseDragged(event -> {
            if (aimingShot) {
                double[] velocity = dragToVelocity(event.getSceneX(), event.getSceneY());
                latestAimVx = velocity[0];
                latestAimVy = velocity[1];
                aimingArrow.update(latestAimVx, latestAimVy);
            } else if (event.isSecondaryButtonDown()) {
                cameraAngleY.set(rotationStartAngleY + (rotationStartX - event.getSceneX()) * 0.5);
            }
        });

        scene.setOnMouseReleased(event -> {
            if (!aimingShot || event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            aimingShot = false;
            aimingArrow.hide();

            double speed = Math.sqrt(latestAimVx * latestAimVx + latestAimVy * latestAimVy);
            if (speed > 0.01 && shotHandler != null) {
                shotHandler.accept(new double[] {latestAimVx, latestAimVy});
            }
        });
    }

    private double[] dragToVelocity(double mouseX, double mouseY) {
        double dx = mouseX - aimStartX;
        double dy = aimStartY - mouseY;

        double vx = dx * (MAX_SHOT_SPEED / AIM_PIXELS_FOR_MAX_SPEED);
        double vy = dy * (MAX_SHOT_SPEED / AIM_PIXELS_FOR_MAX_SPEED);
        double speed = Math.sqrt(vx * vx + vy * vy);

        if (speed > MAX_SHOT_SPEED) {
            vx = vx / speed * MAX_SHOT_SPEED;
            vy = vy / speed * MAX_SHOT_SPEED;
        }

        return new double[] {vx, vy};
    }
}
