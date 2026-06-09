package GolfCourseData;

/*
 * Main course class
 * Stores course settings and uses helper classes for saving, terrain, obstacles, and borders
 */

import java.util.*;

import GolfCourseData.Obstacles.ObstacleObjects;
import GolfCourseData.Obstacles.Sand;
import GolfCourseData.Obstacles.Tree;
import GolfCourseData.Obstacles.Wall;


public class GolfCourse {
    public final double epsilon = 1e-7;
    public static final double FIXED_TARGET_RADIUS = 0.35;

    //these values are just test/default values
    private String[] friction;
    private double[] frictionValues = {0.15, 0.5, 0.3, 0.75}; //TO DO: ADD DEFAULT SAND FRICTION!!!
    private String[] target;
    private double[] targetValues = {20.0, 20.0, FIXED_TARGET_RADIUS};
    private String[] start;
    private double[] currentBallPosition = {-20.0, -20.0, 0.0}; //updates after every shot
    private double[] initialBallPosition = {-20.0, -20.0, 0.0};
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
        LegacyCourseInput input = LegacyCourseInput.fromFile(filePath);
        applyLegacyInput(input);
        System.out.println("New settings loaded");
    }

    //Used only in phase 2 in the old GUI
    public void loadFromGUI(String heightFormula, String[][] inputValuesGUI) throws Exception{
        applyLegacyInput(LegacyCourseInput.fromGui(heightFormula, inputValuesGUI));
    }

    //Better way to save data used in phase 3
    public void saveToJson(String filePath) throws Exception {
        GolfCourseJsonPersistence.save(this, filePath);
        System.out.println("Course properties successfully serialized to JSON.");
    }
    

    //Better way to save data used in phase 3
    public void loadFromJson(String filePath) throws Exception {
        GolfCourseJsonPersistence.LoadedCourse loadedCourse = GolfCourseJsonPersistence.load(filePath);
        applyLoadedCourse(loadedCourse);
        System.out.println("Course properties successfully deserialized from JSON.");
    }

    private void applyLoadedCourse(GolfCourseJsonPersistence.LoadedCourse loadedCourse) {
        GolfCourse loadedData = loadedCourse.course();

        this.terrainFormula = loadedData.terrainFormula;
        this.size = loadedData.size;
        this.targetValues = loadedData.targetValues;
        normalizeTargetRadius();
        this.frictionValues = loadedData.frictionValues;
        this.currentBallPosition = loadedData.currentBallPosition;
        this.sandPits = loadedData.sandPits != null ? loadedData.sandPits : new ArrayList<>();
        this.trees = loadedData.trees != null ? loadedData.trees : new ArrayList<>();
        this.walls = loadedData.walls != null ? loadedData.walls : new ArrayList<>();

        if (loadedCourse.legacyObstacles().hasObstacles()) {
            this.sandPits = loadedCourse.legacyObstacles().sandPits();
            this.trees = loadedCourse.legacyObstacles().trees();
            this.walls = loadedCourse.legacyObstacles().walls();
        }

        if (loadedData.initialBallPosition != null) {
            this.initialBallPosition = loadedData.initialBallPosition;
        } else {
            this.initialBallPosition = Arrays.copyOf(
                loadedData.currentBallPosition,
                loadedData.currentBallPosition.length
            );
        }

        this.clearAllHills();
        if (loadedData.TerrainManipulator != null && loadedData.TerrainManipulator.getHills() != null) {
            for (Hill h : loadedData.TerrainManipulator.getHills()) {
                this.addHill(h.centerX, h.centerY, h.peakHeight, h.width);
            }
        }
        removeObstaclesInWater();
    }

    public void clearAllHills() {
        this.TerrainManipulator.clearHills(); 
    }

    private void applyLegacyInput(LegacyCourseInput input) {
        friction = input.friction();
        target = input.target();
        start = input.start();
        terrainFormula = input.terrainFormula();

        convertToDouble();
    }

    public void convertToDouble(){
        frictionValues = Arrays.stream(friction)
            .mapToDouble(Double::parseDouble)
            .toArray();

        targetValues = Arrays.stream(target)
            .mapToDouble(Double::parseDouble)
            .toArray();
        normalizeTargetRadius();

        currentBallPosition = Arrays.stream(start)
            .mapToDouble(Double::parseDouble)
            .toArray();
        initialBallPosition = Arrays.copyOf(currentBallPosition, currentBallPosition.length);
    }
    
    public void fetchSize(double[] courseBounds){//For phase 2 GUI
        setSize(courseBounds);
    }

    public void setSize(double minX, double maxX, double minY, double maxY){////For phase 3 GUI
        size = CourseBounds.of(minX, maxX, minY, maxY).toArray();
    }

    public void setSize(double[] courseBounds) {
        size = CourseBounds.fromArray(courseBounds).toArray();
    }

    public double[] getSize() {
        return this.size;
    }

    public CourseBounds getBounds() {
        return CourseBounds.fromArray(size);
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
        return obstacles().addSandPit(centerX, centerY, radius);
    }

    public boolean addTree(double centerX, double centerY, double radius) {
        return obstacles().addTree(centerX, centerY, radius);
    }

    public boolean addWall(double startX, double startY, double endX, double endY, double thickness, double height) {
        return obstacles().addWall(startX, startY, endX, endY, thickness, height);
    }

    public boolean canPlaceSandPit(double centerX, double centerY, double radius) {
        return obstacles().canPlaceSandPit(centerX, centerY, radius);
    }

    public boolean canPlaceTree(double centerX, double centerY, double radius) {
        return obstacles().canPlaceTree(centerX, centerY, radius);
    }

    public boolean canPlaceWall(double startX, double startY, double endX, double endY, double thickness) {
        return obstacles().canPlaceWall(startX, startY, endX, endY, thickness);
    }

    public void removeObstaclesInWater() {
        obstacles().removeObstaclesInWater();
    }

    public void clearSandPits() {
        obstacles().clearSandPits();
    }

    public void clearTrees() {
        obstacles().clearTrees();
    }

    public void clearWalls() {
        obstacles().clearWalls();
    }

    public void clearObstacles() {
        clearSandPits();
        clearTrees();
        clearWalls();
    }

    public List<Sand> getSandPits() {
        return obstacles().getSandPits();
    }

    public List<Tree> getTrees() {
        return obstacles().getTrees();
    }

    public List<Wall> getWalls() {
        return obstacles().getWalls();
    }

    public List<ObstacleObjects> getObstacles() {
        return obstacles().getObstacles();
    }

    public boolean isSand(double x, double y) {
        return obstacles().isSand(x, y);
    }

    public boolean isTree(double x, double y) {
        return obstacles().isTree(x, y);
    }

    public boolean isWall(double x, double y) {
        return obstacles().isWall(x, y);
    }

    public Tree getTreeAt(double x, double y) {
        return obstacles().getTreeAt(x, y);
    }

    public Wall getWallAt(double x, double y) {
        return obstacles().getWallAt(x, y);
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
        return terrain().height(x, y);
    }
    
    public double[] getDerivative(double x, double y){
       return terrain().getDerivative(x, y);
    }

    public double dhdx(double x, double y){
        return terrain().dhdx(x, y);
    }

    public double dhdy(double x, double y){
        return terrain().dhdy(x, y);
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

    public void setTargetPosition(double x, double y) {
        setTargetXYR(x, y, FIXED_TARGET_RADIUS);
    }

    private void normalizeTargetRadius() {
        if (targetValues == null || targetValues.length < 3) {
            targetValues = new double[] {20.0, 20.0, FIXED_TARGET_RADIUS};
            return;
        }
        targetValues[2] = FIXED_TARGET_RADIUS;
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

    private TerrainHeightProvider terrain() {
        return new TerrainHeightProvider(
            TerrainManipulator,
            terrainFormula,
            targetValues,
            getBounds(),
            borderSteepness,
            epsilon,
            usePerlinNoise,
            widthScale,
            heightScale,
            noiseOffset,
            globalElevation
        );
    }

    private ObstacleManager obstacles() {
        return new ObstacleManager(this, sandPits, trees, walls);
    }
}
