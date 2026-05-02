package Bots;

import java.util.Random;

import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulation;
import ShotEngine.ShotSimulator;

public class MLBot extends GolfBot {

    private static final double MAX_SPEED = 5.0;
    private final Random random = new Random();

    public MLBot(GolfCourse course, ShotSimulator shotSimulator) {
        super(course, shotSimulator);
    }

    public double[] shoot() {
        System.out.println("start MLBot");
        double[] target = course.getTargetXYR();
        double tx = target[0];
        double ty = target[1];
        double tRadius = target[2];
        double bestVx = 0;
        double bestVy = 0;
        double distanceToHole = Double.MAX_VALUE;
        double bestDistance = Double.MAX_VALUE;
        
        // usually takes between 1-10k shots to get result but sometimes more than 40k
        int i = 0;
        while (distanceToHole > tRadius) {
            System.out.println("shot nr " + (i + 1));

            double angle = random.nextDouble() * 2 * Math.PI;// get direction based on a circle
            double speed = random.nextDouble() * MAX_SPEED; // get random speed, max <= 5

            double vx = speed * Math.cos(angle); 
            double vy = speed * Math.sin(angle);

            ShotSimulation sim = shotSimulator.simulate(vx, vy); // simulate the shot

            // get results from simulation
            double finalX = sim.finalX();
            double finalY = sim.finalY();

            distanceToHole = course.distanceToTarget(finalX, finalY);
            System.out.println("distanceToHole = " + distanceToHole);

            if (distanceToHole < bestDistance) {
                bestDistance = distanceToHole;
                bestVx = vx;
                bestVy = vy;
            }

            if (distanceToHole <= tRadius) {
                System.out.println("Found shot inside target radius");
                break;
            }
            i++;
        }

        System.out.println(" bestVx = " + bestVx + " bestVy = " + bestVy + " bestDistance = " + bestDistance);

        return new double[] { bestVx, bestVy };
    }
}