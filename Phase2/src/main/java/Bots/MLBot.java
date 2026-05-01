package Bots;

import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulator;

public class MLBot extends GolfBot {
    /*
     * we need machine learning golf bot
     * to do this, i think only reingorcement learning can be good
     * because supervised would be just a rule based bot again: am i at the hole?
     * No. Now? no. Now? yes.
     * and unsupervised just doesnt work here.
     * 
     * some ideeas:
     * create a few raiuses around the hole, with bigger diameter.
     * assign a reward for being in that radius , the closer the better.
     * Let the bot shoot randomly, and reward it those points.
     * problem wiith this would be that it would exploit a path rather than explore
     * to find the best one,
     * so i need some derivation for each shot or sum like that.
     * also idk if its even possible to get a hole in one with this terrain and ball
     * location
     * 
     */
    // max initial speed based on manual
    private static final double MAX_SPEED = 5.0;
    private static final double MAX_REWARD = 100;

    public MLBot(GolfCourse course, ShotSimulator shotSimulator) {
        super(course, shotSimulator);
    }

    double[] start = course.getStartPosition();
    double[] target = course.getTargetXYR();
    double sx = start[0];
    double sy = start[1];
    double tx = target[0];
    double ty = target[1];
    double targetRadius = target[2];

    private double calculateReward(double ballX, double ballY) {
        double dx = ballX - tx;
        double dy = ballY - ty;
        double distanceToHole = Math.hypot(dx, dy);

        // ball is actually in the hole
        if (distanceToHole <= targetRadius) {
            return MAX_REWARD;
        }

        // reward zones around the hole
        double[] radii = {
                targetRadius * 2,
                targetRadius * 4,
                targetRadius * 8,
                targetRadius * 16,
                targetRadius * 32
        };
        // rewards for each zone respectively
        double[] rewards = {
                80.0,
                60.0,
                40.0,
                20.0,
                10.0
        };
        // assign reward
        for (int i = 0; i < radii.length; i++) {
            if (distanceToHole <= radii[i]) {
                return rewards[i];
            }
        }

        // very far
        return 0.0;
    }

    /* to do
    1) make the bot shoot
    2) store the best shot
    3) make the best shot influence the next
     */



}
