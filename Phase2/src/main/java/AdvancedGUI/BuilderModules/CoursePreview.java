package AdvancedGUI.BuilderModules;

import java.util.Map;

import GolfCourseData.GolfCourse;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CoursePreview extends Canvas {

    private final GolfCourse course;
    private int resolutionSize = 3; //#pixels per "color tile"

    // Constructor defines the size of the preview canvas
    public CoursePreview(GolfCourse course, double width, double height) {
        super(width, height);
        this.course = course;
        
        // Initial draw when the component is created
        updatePreview();
    }

    // Call this whenever the map updates to repaint the canvas
    public void updatePreview() {
        GraphicsContext gc = this.getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // Fill the canvas with a background color for out-of-bounds space
        gc.setFill(Color.web("#1e1e1e")); // dark gray
        gc.fillRect(0, 0, w, h);

        // Pull the actual live game boundaries from the course
        double[] size = course.getSize(); // {minX, maxX, minY, maxY}
        double minX = size[0];
        double maxX = size[1];
        double minY = size[2];
        double maxY = size[3];

        double gameWidth = maxX - minX;
        double gameHeight = maxY - minY;

        //Find the limiting scale direction to avoid stretching
        double scaleX = w / gameWidth;
        double scaleY = h / gameHeight;
        double scale = Math.min(scaleX, scaleY);

        //locate center
        double canvasCenterX = w / 2.0;
        double canvasCenterY = h / 2.0;
        double gameCenterX = (minX + maxX) / 2.0;
        double gameCenterY = (minY + maxY) / 2.0;

        // (For maximum speed during testing, we step by "resolutionSize" pixels)
        //Map the screen pixels dynamically to the actual game boundaries
        for (int screenX = 0; screenX < w; screenX += resolutionSize) {
            for (int screenY = 0; screenY < h; screenY += resolutionSize) {
                
                // Dynamic translation formula based on live course sizes
                double gameX = gameCenterX + (screenX - canvasCenterX) / scale;
                double gameY = gameCenterY - (screenY - canvasCenterY) / scale; // Invert graphic coordinate space

                // Only colotr the canvas if in bounds
                if (gameX >= minX && gameX <= maxX && gameY >= minY && gameY <= maxY) {
                    Color terrainColor = getColorForPosition(gameX, gameY);
                    gc.setFill(terrainColor);
                    gc.fillRect(screenX, screenY, resolutionSize, resolutionSize);
                }
            }
        }

        // DRAW THE HOLE / TARGET
        double[] target = course.getTargetXYR(); // [x, y, r]
        double targetScreenX = canvasCenterX + (target[0] - gameCenterX) * scale;
        double targetScreenY = canvasCenterY - (target[1] - gameCenterY) * scale;
        double targetRadius = target[2] * scale;

        // Draw the outer cup rim
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(targetScreenX - targetRadius, targetScreenY - targetRadius, targetRadius * 2, targetRadius * 2);
        
        // Draw the center flag cup
        gc.setFill(Color.BLACK);
        gc.fillOval(targetScreenX - 3, targetScreenY - 3, 6, 6);
    }

    private Color getColorForPosition(double gameX, double gameY) {
        // Check for water
        if (course.isWater(gameX, gameY)) {
            return Color.web("#3498db"); // Blue for water
        } 
    
        //  Otherwise, calculate the green terrain shading based on height
        double heightVal = course.height(gameX, gameY);

        // Simple procedural shading: higher ground is lighter green
        double greenIntensity = 0.4 + (heightVal * 0.1);
    
        // Clamp the value strictly between 0.1 and 0.9 to prevent out-of-bounds color ranges
        greenIntensity = Math.max(0.1, Math.min(0.9, greenIntensity));

        return Color.color(0.2, greenIntensity, 0.2);
    }
}
