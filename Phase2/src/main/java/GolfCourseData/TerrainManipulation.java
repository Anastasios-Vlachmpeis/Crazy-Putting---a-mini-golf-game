package GolfCourseData;

import java.util.ArrayList;
import java.util.List;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class TerrainManipulation {

    private List<Hill> hills = new ArrayList<>(); //Artificial added Hills/valleys
    //private GolfCourse currentCourse = new GolfCourse(){};

    //Improbe perfomance
    private String lastFormula = "";
    private transient Expression compiledExpression;

    public double calculateHeight(String formula, double x, double y, double[] target){
        return functionHeight(formula, x, y, target) + artificialHillsHeight(x, y);
    }

    //height based on the formula
    private double functionHeight(String formula, double x, double y, double[] target){
        // OPTIMIZATION: Only parse the string into a mathematical tree if the formula text actually changed
        if (!formula.equals(lastFormula) || compiledExpression == null) {
            lastFormula = formula;
            compiledExpression = new ExpressionBuilder(formula) 
                .variables("x", "y") 
                .build(); 
        }
        
        // Re-use the existing compiled function instantly by just swapping the coordinates
        compiledExpression.setVariable("x", x); 
        compiledExpression.setVariable("y", y);      
        return compiledExpression.evaluate(); 
    }

    //Calculate hill/valley height on given coordinate
    private double artificialHillsHeight(double x, double y){
        // Accumulate the height of ALL hills at this specific (x, y) point
        double totalHillHeight = 0;
        for (Hill hill : hills) {
            double dx = x - hill.centerX;
            double dy = y - hill.centerY;
            double distanceSquared = (dx * dx) + (dy * dy);
            
            // Optimization: If the ball is too far from the hill, then skip
            // A Gaussian hill effectively hits 0 height at roughly 3 * width
            double cutOffDistance = hill.width * 3.0;
            if (distanceSquared > (cutOffDistance * cutOffDistance)) {
                continue; 
            }
            double divisor = 2.0 * (hill.width * hill.width);
            totalHillHeight += hill.peakHeight * Math.exp(-distanceSquared / divisor);
        }
        //cap the Height
        if(totalHillHeight < -5){totalHillHeight = -5;};
        if(totalHillHeight > 5){totalHillHeight = 5;};
        return totalHillHeight;
    }

    public void addHill(double centerX, double centerY, double peakHeight, double width){
        hills.add(new Hill(centerX, centerY, peakHeight, width));
        System.out.println("New Hill/valley added at " + centerX + centerY);
    }

    public java.util.List<Hill> getHills() {
        return this.hills;
    }

    public void clearHills() {
        this.hills.clear();
    }
}
