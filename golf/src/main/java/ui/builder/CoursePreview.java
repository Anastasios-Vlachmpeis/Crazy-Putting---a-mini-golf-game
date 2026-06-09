package ui.builder;

import java.util.Map;

import domain.course.GolfCourse;
import domain.obstacles.Sand;
import domain.obstacles.Tree;
import domain.obstacles.Wall;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CoursePreview extends Canvas {

    private final GolfCourse course;
    private int resolutionSize = 2; //number of pixels per "color tile"
    private static final double COURSE_PADDING_PIXELS = 12.0;

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

        PreviewTransform transform = createPreviewTransform();
        if (transform == null) {
            return;
        }

        // (For maximum speed during testing, we step by "resolutionSize" pixels)
        //Map the screen pixels dynamically to the actual game boundaries
        for (int screenX = 0; screenX < w; screenX += resolutionSize) {
            for (int screenY = 0; screenY < h; screenY += resolutionSize) {
                
                // Dynamic translation formula based on live course sizes
                double gameX = transform.toCourseX(screenX);
                double gameY = transform.toCourseY(screenY); // Invert graphic coordinate space

                // Only colotr the canvas if in bounds
                if (transform.isInBounds(gameX, gameY)) {
                    double heightVal = course.height(gameX, gameY);
                    Color terrainColor;
                    
                    if (heightVal < 0) {
                        terrainColor = Color.web("#3498db"); //for water
                    } else {
                        // shading
                        double greenIntensity = 0.4 + (heightVal * 0.05); 
                        greenIntensity = Math.max(0.1, Math.min(0.9, greenIntensity)); 
                        terrainColor = Color.color(0.2, greenIntensity, 0.2); 
                    }
                    
                    gc.setFill(terrainColor);
                    gc.fillRect(screenX, screenY, resolutionSize, resolutionSize); 
                }
            }
        }

        // DRAW OBSTACLES
        double boundsScreenX1 = transform.toScreenX(transform.minX);
        double boundsScreenX2 = transform.toScreenX(transform.maxX);
        double boundsScreenY1 = transform.toScreenY(transform.maxY);
        double boundsScreenY2 = transform.toScreenY(transform.minY);
        gc.save();
        gc.beginPath();
        gc.rect(
            Math.min(boundsScreenX1, boundsScreenX2),
            Math.min(boundsScreenY1, boundsScreenY2),
            Math.abs(boundsScreenX2 - boundsScreenX1),
            Math.abs(boundsScreenY2 - boundsScreenY1)
        );
        gc.clip();

        for (Sand sand : course.getSandPits()) {
            double sandScreenX = transform.toScreenX(sand.getCenterX());
            double sandScreenY = transform.toScreenY(sand.getCenterY());
            double sandRadius = sand.getRadius() * transform.scale;

            gc.setFill(Color.web("#d8bd73"));
            gc.fillOval(sandScreenX - sandRadius, sandScreenY - sandRadius, sandRadius * 2, sandRadius * 2);
        }

        gc.restore();

        for (Tree tree : course.getTrees()) {
            double treeScreenX = transform.toScreenX(tree.getCenterX());
            double treeScreenY = transform.toScreenY(tree.getCenterY());
            double treeRadius = tree.getRadius() * transform.scale;
            double trunkRadius = Math.max(2.0, tree.getTrunkRadius() * transform.scale);

            gc.setFill(Color.web("#1f6b3a"));
            gc.fillOval(treeScreenX - treeRadius, treeScreenY - treeRadius, treeRadius * 2, treeRadius * 2);
            gc.setFill(Color.web("#7a4c24"));
            gc.fillOval(treeScreenX - trunkRadius, treeScreenY - trunkRadius, trunkRadius * 2, trunkRadius * 2);
        }

        gc.save();
        gc.beginPath();
        gc.rect(
            Math.min(boundsScreenX1, boundsScreenX2),
            Math.min(boundsScreenY1, boundsScreenY2),
            Math.abs(boundsScreenX2 - boundsScreenX1),
            Math.abs(boundsScreenY2 - boundsScreenY1)
        );
        gc.clip();

        for (Wall wall : course.getWalls()) {
            double wallStartX = transform.toScreenX(wall.getStartX());
            double wallStartY = transform.toScreenY(wall.getStartY());
            double wallEndX = transform.toScreenX(wall.getEndX());
            double wallEndY = transform.toScreenY(wall.getEndY());

            gc.setStroke(Color.web("#2d2d2d"));
            gc.setLineWidth(Math.max(2.0, wall.getThickness() * transform.scale));
            gc.strokeLine(wallStartX, wallStartY, wallEndX, wallEndY);
        }

        gc.restore();

        // DRAW THE HOLE / TARGET
        double[] target = course.getTargetXYR(); // [x, y, r]
        double targetScreenX = transform.toScreenX(target[0]);
        double targetScreenY = transform.toScreenY(target[1]);
        double targetRadius = target[2] * transform.scale;

        // Draw the dark cup first so the target reads as an actual hole.
        gc.setFill(Color.web("#050505"));
        gc.fillOval(targetScreenX - targetRadius, targetScreenY - targetRadius, targetRadius * 2, targetRadius * 2);

        gc.setFill(Color.web("#202020"));
        double innerRadius = targetRadius * 0.55;
        gc.fillOval(targetScreenX - innerRadius, targetScreenY - innerRadius, innerRadius * 2, innerRadius * 2);

        // Draw the outer cup rim
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(targetScreenX - targetRadius, targetScreenY - targetRadius, targetRadius * 2, targetRadius * 2);

        // DRAW THE BALL
        double[] ballPos = course.getStartPosition(); // index 0 = x, index 1 = y
        double ballScreenX = transform.toScreenX(ballPos[0]);
        double ballScreenY = transform.toScreenY(ballPos[1]);
        double ballRadius = 5.0; // Fixed pixel size so it remains clear on all map sizes

        // Draw the primary white golf ball body
        gc.setFill(Color.WHITE);
        gc.fillOval(ballScreenX - ballRadius, ballScreenY - ballRadius, ballRadius * 2, ballRadius * 2);
    }

    public double[] pixelToCoursePoint(double pixelX, double pixelY) {
        PreviewTransform transform = createPreviewTransform();
        if (transform == null) {
            return null;
        }

        double courseX = transform.toCourseX(pixelX);
        double courseY = transform.toCourseY(pixelY);

        if (!transform.isInBounds(courseX, courseY)) {
            return null;
        }
        return new double[]{courseX, courseY};
    }

    private PreviewTransform createPreviewTransform() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) {
            return null;
        }

        double[] size = course.getSize(); // {minX, maxX, minY, maxY}
        double minX = size[0];
        double maxX = size[1];
        double minY = size[2];
        double maxY = size[3];
        double gameWidth = maxX - minX;
        double gameHeight = maxY - minY;

        if (gameWidth <= 0 || gameHeight <= 0) {
            minX = -20.0;
            maxX = 20.0;
            minY = -20.0;
            maxY = 20.0;
            gameWidth = maxX - minX;
            gameHeight = maxY - minY;
        }

        double padding = Math.min(COURSE_PADDING_PIXELS, Math.max(0.0, Math.min(w, h) / 2.0 - 1.0));
        double drawableWidth = Math.max(1.0, w - padding * 2.0);
        double drawableHeight = Math.max(1.0, h - padding * 2.0);
        double scale = Math.min(drawableWidth / gameWidth, drawableHeight / gameHeight);

        return new PreviewTransform(
            minX,
            maxX,
            minY,
            maxY,
            scale,
            w / 2.0,
            h / 2.0,
            (minX + maxX) / 2.0,
            (minY + maxY) / 2.0
        );
    }

    private static class PreviewTransform {
        private final double minX;
        private final double maxX;
        private final double minY;
        private final double maxY;
        private final double scale;
        private final double canvasCenterX;
        private final double canvasCenterY;
        private final double gameCenterX;
        private final double gameCenterY;

        private PreviewTransform(
            double minX,
            double maxX,
            double minY,
            double maxY,
            double scale,
            double canvasCenterX,
            double canvasCenterY,
            double gameCenterX,
            double gameCenterY
        ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.scale = scale;
            this.canvasCenterX = canvasCenterX;
            this.canvasCenterY = canvasCenterY;
            this.gameCenterX = gameCenterX;
            this.gameCenterY = gameCenterY;
        }

        private double toScreenX(double courseX) {
            return canvasCenterX + (courseX - gameCenterX) * scale;
        }

        private double toScreenY(double courseY) {
            return canvasCenterY - (courseY - gameCenterY) * scale;
        }

        private double toCourseX(double screenX) {
            return gameCenterX + (screenX - canvasCenterX) / scale;
        }

        private double toCourseY(double screenY) {
            return gameCenterY - (screenY - canvasCenterY) / scale;
        }

        private boolean isInBounds(double courseX, double courseY) {
            return courseX >= minX && courseX <= maxX && courseY >= minY && courseY <= maxY;
        }
    }
}
