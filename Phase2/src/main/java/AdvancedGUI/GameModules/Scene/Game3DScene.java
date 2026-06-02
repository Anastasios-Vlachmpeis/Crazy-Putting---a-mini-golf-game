package AdvancedGUI.GameModules.Scene;

import GameEngine.GameManager;
import GolfCourseData.GolfCourse;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.shape.CullFace;

public class Game3DScene extends SubScene {

    private final GameManager gameManager;
    private final Group rootGroup;
    private final Group worldGroup; 
    
    // 3D Nodes
    private Sphere ballNode;
    private Cylinder flagPoleNode;

    // Mouse rotation tracking
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

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
        
        if (maxDim <= 0) { maxDim = 40.0; minX = -20; maxX = 20; minY = -20; maxY = 20; gameWidth = 40; gameHeight = 40; }

        double step = Math.max(0.5, maxDim / 200.0); // Increase 200 for higher 3D resolution
        int cols = (int) (gameWidth / step) + 1;
        int rows = (int) (gameHeight / step) + 1;

        TriangleMesh mesh = new TriangleMesh();
        
        // Dummy texture coordinates required by JavaFX to render the mesh
        mesh.getTexCoords().addAll(0, 0);

        // A. Create the vertices (The points in space)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double x = minX + (c * step);
                double z = minY + (r * step);
                double elevation = gameManager.getTerrainHeight(x, z);
                
                // -elevation because JavaFX Y goes down, but we want mountains to go up
                mesh.getPoints().addAll((float)x, (float)-elevation, (float)z); 
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
                mesh.getFaces().addAll(tl, 0, bl, 0, tr, 0);
                // Triangle 2: Top-Right -> Bottom-Left -> Bottom-Right
                mesh.getFaces().addAll(tr, 0, bl, 0, br, 0);
            }
        }

        // Apply a green material to the terrain
        MeshView meshView = new MeshView(mesh);
        PhongMaterial grassMat = new PhongMaterial(Color.color(0.15, 0.65, 0.2));
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

        double centerX = (minX + maxX) / 2.0;
        double centerZ = (minY + maxY) / 2.0;

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(maxDim * 5.0); 
        
        camera.setTranslateX(centerX);
        camera.setTranslateZ(centerZ - (maxDim * 1.2));
        //GODS view 
        camera.setTranslateY(-(maxDim * 0.8));
        camera.getTransforms().add(new Rotate(-35, Rotate.X_AXIS)); 
        
        this.setCamera(camera);
        rootGroup.getChildren().add(camera);

        Rotate xRotate = new Rotate(0, centerX, 0, centerZ, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, centerX, 0, centerZ, Rotate.Y_AXIS);
        worldGroup.getTransforms().addAll(xRotate, yRotate);

        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        this.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        this.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()) * 0.5);
            angleY.set(anchorAngleY + (anchorX - event.getSceneX()) * 0.5);
        });
    }

    private void buildGameObjects() {
        ballNode = new Sphere(0.1);
        PhongMaterial ballMat = new PhongMaterial(Color.WHITE);
        ballMat.setSpecularColor(Color.LIGHTGRAY);
        ballNode.setMaterial(ballMat);

        flagPoleNode = new Cylinder(0.05, 1.0); 
        PhongMaterial flagMat = new PhongMaterial(Color.DARKRED);
        flagPoleNode.setMaterial(flagMat);

        worldGroup.getChildren().addAll(ballNode, flagPoleNode);
    }

    public void renderBallPosition(double physX, double physY, double physHeight) {
        ballNode.setTranslateX(physX);
        ballNode.setTranslateZ(physY); 
        ballNode.setTranslateY(-physHeight - 0.1); 
    }

    public void renderFlagPosition(double physX, double physY, double physHeight) {
        flagPoleNode.setTranslateX(physX);
        flagPoleNode.setTranslateZ(physY); 
        flagPoleNode.setTranslateY(-physHeight - 0.5); 
    }
}