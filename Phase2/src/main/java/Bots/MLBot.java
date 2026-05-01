package Bots;

import java.util.Random;

import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulator;

public class MLBot extends GolfBot {
    // max initial speed based on manual
    private static final double MAX_SPEED = 5.0;
    private static final double MAX_REWARD = 100;
    Random random = new Random();

    double[] start = course.getStartPosition();
    double[] target = course.getTargetXYR();
    double sx = start[0];
    double sy = start[1];
    double tx = target[0];
    double ty = target[1];
    double targetRadius = target[2];


    //double distanceToTarget = course.distanceToTarget;

    public MLBot(GolfCourse course, ShotSimulator shotSimulator) {
        super(course, shotSimulator);
    }


    private double[] getRandomChange(double vx, double vy) {
        switch (random.nextInt(5)) {
            case 0:
                return new double[] { vx , vy };

            case 1:

                break;
            case 2:

                break;
            case 3:

                break;
            case 4:

                break;

            default:
                break;
        }
    }

    public double[] shoot() {

        return new double[] { vx, vy };
    }

}
