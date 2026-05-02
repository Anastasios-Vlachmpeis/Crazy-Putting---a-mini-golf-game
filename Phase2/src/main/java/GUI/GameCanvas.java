package GUI;

import java.util.Arrays;
import GolfCourseData.*;
import Physics.Ball;
import Solvers.RungeKuttaSolver;
import Systems.GolfODE;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

public class GameCanvas {

    // fixed canvas pixel size
    private static final double CANVAS_W = 800.0;
    private static final double CANVAS_H = 800.0;

    // we don't want target or ball to sit right on the edge of the canvas, so we
    // add a margin
    private static final double MARGIN = 2.0;

    // for scaling:
    // how many real world meters are represented by one pixel on the canvas
    // (default value)
    private double metersPerPixel = 20.0 / CANVAS_W; // 20 meters across the whole canvas
    // world coordinates at top left corner of canvas (default value)
    // user never touches these -> derived from course
    private double worldX0 = 0.0;
    private double worldY0 = 0.0;

    // private static final double TARGET_RADIUS_PIXELS = 5.0; Not used ~Stan
    private static final double BALL_RADIUS_PIXELS = 5.0;
    private static final double V_MAX = 5.0; // max velocity for shot
    private static final double ARROW_SCALE = 40.0; // pixels per meters/second for aiming arrow

    private static final double SIMULATION_STEP = 0.01; // seconds per simulation step -> step size for RK4 in seconds

    private static final int ANIMATION_FPS = 10; // frames per second for animation -> need larger number of animation
                                                 // takes forever

    private final Canvas terrain = new Canvas(CANVAS_W, CANVAS_H); // will only be drawn once at the start of the game
    private final Canvas objects = new Canvas(CANVAS_W, CANVAS_H); // holds ball and target, will be cleared and redrawn
                                                                   // every frame
    private final StackPane view = new StackPane(terrain, objects); // stacked so terrain is in the background and
                                                                    // objects are in the foreground

    // shared states (getter methods in GUI_phase2)
    private GolfCourse golfCourse;
    private GolfODE golfODE;
    private Ball ball;
    private ShotPanel shotPanel; // to update after every shot

    // for aiming arrow
    private double dragStartX = 0.0;
    private double dragStartY = 0.0;
    private boolean dragging = false;

    public GameCanvas(GolfCourse golfCourse, GolfODE golfODE, Ball ball) {
        this.golfCourse = golfCourse;
        this.golfODE = golfODE;
        this.ball = ball;

        attachMouseHandlers();
    }

    public void setCourse(GolfCourse golfCourse, GolfODE golfODE, Ball ball) {
        this.golfCourse = golfCourse;
        this.golfODE = golfODE;
        this.ball = ball;
    }

    public void setShotPanel(ShotPanel shotPanel) {
        this.shotPanel = shotPanel;
    }

    public StackPane getView() {
        return view;
    }

    // we use this mehod to make sure that scaling and worl coordinates work for the
    // new course and we can see the target and ball
    private void fitToCurrentCourse() {
        double[] target = golfCourse.getTargetXYR(); // [tx, ty, r]
        double[] start = golfCourse.getStartPosition(); // [sx, sy, sh]

        // find bounding box of course features (start and target)
        // this ensures we don't draw the whole world but only the part that includes
        // target and ball
        double minX = Math.min(start[0], target[0]) - MARGIN;
        double maxX = Math.max(start[0], target[0]) + MARGIN;
        double minY = Math.min(start[1], target[1]) - MARGIN;
        double maxY = Math.max(start[1], target[1]) + MARGIN;

        // height and width of the bounding box in world coordinates
        double worldW = maxX - minX;
        double worldH = maxY - minY;

        // need to make sure the largest world dimension fits into the canvas, so we
        // calculate the required metersPerPixel for both dimensions
        double mppX = worldW / CANVAS_W;
        double mppY = worldH / CANVAS_H;
        metersPerPixel = Math.max(mppX, mppY); // use the larger metersPerPixel to ensure everything fits within the
                                               // canvas

        // center the view on the middle of the bounding box
        double worldCenterX = (minX + maxX) / 2.0;
        double worldCenterY = (minY + maxY) / 2.0;
        worldX0 = worldCenterX - (CANVAS_W / 2.0) * metersPerPixel;
        worldY0 = worldCenterY - (CANVAS_H / 2.0) * metersPerPixel;
    }

    // Terrain layer
    public void drawTerrain() {
        fitToCurrentCourse();

        GraphicsContext gc = terrain.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_W, CANVAS_H); // we want to clear the canvas before we redraw

        int cellPx = 2; // size of each cell in pixels -> determines resolution of terrain drawing
                        // (smaller = more detailed but slower)
        int cols = (int) (CANVAS_W / cellPx); //
        int rows = (int) (CANVAS_H / cellPx);

        double maxH = getMaxHeight();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double px = col * cellPx + cellPx * 0.5;
                double py = row * cellPx + cellPx * 0.5;

                double[] world = canvasToWorld(px, py);
                double h = golfCourse.height(world[0], world[1]);

                gc.setFill(computeColorForHeight(h, maxH));
                gc.fillRect(col * cellPx, row * cellPx, cellPx, cellPx);
            }
        }

        drawGrid(gc);
    }

    private Color computeColorForHeight(double h, double maxH) {
        ColorTerrain cT = new ColorTerrain();

        if (h < 0) {
            return cT.isWaterColor(h);
        } else {
            return cT.isGrassColor(h, maxH);
        }
    }

    private double getMaxHeight() {
        double maxH = 0.0;
        for (int x = 0; x <= CANVAS_W; x += 5) { // we don't want to check every pixels since this takes a long time so
                                                 // we check every 5 pixels
            for (int y = 0; y <= CANVAS_H; y += 5) {
                double[] world = canvasToWorld(x, y);
                double h = golfCourse.height(world[0], world[1]);
                if (h > maxH)
                    maxH = h;
            }
        }
        return maxH;
    }

    // Objects layer (ball and target)
    public void drawObjects(double[] ballState, double[][] trajectory, double arrowVx, double arrowVy) {
        GraphicsContext gc = objects.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_W, CANVAS_H);

        if (trajectory != null)
            drawTrail(gc, trajectory);

        // target
        double[] target = golfCourse.getTargetXYR();
        double tpx = worldToCanvasX(target[0]);
        double tpy = worldToCanvasY(target[1]);
        // convert world-unit radius to pixels using current scale
        double tradPx = target[2] / metersPerPixel;

        // outer red circle for target
        gc.setStroke(Color.RED);
        gc.setLineWidth(2.5);
        gc.strokeOval(tpx - tradPx, tpy - tradPx, tradPx * 2, tradPx * 2);

        // flag pin
        gc.setFill(Color.YELLOW); // at bottom of flag pin
        gc.fillOval(tpx - 3, tpy - 3, 6, 6);
        gc.setStroke(Color.WHITE); // outline
        gc.setLineWidth(1.5);
        gc.strokeLine(tpx, tpy - tradPx, tpx, tpy - tradPx - 14);
        gc.setFill(Color.RED);
        gc.fillPolygon(
                new double[] { tpx, tpx + 10, tpx },
                new double[] { tpy - tradPx - 14, tpy - tradPx - 9, tpy - tradPx - 4 },
                3); // simple triangle flag

        // ball
        double bpx = worldToCanvasX(ballState[0]);
        double bpy = worldToCanvasY(ballState[1]);

        gc.setFill(Color.WHITE);
        gc.fillOval(bpx - BALL_RADIUS_PIXELS, bpy - BALL_RADIUS_PIXELS, BALL_RADIUS_PIXELS * 2, BALL_RADIUS_PIXELS * 2);
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1.0);
        gc.strokeOval(bpx - BALL_RADIUS_PIXELS, bpy - BALL_RADIUS_PIXELS, BALL_RADIUS_PIXELS * 2,
                BALL_RADIUS_PIXELS * 2);

        // aiming arrow
        if (dragging && (arrowVx != 0.0 || arrowVy != 0.0)) {
            drawShotArrow(gc, bpx, bpy, arrowVx, arrowVy);
        }
    }

    // Aiming arrow to help user
    private void drawShotArrow(GraphicsContext gc, double bpx, double bpy, double vx, double vy) {
        double speed = Math.sqrt(vx * vx + vy * vy);
        double fraction = Math.min(speed / V_MAX, 1.0);

        double tipX = bpx + vx * ARROW_SCALE;
        double tipY = bpy - vy * ARROW_SCALE; // canvas y is flipped relative to world y

        Color arrowColor = Color.DARKGRAY;

        gc.setStroke(arrowColor);
        gc.setLineWidth(2.0);
        gc.strokeLine(bpx, bpy, tipX, tipY);

        double angle = Math.atan2(bpy - tipY, tipX - bpx); // angle of the arrow in radians
        double headLen = 12.0; // length of the arrow head in pixels
        double spread = Math.PI / 7.0;
        double ax1 = tipX - headLen * Math.cos(angle - spread);
        double ay1 = tipY + headLen * Math.sin(angle - spread);
        double ax2 = tipX - headLen * Math.cos(angle + spread);
        double ay2 = tipY + headLen * Math.sin(angle + spread);

        gc.setFill(arrowColor);
        gc.fillPolygon(new double[] { tipX, ax1, ax2 },
                new double[] { tipY, ay1, ay2 }, 3);
        gc.setFill(Color.WHITE);
        gc.fillText(String.format("%.1f m/s", speed), tipX + 6, tipY - 4);
    }

    // draw the trail of the ball's trajectory as semi-transparent circles
    private void drawTrail(GraphicsContext gc, double[][] trajectory) {
        // as semi-transparent circles
        gc.setFill(Color.rgb(255, 255, 255, 0.35));
        for (int i = 0; i < trajectory.length; i += 5) {
            double px = worldToCanvasX(trajectory[i][1]); // col 1 = x
            double py = worldToCanvasY(trajectory[i][2]); // col 2 = y
            gc.fillOval(px - 2, py - 2, 4, 4);
        }

        // pyrmaid shape
        /*
         * int total = trajectory.length;
         * for (int i = 1; i < total; i += 3) {
         * double px = worldToCanvasX(trajectory[i][1]);
         * double py = worldToCanvasY(trajectory[i][2]);
         * 
         * //the fraction of the way through the trajectory (0 to 1) -> determines width
         * of pyramid at current point and opacity of the fill color
         * double fraction = (double) i / total; // 0 = oldest, 1 = newest
         * double halfW = fraction * 4.0;
         * 
         * //perpendicular direction to trail at current point
         * double prevPx = worldToCanvasX(trajectory[i - 1][1]);
         * double prevPy = worldToCanvasY(trajectory[i - 1][2]);
         * double dx = px - prevPx;
         * double dy = py - prevPy;
         * double len = Math.sqrt(dx * dx + dy * dy);
         * if (len < 1e-9)
         * continue;
         * double perpX = -dy / len * halfW;
         * double perpY = dx / len * halfW;
         * 
         * gc.setFill(Color.rgb(255, 255, 255, 0.15 + fraction * 0.3));
         * gc.fillPolygon(
         * new double[]{ prevPx, px + perpX, px - perpX },
         * new double[]{ prevPy, py + perpY, py - perpY },
         * 3
         * );
         * }
         */
    }

    // helps the user have a sense of the scale of the terrain and where the target
    // is in relation to the ball
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.rgb(0, 0, 0, 0.10));
        gc.setLineWidth(0.5);

        // world metres that correspond to ~60 px at current scale
        double gridSpacing = niceGridSpacing(60.0 * metersPerPixel);

        double worldLeft = worldX0;
        double worldRight = worldX0 + CANVAS_W * metersPerPixel;
        double worldBottom = worldY0;
        double worldTop = worldY0 + CANVAS_H * metersPerPixel;

        // vertical lines
        double startX = Math.floor(worldLeft / gridSpacing) * gridSpacing;
        for (double wx = startX; wx <= worldRight; wx += gridSpacing) {
            gc.strokeLine(worldToCanvasX(wx), 0, worldToCanvasX(wx), CANVAS_H);
        }

        // horizontal lines
        double startY = Math.floor(worldBottom / gridSpacing) * gridSpacing;
        for (double wy = startY; wy <= worldTop; wy += gridSpacing) {
            gc.strokeLine(0, worldToCanvasY(wy), CANVAS_W, worldToCanvasY(wy));
        }
    }

    // helper function to choose "nice" grid spacing (1, 2, 5, 10, 20, etc.) based
    // on current scale -> keeps it at round values
    private double niceGridSpacing(double rawSpacing) {
        double[] nice = { 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0 };
        for (double s : nice) {
            if (s >= rawSpacing)
                return s;
        }
        return nice[nice.length - 1];
    }

    public void fireShot(double vx, double vy) {
        double[] startState = { ball.getState()[0], ball.getState()[1], vx, vy };
        // Weird loop in ball state between this file and Ball.java
        // This causes the location of the ball to suddenly reset to default ~Stan
        // double[] startState = new double[]{ball.getState()[0], ball.getState()[1],
        // vx, vy};
        // ball.setPos(startState);

        double[][] trajectory = new RungeKuttaSolver().solveBall(golfODE, startState, SIMULATION_STEP);

        animateBall(trajectory);
    }

    public void animateBall(double[][] trajectory) {
        objects.setMouseTransparent(true);

        final int[] frameIndex = { 0 }; // use array to allow modification inside lambda
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(16), event -> {
            if (frameIndex[0] >= trajectory.length) {
                timeline.stop();
                objects.setMouseTransparent(false);

                double[] finalRow = trajectory[trajectory.length - 1];
                double[] finalState = new double[] { finalRow[1], finalRow[2], finalRow[3], finalRow[4] };
                System.out.println("Ball State: " + Arrays.toString(finalState)); // ~Stan
                ball.setPos(finalState);
                onShotComplete(finalState, trajectory);
                return;
            }

            double[] row = trajectory[frameIndex[0]];
            double[] state = new double[] { row[1], row[2], row[3], row[4] };
            drawObjects(state, trajectory, 0.0, 0.0);
            frameIndex[0] += ANIMATION_FPS;
        }));

        timeline.play();
    }

    private void onShotComplete(double[] finalState, double[][] trajectory) {
        double dist = golfCourse.distanceToTarget(finalState);
        boolean inWater = golfCourse.isWater(finalState[0], finalState[1]);
        boolean won = dist <= golfCourse.getTargetXYR()[2]; // check if ball is within target radius -> if yes player
                                                            // wins

        if (shotPanel != null)
            shotPanel.onShotLanded(finalState, dist, inWater, won);

        drawObjects(finalState, trajectory, 0.0, 0.0);
    }

    private void attachMouseHandlers() {

        objects.setOnMousePressed(event -> {
            dragStartX = event.getX();
            dragStartY = event.getY();
            dragging = true;
        });

        objects.setOnMouseDragged(event -> {
            if (!dragging)
                return;
            double[] vel = dragToVelocity(event.getX(), event.getY());
            drawObjects(ball.getState(), null, vel[0], vel[1]);
        });

        objects.setOnMouseReleased(event -> {
            if (!dragging)
                return;
            dragging = false;

            double[] vel = dragToVelocity(event.getX(), event.getY());
            if (Math.abs(vel[0]) < 1e-6 && Math.abs(vel[1]) < 1e-6) {
                drawObjects(ball.getState(), null, 0.0, 0.0);
                return;
            }
            if (shotPanel != null)
                shotPanel.recordShot();
            fireShot(vel[0], vel[1]);
        });
    }

    private double[] dragToVelocity(double mouseX, double mouseY) {
        double bpx = worldToCanvasX(ball.getState()[0]);
        double bpy = worldToCanvasY(ball.getState()[1]);

        double ddx = mouseX - bpx; // from ball toward mouse
        double ddy = bpy - mouseY; // canvas y is flipped, so flip the sign

        double vx = ddx * (V_MAX / (CANVAS_W * 0.3));
        double vy = ddy * (V_MAX / (CANVAS_H * 0.3));
        double spd = Math.sqrt(vx * vx + vy * vy);
        if (spd > V_MAX) {
            vx = vx / spd * V_MAX;
            vy = vy / spd * V_MAX;
        }

        return new double[] { vx, vy };
    }

    // converts world coordinates (meters) to canvas coordinates (pixels)
    private double worldToCanvasX(double wx) {
        return (wx - worldX0) / metersPerPixel;
    }

    private double worldToCanvasY(double wy) {
        return CANVAS_H - (wy - worldY0) / metersPerPixel;
    }

    // converts canvas coordinates (pixels) to world coordinates (meters)
    private double[] canvasToWorld(double px, double py) {
        double wx = worldX0 + px * metersPerPixel;
        double wy = worldY0 + (CANVAS_H - py) * metersPerPixel;
        return new double[] { wx, wy };
    }
}
