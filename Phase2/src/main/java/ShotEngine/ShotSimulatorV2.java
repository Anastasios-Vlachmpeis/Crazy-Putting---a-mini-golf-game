package ShotEngine;

import java.util.ArrayList;
import java.util.Arrays;

import GolfCourseData.GolfCourse;
import GolfCourseData.Obstacles.Tree;
import GolfCourseData.Obstacles.Wall;
import Solvers.Solver;
import Systems.GolfODE;

public class ShotSimulatorV2 {
    
    //state {x, y, vx, vy}
    //h stepsize
    public double[][] schoot(GolfODE equation, Solver solver, double[] y0, double h){

        GolfCourse course = equation.getCourse();
        double[] targetValues = course.getTargetXYR();
        
        double t = 0.0;
        double[] state = Arrays.copyOf(y0, y0.length);

        //double[][] solution = new double[MAX_STEPS][5]; //made space for 1000 iterations
        ArrayList<double[]> trajectoryList = new ArrayList<>();
        // Store initial position before movement starts
        trajectoryList.add(solver.storeRow(t, state));

        int MAX_STEPS = 5_000;//safety cap -> 100 steps per second so a max simulation time of 50 seconds
        for (int k = 0; k < MAX_STEPS; k++){
            double speed = Math.sqrt(state[2]*state[2]+state[3]*state[3]);

            double hx = course.dhdx(state[0],state[1]);
            double hy = course.dhdy(state[0],state[1]);
            double slopeMagnitude = Math.sqrt(hx * hx + hy * hy);

            if (speed < 0.01 && course.getMiuS(state[0], state[1]) > slopeMagnitude) {//speed threshhold of 0.01m/s
                state[2] = 0;
                state[3] = 0;
                trajectoryList.add(solver.storeRow(t, state));
                //solution[k++] = storeRow(t, state);
                break;
            }

            // ball is in the hole
            double dx = state[0] - targetValues[0];
            double dy = state[1] - targetValues[1];
            double distanceTarget = Math.sqrt(dx * dx + dy * dy);
            if (distanceTarget < 0.75 * targetValues[2]) { 
                break; 
            }

            //continue if ball did not stop
            double[] previousState = Arrays.copyOf(state, state.length);
            state = solver.iteration(equation, t, state, h);
            if (course.isTree(state[0], state[1])) {
                state = bounceOffTree(course, previousState, state);
            } else if (course.isWall(state[0], state[1])) {
                state = bounceOffWall(course, previousState, state);
            }
            t += h;
            //System.out.println(Arrays.toString(state));
            
            trajectoryList.add(solver.storeRow(t, state));
            if (course.isWater(state[0], state[1])) {
                break;
            }
        }
        //Convert to simple array
        double[][] solution = trajectoryList.toArray(new double[0][]);
        return solution;
    }

    private double[] bounceOffTree(GolfCourse course, double[] previousState, double[] collisionState) {
        Tree tree = course.getTreeAt(collisionState[0], collisionState[1]);
        if (tree == null) {
            return collisionState;
        }

        double normalX = collisionState[0] - tree.getCenterX();
        double normalY = collisionState[1] - tree.getCenterY();
        double normalLength = Math.sqrt(normalX * normalX + normalY * normalY);

        if (normalLength < 0.0001) {
            normalX = previousState[0] - tree.getCenterX();
            normalY = previousState[1] - tree.getCenterY();
            normalLength = Math.sqrt(normalX * normalX + normalY * normalY);
        }

        if (normalLength < 0.0001) {
            normalX = -previousState[2];
            normalY = -previousState[3];
            normalLength = Math.sqrt(normalX * normalX + normalY * normalY);
        }

        if (normalLength < 0.0001) {
            return previousState;
        }

        normalX /= normalLength;
        normalY /= normalLength;

        double clearance = 0.03;
        double bouncedX = tree.getCenterX() + normalX * (tree.getCollisionRadius() + clearance);
        double bouncedY = tree.getCenterY() + normalY * (tree.getCollisionRadius() + clearance);

        return reflectVelocity(collisionState, normalX, normalY, bouncedX, bouncedY, 0.65);
    }

    private double[] bounceOffWall(GolfCourse course, double[] previousState, double[] collisionState) {
        Wall wall = course.getWallAt(collisionState[0], collisionState[1]);
        if (wall == null) {
            return collisionState;
        }

        double wallDx = wall.getEndX() - wall.getStartX();
        double wallDy = wall.getEndY() - wall.getStartY();
        double wallLength = Math.sqrt(wallDx * wallDx + wallDy * wallDy);
        if (wallLength < 0.0001) {
            return previousState;
        }

        double normalX = -wallDy / wallLength;
        double normalY = wallDx / wallLength;
        double fromWallX = collisionState[0] - wall.getCenterX();
        double fromWallY = collisionState[1] - wall.getCenterY();
        if (normalX * fromWallX + normalY * fromWallY < 0) {
            normalX = -normalX;
            normalY = -normalY;
        }

        double clearance = 0.04;
        double bouncedX = previousState[0] + normalX * (wall.getThickness() / 2.0 + clearance);
        double bouncedY = previousState[1] + normalY * (wall.getThickness() / 2.0 + clearance);

        return reflectVelocity(collisionState, normalX, normalY, bouncedX, bouncedY, 0.8);
    }

    private double[] reflectVelocity(
            double[] collisionState, double normalX, double normalY,
            double bouncedX, double bouncedY, double damping) {

        double velocityX = collisionState[2];
        double velocityY = collisionState[3];
        double dot = velocityX * normalX + velocityY * normalY;

        if (dot < 0) {
            velocityX = velocityX - 2.0 * dot * normalX;
            velocityY = velocityY - 2.0 * dot * normalY;
        }

        return new double[]{
            bouncedX,
            bouncedY,
            velocityX * damping,
            velocityY * damping
        };
    }
}
