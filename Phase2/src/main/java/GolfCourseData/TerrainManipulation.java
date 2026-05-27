package GolfCourseData;

import java.util.ArrayList;
import java.util.List;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class TerrainManipulation {
    //Here we add:
    //Height function
    //the Gaussian bell curves
    //The physical hole

    //returns:
    //total height 


    private List<Hill> hills = new ArrayList<>(); //Artificial added Hills/valleys
    //private GolfCourse currentCourse = new GolfCourse(){};

    public double calculateHeight(String formula, double x, double y, double[] target){
        return functionHeight(formula, x, y, target) + artificialHillsHeight(x, y);
    }

    //height based on the formula
    private double functionHeight(String formula, double x, double y, double[] target){
        Expression e = new ExpressionBuilder(formula)
            .variables("x", "y")
            .build();
        e.setVariable("x", x);
        e.setVariable("y", y);     
        double baseFormulaHeight = e.evaluate();

        /*
        //Add target hole
        double dx = x - target[0];
        double dy = y - target[1];
        double distanceSquared = (dx * dx) + (dy * dy);
        double holeRadius = target[2];
        double depth = -0.5;
        // Gaussian bell curve formula: height = A * exp(-d^2 / (2 * sigma^2))
        // Using 'holeRadius' directly as a divisor for simplicity
        double divisor = 2.0 * (holeRadius * holeRadius);
        double holeManipulation = depth * Math.exp(-distanceSquared / divisor);

        return baseFormulaHeight + holeManipulation;

        ////
        double funnelWidth = holeRadius * 2.5; // Smooth apron around the hole
        double divisor = 2.0 * (funnelWidth * funnelWidth);
        double cupDepth = -2.0; 

        double funnelDrop = cupDepth * Math.exp(-distanceSquared / divisor);
        double finalHeight = baseFormulaHeight + funnelDrop;

        // THE FIX: Flatten the bottom of the cup
        // If the math pushes the ball deeper than the physical cup floor, flatten it!
        double actualHoleRadiusSq = holeRadius * holeRadius;
        if (distanceSquared < actualHoleRadiusSq) {
            return baseFormulaHeight + cupDepth; // Perfect flat floor inside the cylinder
        }
        return finalHeight;
        */
        return baseFormulaHeight;
    }

    //Calculate hill/valley height on given coordinate
    private double artificialHillsHeight(double x, double y){
        // Accumulate the height of ALL hills at this specific (x, y) point
        double totalHillHeight = 0;
        for (Hill hill : hills) {
            double dx = x - hill.centerX;
            double dy = y - hill.centerY;
            double distanceSquared = (dx * dx) + (dy * dy);
            
            // Optimization: If the ball is too far from the hill, skip the expensive Math.exp()
            // A Gaussian hill effectively hits 0 height at roughly 3 * width
            double cutOffDistance = hill.width * 3.0;
            if (distanceSquared > (cutOffDistance * cutOffDistance)) {
                continue; 
            }
            double divisor = 2.0 * (hill.width * hill.width);
            totalHillHeight += hill.peakHeight * Math.exp(-distanceSquared / divisor);
        }
        return totalHillHeight;
    }

    public void addHill(double centerX, double centerY, double peakHeight, double width){
        hills.add(new Hill(centerX, centerY, peakHeight, width));
    }
}
