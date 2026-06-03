package GolfCourseData;

import java.nio.file.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import GolfCourseData.Obstacles.ObstacleObjects;
import GolfCourseData.Obstacles.Sand;
import GolfCourseData.Obstacles.Tree;
import GolfCourseData.Obstacles.Wall;
import GolfCourseData.RandomTerrainGeneration.PerlinNoise;

import java.io.Reader;
import java.io.Writer;


public class GolfCourse {
    public final double epsilon = 1e-7;

    //these values are just test/default values
    private String[] friction;
    private double[] frictionValues = {0.15, 0.5, 0.3, 0.75}; //TO DO: ADD DEFAULT SAND FRICTION!!!
    private String[] target;
    private double[] targetValues = {3, 0, 0.35};
    private String[] start;
    private double[] currentBallPosition = {0, 0, 0}; //updates after every shot
    private double[] initialBallPosition = {0.0, 0.0, 0.0};
    private String terrainFormula = "1"; //Default terrain (sin(x-y)/7)+0.5"
    //Gameborders
    private double[] size = {-25,25,-25,25}; //{minX, maxX, minY, maxY}
    private double borderSteepness = 2;
    private List<Sand> sandPits = new ArrayList<>();
    private List<Tree> trees = new ArrayList<>();
    private List<Wall> walls = new ArrayList<>();

    public double stepSize = 0.01;

    //PerlinNoise terrain generation
    public boolean usePerlinNoise = false;
    double widthScale = 0.2; // Adjust the scale to make hills/ valleys wider
    double heightScale = 1.0; //Adjust height stretching
    public double noiseOffset = 0.0; // Changes the map layout completely

    public GolfCourse(){
    }

    private TerrainManipulation TerrainManipulator = new TerrainManipulation();
    //Used only in phase 2 in the old GUI
    public void loadFromFile(String filePath)throws Exception{
        Map<String, String> data = new HashMap<>();
        Files.lines(Path.of(filePath)).forEach(line -> {
            String[] parts = line.split("=");
            if (parts.length == 2) data.put(parts[0].trim(), parts[1].trim());
        });

        friction = data.get("friction").split(",");
        target = data.get("target").split(",");
        start = data.get("start").split(",");

        terrainFormula = data.get("height");

        System.out.println("New settings loaded");
        convertToDouble();
    }
    //Used only in phase 2 in the old GUI
    public void loadFromGUI(String heightFormula, String[][] inputValuesGUI) throws Exception{
        friction = inputValuesGUI[0];
        target = inputValuesGUI[1];
        start = inputValuesGUI[2];
        terrainFormula = heightFormula;

        convertToDouble();
    }
    //Better way to save data used in phase 3
    public void saveToJson(String filePath) throws Exception {
        // Create a Gson instance configured to auto-indent your output files beautifully
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
        // Open a system stream writer and serialize this entire class layout configuration at once
        try (Writer writer = Files.newBufferedWriter(Path.of(filePath))) {
            gson.toJson(this, writer);
        }
        System.out.println("Course properties successfully serialized to JSON.");
    }
    

    //Better way to save data used in phase 3
    public void loadFromJson(String filePath) throws Exception {
        Gson gson = new Gson();
        String json = Files.readString(Path.of(filePath));
    
        try (Reader reader = new java.io.StringReader(json)) {
            // Parse raw JSON text directly back into a temporary data object map
            GolfCourse loadedData = gson.fromJson(reader, GolfCourse.class);
        
            // Safely extract properties back into this active, running UI memory instance
            this.terrainFormula = loadedData.terrainFormula; 
            this.size = loadedData.size;                     
            this.targetValues = loadedData.targetValues;     
            this.frictionValues = loadedData.frictionValues; 
            this.currentBallPosition = loadedData.currentBallPosition;
            this.sandPits = loadedData.sandPits != null ? loadedData.sandPits : new ArrayList<>();
            this.trees = loadedData.trees != null ? loadedData.trees : new ArrayList<>();
            this.walls = loadedData.walls != null ? loadedData.walls : new ArrayList<>();
            loadLegacyObstacles(json);

            if (loadedData.initialBallPosition != null) {
                this.initialBallPosition = loadedData.initialBallPosition;
            } else {
                this.initialBallPosition = Arrays.copyOf(
                    loadedData.currentBallPosition,
                    loadedData.currentBallPosition.length
                );
            }       
        
            // Wipe and rebuild the active hill list seamlessly
            this.clearAllHills(); 
            if (loadedData.TerrainManipulator != null && loadedData.TerrainManipulator.getHills() != null) { 
                for (Hill h : loadedData.TerrainManipulator.getHills()) { 
                    this.addHill(h.centerX, h.centerY, h.peakHeight, h.width); 
                }
            }
            removeObstaclesInWater();
        }
        System.out.println("Course properties successfully deserialized from JSON.");
    }

    private void loadLegacyObstacles(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray legacyObstacles = root.getAsJsonArray("obstacles");
        if (legacyObstacles == null) {
            return;
        }

        sandPits.clear();
        trees.clear();
        walls.clear();

        for (JsonElement element : legacyObstacles) {
            JsonObject obstacle = element.getAsJsonObject();
            String type = obstacle.get("type").getAsString();
            double x1 = obstacle.get("x1").getAsDouble();
            double y1 = obstacle.get("y1").getAsDouble();

            switch (type) {
                case "SAND" -> sandPits.add(new Sand(x1, y1, obstacle.get("radius").getAsDouble()));
                case "TREE" -> trees.add(new Tree(x1, y1, obstacle.get("radius").getAsDouble()));
                case "WALL" -> walls.add(new Wall(
                    x1,
                    y1,
                    obstacle.get("x2").getAsDouble(),
                    obstacle.get("y2").getAsDouble(),
                    obstacle.get("thickness").getAsDouble(),
                    1.0
                ));
                default -> {
                }
            }
        }
    }

    public void clearAllHills() {
        this.TerrainManipulator.clearHills(); 
    }

    public void convertToDouble(){
        frictionValues = Arrays.stream(friction)
            .mapToDouble(Double::parseDouble)
            .toArray();

        targetValues = Arrays.stream(target)
            .mapToDouble(Double::parseDouble)
            .toArray();

        currentBallPosition = Arrays.stream(start)
            .mapToDouble(Double::parseDouble)
            .toArray();
        initialBallPosition = Arrays.copyOf(currentBallPosition, currentBallPosition.length);
    }
    
    public void fetchSize(){//For phase 2 GUI
        size = SimpleGUI.GameCanvas.sendCanvasSize();
    }

    public void setSize(double minX, double maxX, double minY, double maxY){////For phase 3 GUI
        size = new double[]{minX, maxX, minY, maxY};
    }

    public double[] getSize() {
        return this.size;
    }

    public void setTerrainFormula(String formula){
        terrainFormula = formula;
        removeObstaclesInWater();
    }

    public void addHill(double centerX, double centerY, double peakHeight, double width) {
        // Forward the parameters straight down to your existing terrain manager
        this.TerrainManipulator.addHill(centerX, centerY, peakHeight, width);
        removeObstaclesInWater();
    }

    public boolean addSandPit(double centerX, double centerY, double radius) {
        if (!canPlaceSandPit(centerX, centerY, radius)) {
            return false;
        }
        this.sandPits.add(new Sand(centerX, centerY, radius));
        return true;
    }

    public boolean addTree(double centerX, double centerY, double radius) {
        if (!canPlaceTree(centerX, centerY, radius)) {
            return false;
        }
        this.trees.add(new Tree(centerX, centerY, radius));
        return true;
    }

    public boolean addWall(double startX, double startY, double endX, double endY, double thickness, double height) {
        if (!canPlaceWall(startX, startY, endX, endY, thickness)) {
            return false;
        }
        this.walls.add(new Wall(startX, startY, endX, endY, thickness, height));
        return true;
    }

    public boolean canPlaceSandPit(double centerX, double centerY, double radius) {
        return isAreaDry(centerX, centerY, radius);
    }

    public boolean canPlaceTree(double centerX, double centerY, double radius) {
        Tree tree = new Tree(centerX, centerY, radius);
        return isAreaDry(centerX, centerY, tree.getCollisionRadius())
            && isTreeTrunkClearOfWalls(tree);
    }

    public boolean canPlaceWall(double startX, double startY, double endX, double endY, double thickness) {
        Wall wall = new Wall(startX, startY, endX, endY, thickness, 1.0);
        if (!isWallClearOfTreeTrunks(wall)) {
            return false;
        }

        int samples = Math.max(2, (int) Math.ceil(wall.getLength() / Math.max(0.25, thickness)));
        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            double x = startX + (endX - startX) * t;
            double y = startY + (endY - startY) * t;
            if (!isAreaDry(x, y, thickness / 2.0)) {
                return false;
            }
        }
        return true;
    }

    private boolean isTreeTrunkClearOfWalls(Tree tree) {
        for (Wall wall : walls) {
            double minClearance = tree.getTrunkRadius() + wall.getThickness() / 2.0;
            if (wall.distanceToSegment(tree.getCenterX(), tree.getCenterY()) <= minClearance) {
                return false;
            }
        }
        return true;
    }

    private boolean isWallClearOfTreeTrunks(Wall wall) {
        for (Tree tree : trees) {
            double minClearance = tree.getTrunkRadius() + wall.getThickness() / 2.0;
            if (wall.distanceToSegment(tree.getCenterX(), tree.getCenterY()) <= minClearance) {
                return false;
            }
        }
        return true;
    }

    public void removeObstaclesInWater() {
        sandPits.removeIf(sand -> !canPlaceSandPit(sand.getCenterX(), sand.getCenterY(), sand.getRadius()));
        trees.removeIf(tree -> !canPlaceTree(tree.getCenterX(), tree.getCenterY(), tree.getRadius()));
        walls.removeIf(wall -> !canPlaceWall(
            wall.getStartX(), wall.getStartY(), wall.getEndX(), wall.getEndY(), wall.getThickness()));
    }

    private boolean isAreaDry(double centerX, double centerY, double radius) {
        if (isWater(centerX, centerY)) {
            return false;
        }

        int samples = 16;
        for (int i = 0; i < samples; i++) {
            double angle = (2.0 * Math.PI * i) / samples;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            if (isWater(centerX + cos * radius, centerY + sin * radius)) {
                return false;
            }

            double innerRadius = radius * 0.5;
            if (isWater(centerX + cos * innerRadius, centerY + sin * innerRadius)) {
                return false;
            }
        }

        return true;
    }

    public void clearSandPits() {
        this.sandPits.clear();
    }

    public void clearTrees() {
        this.trees.clear();
    }

    public void clearWalls() {
        this.walls.clear();
    }

    public void clearObstacles() {
        clearSandPits();
        clearTrees();
        clearWalls();
    }

    public List<Sand> getSandPits() {
        return Collections.unmodifiableList(sandPits);
    }

    public List<Tree> getTrees() {
        return Collections.unmodifiableList(trees);
    }

    public List<Wall> getWalls() {
        return Collections.unmodifiableList(walls);
    }

    public List<ObstacleObjects> getObstacles() {
        List<ObstacleObjects> all = new ArrayList<>();
        all.addAll(sandPits);
        all.addAll(trees);
        all.addAll(walls);
        return Collections.unmodifiableList(all);
    }

    public boolean isSand(double x, double y) {
        for (Sand sand : sandPits) {
            if (sand.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTree(double x, double y) {
        for (Tree tree : trees) {
            if (tree.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWall(double x, double y) {
        for (Wall wall : walls) {
            if (wall.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    public Tree getTreeAt(double x, double y) {
        for (Tree tree : trees) {
            if (tree.contains(x, y)) {
                return tree;
            }
        }
        return null;
    }

    public Wall getWallAt(double x, double y) {
        for (Wall wall : walls) {
            if (wall.contains(x, y)) {
                return wall;
            }
        }
        return null;
    }

    public GolfCourse(double miuK, double miuS) {
        if (miuK != 0.0 && miuS != 0.0){
            this.frictionValues[0] = miuK; 
            this.frictionValues[1] = miuS;
        }  
    }

    public double getStepSize() {
        return this.stepSize;
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }

    public double getMiuK() {
        return frictionValues[0];
    }

    public double getMiuS() {
        return frictionValues[1];
    }

    public double getMiuK(double x, double y) {
        for (Sand sand : sandPits) {
            if (sand.contains(x, y)) {
                return frictionValues[2];
            }
        }
        return getMiuK();
    }

    public double getMiuS(double x, double y) {
        for (Sand sand : sandPits) {
            if (sand.contains(x, y)) {
                return frictionValues[3];
            }
        }
        return getMiuS();
    }

    public double getMiuKSand() {
        return frictionValues[2];
    }

    public double getMiuSSand() {
        return frictionValues[3];
    }

    public void setFrictions(double MiuK, double MiuS, double MiuKSand, double MiuSSand) {
        this.frictionValues = new double[]{MiuK, MiuS, MiuKSand, MiuSSand};
    }


    public double height(double x, double y){
        /* 
        //return calculateHeight(terrainFormula, x, y);
        return TerrainManipulator.calculateHeight(terrainFormula, x, y, targetValues);
        */
        double calculatedHeight;
        if (usePerlinNoise) {
            // The noise function returns a value between -1 and 1. We multiply by 3 to make hills taller.
            calculatedHeight = PerlinNoise.noise((x + noiseOffset) * widthScale, (y + noiseOffset) * widthScale) * heightScale + globalElevation + 0.25; 
        } else {
            // Keep your standard Exp4j formula parser here for the builder!
            calculatedHeight = TerrainManipulator.calculateHeight(terrainFormula, x, y, targetValues);
        }
        return calculatedHeight;
    }
    
    public double[] getDerivative(double x, double y){
       return new double[] {dhdx(x,y), dhdy(x, y)};
    }

    public double dhdx(double x, double y){
        //fetchSize();
        if(x < size[0]) return -1 * borderSteepness;
        if(x > size[1]) return borderSteepness;
        
        double slopeX = (TerrainManipulator.calculateHeight(terrainFormula, x + epsilon, y, targetValues) - TerrainManipulator.calculateHeight(terrainFormula, x, y, targetValues))/epsilon;
        return slopeX;
    }

    public double dhdy(double x, double y){
        //fetchSize();
        if(y < size[2]) return -1 * borderSteepness;
        if(y > size[3]) return borderSteepness;

        double slopeY = (TerrainManipulator.calculateHeight(terrainFormula, x, y + epsilon, targetValues) - TerrainManipulator.calculateHeight(terrainFormula, x, y, targetValues))/epsilon;
        return slopeY;
    }

    public boolean isWater(double x, double y) {
        return height(x, y) < 0;
    }

    public double[] getFrictions(){
       return frictionValues;
    }

    public double[] getTargetXYR(){
       return targetValues;
    }

    public void setTargetXYR(double x, double y, double r){
        targetValues[0] = x;
        targetValues[1] = y;
        targetValues[2] = r;
    }

    public double[] getStartPosition(){
        double x = currentBallPosition[0];
        double y = currentBallPosition[1];
        double height = TerrainManipulator.calculateHeight(terrainFormula, x,y, targetValues);
        return new double[] {x, y, height};
    }

    public double[] getOriginalStartPosition(){
        double x = initialBallPosition[0];
        double y = initialBallPosition[1];
        double height = TerrainManipulator.calculateHeight(terrainFormula, x,y, targetValues);
        return new double[] {x, y, height};
    }

    public void setOriginalStartPosition(double x, double y){
        initialBallPosition = new double[]{x, y, 0.0};
    }

    //Set the current ball position for the next stroke
    // Position needs to be updated before the bot shoots again
    public void setBallPosition(double x, double y) {
        currentBallPosition[0] = x;
        currentBallPosition[1] = y;
    }

    //we pass different things around in classes so we nee dthe same method twice but slightly different input
    public double distanceToTarget(double x, double y) {
        double[] target = getTargetXYR(); // [tx, ty, r]
        double dx = x - target[0];
        double dy = y - target[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceToTarget(double[] ballState) {
        return distanceToTarget(ballState[0], ballState[1]);
    }

    // Global map elevation for Perlin Noise
    private double globalElevation = 0.0;

    public void setGlobalElevation(double elevation) {
        this.globalElevation = elevation;
        removeObstaclesInWater();
    }
    
    public double getGlobalElevation() {
        return this.globalElevation;
    }
}
