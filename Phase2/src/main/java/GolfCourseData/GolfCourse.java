package GolfCourseData;

import java.nio.file.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import GolfCourseData.RandomTerrainGeneration.PerlinNoise;

import java.io.Reader;
import java.io.Writer;


public class GolfCourse {
    public final double epsilon = 1e-7;

    //these values are just test/default values
    private String[] friction;
    private double[] frictionValues = {0.15, 0.5, 0.0};
    private String[] target;
    private double[] targetValues = {3, 0, 0.2};
    private String[] start;
    private double[] currentBallPosition = {0, 0, 0}; //updates after every shot
    private double[] initialBallPosition = {0.0, 0.0, 0.0};
    private String terrainFormula = "(sin(x-y)/7)+0.5"; //Default terrain
    //Gameborders
    private double[] size = {-50,50,-50,50}; //{minX, maxX, minY, maxY}
    private double borderSteepness = 2;

    //PerlinNoise terrain generation
    public boolean usePerlinNoise = false;
    double widthScale = 0.08; // Adjust the scale to make hills/ valleys wider
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
    public void loadFromGUI(String heightFormula, String[][] inputValuesGUI)throws Exception{
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
    
        try (Reader reader = Files.newBufferedReader(Path.of(filePath))) {
            // Parse raw JSON text directly back into a temporary data object map
            GolfCourse loadedData = gson.fromJson(reader, GolfCourse.class);
        
            // Safely extract properties back into this active, running UI memory instance
            this.terrainFormula = loadedData.terrainFormula; 
            this.size = loadedData.size;                     
            this.targetValues = loadedData.targetValues;     
            this.frictionValues = loadedData.frictionValues; 
            this.currentBallPosition = loadedData.currentBallPosition;

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
        }
        System.out.println("Course properties successfully deserialized from JSON.");
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
    }
    /* 
    public String getTerrainFormula() {
        return this.terrainFormula;
    }
    */

    public void addHill(double centerX, double centerY, double peakHeight, double width) {
        // Forward the parameters straight down to your existing terrain manager
        this.TerrainManipulator.addHill(centerX, centerY, peakHeight, width);
    }

    public GolfCourse(double miuK, double miuS) {
        if (miuK != 0.0 && miuS != 0.0){
            this.frictionValues[0] = miuK; 
            this.frictionValues[1] = miuS;
        }  
    }

    public double getMiuK() {
        return frictionValues[0];
    }

    public double getMiuS() {
        return frictionValues[1];
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
        fetchSize();
        if(x < size[0]) return -1 * borderSteepness;
        if(x > size[1]) return borderSteepness;
        
        double slopeX = (TerrainManipulator.calculateHeight(terrainFormula, x + epsilon, y, targetValues) - TerrainManipulator.calculateHeight(terrainFormula, x, y, targetValues))/epsilon;
        return slopeX;
    }

    public double dhdy(double x, double y){
        fetchSize();
        if(y < size[2]) return -1 * borderSteepness;
        if(y > size[3]) return borderSteepness;

        double slopeY = (TerrainManipulator.calculateHeight(terrainFormula, x, y + epsilon, targetValues) - TerrainManipulator.calculateHeight(terrainFormula, x, y, targetValues))/epsilon;
        return slopeY;
    }

    public boolean isWater(double x, double y) {
        return TerrainManipulator.calculateHeight(terrainFormula, x, y, targetValues) < 0;
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
    }
    
    public double getGlobalElevation() {
        return this.globalElevation;
    }
}
