package AdvancedGUI.GameModules.Scene;

import GameEngine.GameManager;
import GolfCourseData.GolfCourse;
import GolfCourseData.Obstacles.Sand;
import GolfCourseData.Obstacles.Tree;
import GolfCourseData.Obstacles.Wall;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial; //how material looks 
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.shape.CullFace;
import javafx.util.Duration;
import java.util.function.Consumer;

public class Game3DScene extends SubScene {

    private final GameManager gameManager;
    private final Group rootGroup;
    private final Group worldGroup; 
    
    // 3D Nodes
    private Group terrainGroup;
    private Group obstacleGroup;
    private PerspectiveCamera camera;
    private Sphere playerBall;
    private Sphere botBall;
    private Cylinder targetHoleNode;
    private Cylinder flagPoleNode;
    private MeshView flagBannerNode;
    private AimingShotController aimingShotController;

    // Mouse rotation tracking
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);
    private double zoomMultiplier = 1.0;
    private double cameraCenterX;
    private double cameraCenterZ;
    private double cameraMaxDim;

    public Game3DScene(GameManager gameManager, double width, double height) {
        super(new Group(), width, height, true, SceneAntialiasing.BALANCED);
        this.gameManager = gameManager;
        this.rootGroup = (Group) this.getRoot();
        
        this.worldGroup = new Group();
        this.rootGroup.getChildren().add(worldGroup);
        
        this.setFill(Color.web("#87CEEB")); // Shifted to a lighter Sky Blue
        
        setupLighting();
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
        if (camera != null) {
            rootGroup.getChildren().remove(camera);
        }
        worldGroup.getTransforms().clear();

        terrainGroup = new Group();
        GolfCourse course = gameManager.getCourse();
        double[] size = course.getSize(); 
        
        double minX = size[0];
        double maxX = size[1];
        double minY = size[2];
        double maxY = size[3];

        double gameWidth = maxX - minX;
        double gameHeight = maxY - minY;
        double maxDim = Math.max(gameWidth, gameHeight);
        
        if (maxDim <= 0) { maxDim = 40.0; minX = -20; maxX = 20; minY = -20; maxY = 20; gameWidth = 40; gameHeight = 40; }

        double step = Math.max(0.5, maxDim / 200.0); // Increase 200 for higher 3D resolution
        int cols = (int) (gameWidth / step) + 1;
        int rows = (int) (gameHeight / step) + 1;

        TriangleMesh mesh = new TriangleMesh();

        // A. Create the vertices (The points in space)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double x = minX + (c * step);
                double z = minY + (r * step);
                double elevation = gameManager.getTerrainHeight(x, z);
                
                // -elevation because JavaFX Y goes down, but we want mountains to go up
                mesh.getPoints().addAll((float)x, (float)-elevation, (float)z); 
                mesh.getTexCoords().addAll((float)getHeightShadeTextureX(elevation), 0.5f);
            }
        }

        // B. Connect the vertices into triangles
        for (int r = 0; r < rows - 1; r++) {
            for (int c = 0; c < cols - 1; c++) {
                int tl = r * cols + c;          // Top-Left vertex
                int tr = tl + 1;                // Top-Right vertex
                int bl = (r + 1) * cols + c;    // Bottom-Left vertex
                int br = bl + 1;                // Bottom-Right vertex

                // Triangle 1: Top-Left -> Bottom-Left -> Top-Right
                mesh.getFaces().addAll(tl, tl, bl, bl, tr, tr);
                // Triangle 2: Top-Right -> Bottom-Left -> Bottom-Right
                mesh.getFaces().addAll(tr, tr, bl, bl, br, br);
            }
        }

        // Apply a green material to the terrain
        MeshView meshView = new MeshView(mesh);
        PhongMaterial grassMat = new PhongMaterial();
        grassMat.setDiffuseMap(createHeightShadeTexture());
        meshView.setMaterial(grassMat);
        //Proper culling
        meshView.setCullFace(CullFace.NONE);
        terrainGroup.getChildren().add(meshView);

        // A single giant flat blue box placed exactly at height 0.0. 
        // If the green mesh dips below 0.0, the blue water covers it naturally
        Box waterPlane = new Box(gameWidth, 0.1, gameHeight);
        waterPlane.setTranslateX((minX + maxX) / 2.0);
        waterPlane.setTranslateZ((minY + maxY) / 2.0);
        waterPlane.setTranslateY(0.1); // Slightly sunken so 0.0 height grass doesn't flicker
        
        // Make the water slightly translucent
        PhongMaterial waterMat = new PhongMaterial(Color.color(0.1, 0.5, 0.9, 1.0)); 
        waterPlane.setMaterial(waterMat);
        terrainGroup.getChildren().add(waterPlane);

        worldGroup.getChildren().add(terrainGroup);

        cameraCenterX = (minX + maxX) / 2.0;
        cameraCenterZ = (minY + maxY) / 2.0;
        cameraMaxDim = maxDim;

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(maxDim * 5.0); 
        
        updateCameraZoom();
        camera.getTransforms().add(new Rotate(-35, Rotate.X_AXIS)); 
        
        this.setCamera(camera);
        rootGroup.getChildren().add(camera);

        Rotate yRotate = new Rotate(0, cameraCenterX, 0, cameraCenterZ, Rotate.Y_AXIS);
        worldGroup.getTransforms().add(yRotate);

        yRotate.angleProperty().bind(angleY);

        this.setOnScroll(event -> {
            double zoomStep = event.getDeltaY() > 0 ? 0.9 : 1.1;
            zoomMultiplier = clamp(zoomMultiplier * zoomStep, 0.35, 2.5);
            updateCameraZoom();
            event.consume();
        });
    }

    public void refreshCourseGeometry() {
        buildSmoothTerrainAndCamera();
        buildObstacleObjects();
    }

    private void updateCameraZoom() {
        if (camera == null) return;

        camera.setTranslateX(cameraCenterX);
        camera.setTranslateZ(cameraCenterZ - (cameraMaxDim * 1.2 * zoomMultiplier));
        // GODS view
        camera.setTranslateY(-(cameraMaxDim * 0.8 * zoomMultiplier));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double getHeightShadeTextureX(double elevation) {
        double greenIntensity = 0.4 + (elevation * 0.05);
        greenIntensity = clamp(greenIntensity, 0.1, 0.9);
        return (greenIntensity - 0.1) / 0.8;
    }

    private WritableImage createHeightShadeTexture() {
        int width = 256;
        WritableImage image = new WritableImage(width, 1);
        PixelWriter writer = image.getPixelWriter();

        for (int x = 0; x < width; x++) {
            double greenIntensity = 0.1 + (x / (double)(width - 1)) * 0.8;
            writer.setColor(x, 0, Color.color(0.2, greenIntensity, 0.2));
        }

        return image;
    }

    private void buildGameObjects() {
        //player ball = white
        playerBall = new Sphere(0.25);
        playerBall.setMaterial(new PhongMaterial(Color.WHITE));
        //ballMat.setSpecularColor(Color.LIGHTGRAY);
        //ballNode.setMaterial(ballMat);

        botBall = new Sphere(0.095);//little bit smaller to prevent clipping
        botBall.setMaterial(new PhongMaterial(Color.web("#f39c12")));
        botBall.setVisible(false);//make invisible in case of singleplayer

        targetHoleNode = new Cylinder(GolfCourse.FIXED_TARGET_RADIUS, 0.03);
        targetHoleNode.setMaterial(createUnlitMaterial(Color.BLACK));

        flagPoleNode = new Cylinder(0.1, 3.0); 
        flagPoleNode.setMaterial(new PhongMaterial(Color.BLACK));

        flagBannerNode = createFlagBanner();
        PhongMaterial flagBannerMat = createUnlitMaterial(Color.RED);
        flagBannerNode.setMaterial(flagBannerMat);

        worldGroup.getChildren().addAll(playerBall, botBall, targetHoleNode, flagPoleNode, flagBannerNode);

        AimingArrow aimingArrow = new AimingArrow(gameManager, playerBall);
        worldGroup.getChildren().add(aimingArrow.getView());
        aimingShotController = new AimingShotController(this, gameManager, angleY, aimingArrow);

        buildObstacleObjects();
    }

    private void buildObstacleObjects() {
        if (obstacleGroup != null) {
            worldGroup.getChildren().remove(obstacleGroup);
        }

        obstacleGroup = new Group();
        GolfCourse course = gameManager.getCourse();
        double[] courseSize = course.getSize();

        PhongMaterial sandMat = createSandMaterial();
        PhongMaterial trunkMat = new PhongMaterial(Color.web("#7a4c24"));
        PhongMaterial leafMat = new PhongMaterial(Color.web("#1f6b3a"));
        PhongMaterial wallMat = new PhongMaterial(Color.web("#3f3f3f"));

        for (Sand sand : course.getSandPits()) {
            MeshView sandPatch = createSandPatch(sand);
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
            Group wallNode = createClippedWall(wall, courseSize, wallMat);
            if (wallNode != null) {
                obstacleGroup.getChildren().add(wallNode);
            }
        }

        worldGroup.getChildren().add(obstacleGroup);
    }

    private Group createClippedWall(Wall wall, double[] courseSize, PhongMaterial wallMat) {
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

    private double[] clipLineToCourseBounds(double startX, double startY, double endX, double endY, double[] courseSize) {
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

    private MeshView createSandPatch(Sand sand) {
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
                double x = clamp(sand.getCenterX() + angleCos * ringRadius, courseSize[0], courseSize[1]);
                double z = clamp(sand.getCenterY() + angleSin * ringRadius, courseSize[2], courseSize[3]);
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

    private int sandMeshIndex(int ring, int segment, int segments) {
        return 1 + ((ring - 1) * segments) + segment;
    }

    private PhongMaterial createSandMaterial() {
        PhongMaterial material = new PhongMaterial(Color.web("#cfae62"));
        material.setDiffuseMap(createSandTexture());
        material.setSpecularColor(Color.web("#5f4f2d"));
        return material;
    }

    private WritableImage createSandTexture() {
        int size = 96;
        WritableImage image = new WritableImage(size, size);
        PixelWriter writer = image.getPixelWriter();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double grain = pseudoNoise(x, y);
                double ripple = 0.5 + 0.5 * Math.sin((x * 0.22) + (y * 0.08));
                double shade = clamp((grain * 0.75) + (ripple * 0.25), 0.0, 1.0);

                int red = (int) (180 + shade * 35);
                int green = (int) (145 + shade * 30);
                int blue = (int) (70 + shade * 22);
                writer.setColor(x, y, Color.rgb(red, green, blue));
            }
        }

        return image;
    }

    private double pseudoNoise(int x, int y) {
        int value = x * 374761393 + y * 668265263;
        value = (value ^ (value >> 13)) * 1274126177;
        value = value ^ (value >> 16);
        return (value & 0xffff) / 65535.0;
    }

    public void updatePlayerBall(double x, double y, double h) {
        playerBall.setVisible(true);
        resetBallScale(playerBall);
        playerBall.setTranslateX(x);
        playerBall.setTranslateZ(y);
        playerBall.setTranslateY(-h - 0.25);
    }

    public void updateBotBall(double x, double y, double h) {
        resetBallScale(botBall);
        botBall.setTranslateX(x);
        botBall.setTranslateZ(y);
        botBall.setTranslateY(-h - 0.1);
    }

    public Timeline createDropInAnimation(
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

    public Timeline createOutOfBoundsFallAnimation(
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

    private void resetBallScale(Sphere ball) {
        ball.setScaleX(1.0);
        ball.setScaleY(1.0);
        ball.setScaleZ(1.0);
    }

    public void setMultiplayerVisibility(boolean isMultiplayer) {
        botBall.setVisible(isMultiplayer);
    }

    public void renderFlagPosition(double physX, double physY, double physHeight) {
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
        flagBannerNode.setTranslateY(-physHeight - 2.5);
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

    private PhongMaterial createUnlitMaterial(Color color) {
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(color);
        material.setSelfIlluminationMap(createSolidColorTexture(color));
        return material;
    }

    private WritableImage createSolidColorTexture(Color color) {
        WritableImage image = new WritableImage(1, 1);
        image.getPixelWriter().setColor(0, 0, color);
        return image;
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
