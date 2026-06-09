package AdvancedGUI.GameModules.Scene;

/*
 * Manages the 3D game objects
 * Handles the player ball, bot ball, target hole, flag, and ball animations
 */

import GameEngine.GameManager;
import GolfCourseData.GolfCourse;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.util.Duration;

final class GameObjectRenderer {
    private final GameManager gameManager;
    private final Sphere playerBall;
    private final Sphere botBall;
    private final Cylinder targetHoleNode;
    private final Cylinder flagPoleNode;
    private final MeshView flagBannerNode;

    GameObjectRenderer(GameManager gameManager) {
        this.gameManager = gameManager;

        playerBall = new Sphere(0.25);
        playerBall.setMaterial(CourseMaterials.createLitMaterial(Color.WHITE));

        botBall = new Sphere(0.25);
        botBall.setMaterial(CourseMaterials.createLitMaterial(Color.web("#f39c12")));
        botBall.setVisible(false);

        targetHoleNode = new Cylinder(GolfCourse.FIXED_TARGET_RADIUS, 0.03);
        targetHoleNode.setMaterial(CourseMaterials.createUnlitMaterial(Color.BLACK));

        flagPoleNode = new Cylinder(0.1, 3.0);
        flagPoleNode.setMaterial(CourseMaterials.createLitMaterial(Color.BLACK));

        flagBannerNode = createFlagBanner();
        flagBannerNode.setMaterial(CourseMaterials.createUnlitMaterial(Color.RED));
    }

    void addTo(Group worldGroup) {
        worldGroup.getChildren().addAll(playerBall, botBall, targetHoleNode, flagPoleNode, flagBannerNode);
    }

    Sphere getPlayerBall() {
        return playerBall;
    }

    void updatePlayerBall(double x, double y, double h) {
        playerBall.setVisible(true);
        resetBallScale(playerBall);
        playerBall.setTranslateX(x);
        playerBall.setTranslateZ(y);
        playerBall.setTranslateY(-h - 0.25);
    }

    void updateBotBall(double x, double y, double h) {
        resetBallScale(botBall);
        botBall.setTranslateX(x);
        botBall.setTranslateZ(y);
        botBall.setTranslateY(-h - 0.25);
    }

    Timeline createDropInAnimation(
        boolean playerBallActive,
        double holeX,
        double holeY,
        double holeHeight,
        Runnable onFinished
    ) {
        Sphere ball = playerBallActive ? playerBall : botBall;
        ball.setVisible(true);

        Timeline dropTimeline = new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(ball.translateXProperty(), ball.getTranslateX()),
                new KeyValue(ball.translateZProperty(), ball.getTranslateZ()),
                new KeyValue(ball.translateYProperty(), ball.getTranslateY()),
                new KeyValue(ball.scaleXProperty(), 1.0),
                new KeyValue(ball.scaleYProperty(), 1.0),
                new KeyValue(ball.scaleZProperty(), 1.0)
            ),
            new KeyFrame(
                Duration.millis(220),
                new KeyValue(ball.translateXProperty(), holeX, Interpolator.EASE_BOTH),
                new KeyValue(ball.translateZProperty(), holeY, Interpolator.EASE_BOTH),
                new KeyValue(ball.translateYProperty(), -holeHeight - 0.22, Interpolator.EASE_BOTH)
            ),
            new KeyFrame(
                Duration.millis(520),
                new KeyValue(ball.translateYProperty(), -holeHeight + 0.16, Interpolator.EASE_IN),
                new KeyValue(ball.scaleXProperty(), 0.25, Interpolator.EASE_IN),
                new KeyValue(ball.scaleYProperty(), 0.25, Interpolator.EASE_IN),
                new KeyValue(ball.scaleZProperty(), 0.25, Interpolator.EASE_IN)
            )
        );
        dropTimeline.setOnFinished(e -> {
            ball.setVisible(false);
            if (onFinished != null) {
                onFinished.run();
            }
        });
        return dropTimeline;
    }

    Timeline createOutOfBoundsFallAnimation(
        boolean playerBallActive,
        double edgeX,
        double edgeY,
        double edgeHeight,
        double directionX,
        double directionY,
        Runnable onFinished
    ) {
        Sphere ball = playerBallActive ? playerBall : botBall;
        ball.setVisible(true);

        double directionLength = Math.sqrt(directionX * directionX + directionY * directionY);
        if (directionLength < 0.0001) {
            double[] size = gameManager.getCourse().getSize();
            double centerX = (size[0] + size[1]) / 2.0;
            double centerY = (size[2] + size[3]) / 2.0;
            directionX = edgeX - centerX;
            directionY = edgeY - centerY;
            directionLength = Math.sqrt(directionX * directionX + directionY * directionY);
        }
        if (directionLength < 0.0001) {
            directionX = 1.0;
            directionY = 0.0;
            directionLength = 1.0;
        }

        double fallDistance = 1.25;
        double endX = edgeX + (directionX / directionLength) * fallDistance;
        double endY = edgeY + (directionY / directionLength) * fallDistance;

        Timeline fallTimeline = new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(ball.translateXProperty(), edgeX),
                new KeyValue(ball.translateZProperty(), edgeY),
                new KeyValue(ball.translateYProperty(), -edgeHeight - 0.25),
                new KeyValue(ball.scaleXProperty(), 1.0),
                new KeyValue(ball.scaleYProperty(), 1.0),
                new KeyValue(ball.scaleZProperty(), 1.0)
            ),
            new KeyFrame(
                Duration.millis(650),
                new KeyValue(ball.translateXProperty(), endX, Interpolator.EASE_IN),
                new KeyValue(ball.translateZProperty(), endY, Interpolator.EASE_IN),
                new KeyValue(ball.translateYProperty(), -edgeHeight + 2.0, Interpolator.EASE_IN),
                new KeyValue(ball.scaleXProperty(), 0.25, Interpolator.EASE_IN),
                new KeyValue(ball.scaleYProperty(), 0.25, Interpolator.EASE_IN),
                new KeyValue(ball.scaleZProperty(), 0.25, Interpolator.EASE_IN)
            )
        );

        fallTimeline.setOnFinished(e -> {
            resetBallScale(ball);
            if (onFinished != null) {
                onFinished.run();
            }
        });
        return fallTimeline;
    }

    void setMultiplayerVisibility(boolean isMultiplayer) {
        botBall.setVisible(isMultiplayer);
    }

    void renderFlagPosition(double physX, double physY, double physHeight) {
        double[] target = gameManager.getCourse().getTargetXYR();
        targetHoleNode.setRadius(target[2]);
        targetHoleNode.setTranslateX(physX);
        targetHoleNode.setTranslateZ(physY);
        targetHoleNode.setTranslateY(-physHeight - 0.015);

        flagPoleNode.setTranslateX(physX);
        flagPoleNode.setTranslateZ(physY);
        flagPoleNode.setTranslateY(-physHeight - 1.25);

        flagBannerNode.setTranslateX(physX);
        flagBannerNode.setTranslateZ(physY);
        flagBannerNode.setTranslateY(-physHeight - 2.75);
    }

    private void resetBallScale(Sphere ball) {
        ball.setScaleX(1.0);
        ball.setScaleY(1.0);
        ball.setScaleZ(1.0);
    }

    private MeshView createFlagBanner() {
        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(
            0.0f, 0.0f, 0.0f,
            0.0f, 0.45f, 0.0f,
            0.8f, 0.2f, 0.0f
        );
        mesh.getTexCoords().addAll(0, 0);
        mesh.getFaces().addAll(
            0, 0, 1, 0, 2, 0,
            2, 0, 1, 0, 0, 0
        );

        MeshView flagBanner = new MeshView(mesh);
        flagBanner.setCullFace(CullFace.NONE);
        return flagBanner;
    }
}
