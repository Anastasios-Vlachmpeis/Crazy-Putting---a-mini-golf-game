package GolfCourseData;

import java.nio.file.*;
import java.util.*;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class GolfCourse {
//Here we extend the methods for actually using the generated course
    public final double epsilon = 1e-7;

    //private double miuK; // kinetic friction coeficient
    //private double miuS; // static friction coeficient, theoretically its more like a constant. I just put it here to have both mius in one place
    //these values are just test/default values
    private String[] friction;
    private double[] frictionValues = {0.15, 0.5, 0.0};
    private String[] target;
    private double[] targetValues = {10.5, 5.0, 0.2};
    private String[] start;
    private double[] startValues = {0.0, 0.0, 0.0};
    private String terrainFormula = "0.5*sin(x-2y)+2.5";

    //GeneratedCourse course = new GeneratedCourse();

    public GolfCourse(){}

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

    public void loadFromGUI(String heightFormula, String[][] inputValuesGUI)throws Exception{
        friction = inputValuesGUI[0];
        target = inputValuesGUI[1];
        start = inputValuesGUI[2];
        terrainFormula = heightFormula;

        convertToDouble();
    }

    public void convertToDouble(){
        frictionValues = Arrays.stream(friction)
            .mapToDouble(Double::parseDouble)
            .toArray();

        targetValues = Arrays.stream(target)
            .mapToDouble(Double::parseDouble)
            .toArray();

        startValues = Arrays.stream(start)
            .mapToDouble(Double::parseDouble)
            .toArray();

    }

    public double calculateHeight(String formula, double x, double y){
        Expression e = new ExpressionBuilder(formula)
            .variables("x", "y")
            .build();

        e.setVariable("x", x);
        e.setVariable("y", y);

        return e.evaluate();
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
        return calculateHeight(terrainFormula, x, y);
    }
    
    
    public double[] getDerivative(double x, double y){
        double slopeX = (calculateHeight(terrainFormula,x + epsilon, y) - calculateHeight(terrainFormula,x, y))/epsilon;
        double slopeY = (calculateHeight(terrainFormula,x, y + epsilon) - calculateHeight(terrainFormula,x, y))/epsilon;
        return new double[] {slopeX, slopeY};
    }
    

    public double dhdx(double x, double y){
        double slopeX = (calculateHeight(terrainFormula, x + epsilon, y) - calculateHeight(terrainFormula, x, y))/epsilon;
        return slopeX;
    }

    public double dhdy(double x, double y){
        double slopeY = (calculateHeight(terrainFormula, x, y + epsilon) - calculateHeight(terrainFormula, x, y))/epsilon;
        return slopeY;
    }

    public boolean isWater(double x, double y) {
        return calculateHeight(terrainFormula, x, y) < 0;
    }

    public double[] getFrictions(){
        /*
        double friction1 = course.courseData[0][0];
        double friction2 = course.courseData[0][1];
        double friction3 = course.courseData[0][2];
        return new double[] {friction1, friction2, friction3};
        */
       return frictionValues;
    }

    public double[] getTargetXYR(){
        /*
        double x = course.courseData[1][0];
        double y = course.courseData[1][1];
        double radius = course.courseData[1][2];
        return new double[] {x, y, radius};
        */
       return targetValues;
    }

    public double[] getStartPosition(){
        double x = startValues[0];
        double y = startValues[1];
        double height = calculateHeight(terrainFormula, x,y);
        return new double[] {x, y, height};
    }

    //Set the current ball position for the next stroke
    // Position needs to be updated before the bot shoots again
    public void setBallPosition(double x, double y) {
        startValues[0] = x;
        startValues[1] = y;
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
}
