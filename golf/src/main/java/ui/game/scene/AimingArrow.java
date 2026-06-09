package ui.game.scene;

import engine.GameManager;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class AimingArrow {

    private static final double ARROW_LENGTH_SCALE = 0.25;

    private final GameManager gameManager;
    private final Sphere ballNode;
    private final Group view;
    private final Cylinder shaft;
    private final MeshView tip;

    public AimingArrow(GameManager gameManager, Sphere ballNode) {
        this.gameManager = gameManager;
        this.ballNode = ballNode;

        shaft = new Cylinder(0.035, 1.0);
        shaft.setMaterial(new PhongMaterial(Color.DARKGRAY));

        tip = createPyramidTip(0.24, 0.3);
        tip.setMaterial(new PhongMaterial(Color.DARKGRAY));

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

    private MeshView createPyramidTip(double baseSize, double height) {
        float halfBase = (float) baseSize / 2.0f;
        float tipHeight = (float) height;

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(
            0, -tipHeight, 0,
            -halfBase, 0, -halfBase,
            halfBase, 0, -halfBase,
            halfBase, 0, halfBase,
            -halfBase, 0, halfBase
        );
        mesh.getTexCoords().addAll(0, 0);
        mesh.getFaces().addAll(
            0, 0, 1, 0, 2, 0,
            0, 0, 2, 0, 3, 0,
            0, 0, 3, 0, 4, 0,
            0, 0, 4, 0, 1, 0,
            1, 0, 4, 0, 3, 0,
            1, 0, 3, 0, 2, 0
        );

        MeshView pyramid = new MeshView(mesh);
        pyramid.setCullFace(CullFace.NONE);
        return pyramid;
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
