package ui.game.scene;

import engine.GameManager;
import engine.GameState;
import java.util.function.Consumer;
import javafx.beans.property.DoubleProperty;
import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;

public class AimingShotController {

    private static final double MAX_SHOT_SPEED = 5.0;
    private static final double AIM_PIXELS_FOR_MAX_SPEED = 240.0;

    private final SubScene scene;
    private final GameManager gameManager;
    private final DoubleProperty cameraAngleY;
    private final AimingArrow aimingArrow;

    private Consumer<double[]> shotHandler;
    private Consumer<double[]> velocityPreviewHandler;
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

    public void setVelocityPreviewHandler(Consumer<double[]> velocityPreviewHandler) {
        this.velocityPreviewHandler = velocityPreviewHandler;
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
                if (velocityPreviewHandler != null) {
                    velocityPreviewHandler.accept(new double[] {latestAimVx, latestAimVy});
                }
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

        double screenVx = dx * (MAX_SHOT_SPEED / AIM_PIXELS_FOR_MAX_SPEED);
        double screenVy = dy * (MAX_SHOT_SPEED / AIM_PIXELS_FOR_MAX_SPEED);
        double cameraAngleRadians = Math.toRadians(cameraAngleY.get());

        double vx = screenVx * Math.cos(cameraAngleRadians) - screenVy * Math.sin(cameraAngleRadians);
        double vy = screenVx * Math.sin(cameraAngleRadians) + screenVy * Math.cos(cameraAngleRadians);
        double speed = Math.sqrt(vx * vx + vy * vy);

        if (speed > MAX_SHOT_SPEED) {
            vx = vx / speed * MAX_SHOT_SPEED;
            vy = vy / speed * MAX_SHOT_SPEED;
        }

        return new double[] {vx, vy};
    }
}
