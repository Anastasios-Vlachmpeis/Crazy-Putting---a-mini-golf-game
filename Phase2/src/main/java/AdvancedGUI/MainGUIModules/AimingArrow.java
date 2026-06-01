package AdvancedGUI.MainGUIModules;

import GameEngine.GameManager;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class AimingArrow {

    private static final double ARROW_LENGTH_SCALE = 0.25;

    private final GameManager gameManager;
    private final Sphere ballNode;
    private final Group view;
    private final Cylinder shaft;
    private final Sphere tip;

    public AimingArrow(GameManager gameManager, Sphere ballNode) {
        this.gameManager = gameManager;
        this.ballNode = ballNode;

        shaft = new Cylinder(0.035, 1.0);
        shaft.setMaterial(new PhongMaterial(Color.DARKGRAY));

        tip = new Sphere(0.12);
        tip.setMaterial(new PhongMaterial(Color.RED));

        view = new Group(shaft, tip);
        view.setVisible(false);
    }

    public Group getView() {
        return view;
    }

    public void update(double vx, double vy) {
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed < 0.01) {
            hide();
            return;
        }

        double startX = ballNode.getTranslateX();
        double startZ = ballNode.getTranslateZ();
        double startY = ballNode.getTranslateY();

        double endX = startX + vx * ARROW_LENGTH_SCALE;
        double endZ = startZ + vy * ARROW_LENGTH_SCALE;
        double endY = -gameManager.getTerrainHeight(endX, endZ) - 0.1;

        Point3D start = new Point3D(startX, startY, startZ);
        Point3D end = new Point3D(endX, endY, endZ);
        positionShaft(start, end);

        tip.setTranslateX(endX);
        tip.setTranslateY(endY);
        tip.setTranslateZ(endZ);

        view.setVisible(true);
    }

    public void hide() {
        view.setVisible(false);
    }

    private void positionShaft(Point3D start, Point3D end) {
        Point3D direction = end.subtract(start);
        double length = direction.magnitude();

        shaft.setHeight(length);
        shaft.getTransforms().clear();

        Point3D midpoint = start.midpoint(end);
        shaft.getTransforms().add(new Translate(midpoint.getX(), midpoint.getY(), midpoint.getZ()));

        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D rotationAxis = yAxis.crossProduct(direction);
        double angle = yAxis.angle(direction);

        if (rotationAxis.magnitude() > 0.0001) {
            shaft.getTransforms().add(new Rotate(angle, rotationAxis));
        }
    }
}
