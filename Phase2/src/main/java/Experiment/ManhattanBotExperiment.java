package Experiment;

import Bots.ManhattanBot;
import GolfCourseData.GolfCourse;
import ShotEngine.ShotSimulatorV2;
import Solvers.RungeKuttaSolver;
import Systems.GolfODE;

public class ManhattanBotExperiment {
    public static void main(String[] args) throws Exception {
        long seed = "Groningen".hashCode();

        // // create course
        // GolfCourse course = new GolfCourse(0, 0);
        // course.usePerlinNoise = true;
        // RandomCourseGenerator.generateSeededCourse(course, seed);

        GolfCourse course = new GolfCourse();
        course.loadFromJson("src/main/java/Presets/Phase3Format/WaterExperiment.json");

        GolfODE golfODE = new GolfODE(course);

        // create simulator and solver
        ShotSimulatorV2 simulator = new ShotSimulatorV2();
        RungeKuttaSolver solver = new RungeKuttaSolver();

        // get targer
        double[] target = course.getTargetXYR();
        double holeR = target[2];

        int finishedGames = 0; // nr of times the bot got the ball in the hole
        double nrShots = 0;
        double nrIterations = 0;
        int waterHits = 0;

        for (int i = 0; i < 1000; i++) {
            // reset ball for each new game
            double[] originalStart = course.getOriginalStartPosition();
            course.setBallPosition(originalStart[0], originalStart[1]);
            ManhattanBot bot = new ManhattanBot(course, solver);

            for (int shot = 1; shot <= 10; shot++) {
                double[] v = bot.shoot();
                nrIterations += bot.getSimulationCount();

                // simulate
                double[] ballPosition = course.getStartPosition();
                double[] startState = { ballPosition[0], ballPosition[1], v[0], v[1] };
                double[][] fullShotTrajectory = simulator.schoot(golfODE, new RungeKuttaSolver(), startState, 0.01);
                double[] finalState = fullShotTrajectory[fullShotTrajectory.length - 1];

                // Extract the final X and Y coordinates
                // Note: Index 0 is time (t), Index 1 is X, Index 2 is Y
                double fx = finalState[1];
                double fy = finalState[2];
                double dist = course.distanceToTarget(fx, fy);

                if (dist <= holeR) {
                    System.out.println("GAME");
                    nrShots += shot;
                    finishedGames++;
                    break;
                }
                if (course.isWater(fx, fy)) {
                    waterHits++;
                    nrShots++;
                    course.setBallPosition(ballPosition[0], ballPosition[1]);
                } else {
                    course.setBallPosition(fx, fy);
                }
            }
        }
        double avg = nrShots / finishedGames;
        double avgIterations = nrIterations / finishedGames;
        System.out.println("Finished games: " + finishedGames);
        System.out.println("Avg Shots: " + avg);
        System.out.println("Avg iterations: " + avgIterations);
        System.out.println("Avg water penalties per game: " + ((double) waterHits / 1000));
    }
}
