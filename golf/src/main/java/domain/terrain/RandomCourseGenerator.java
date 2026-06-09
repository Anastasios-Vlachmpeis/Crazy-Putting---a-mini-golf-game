package domain.terrain;

import domain.course.GolfCourse;
import java.util.Random;

public class RandomCourseGenerator {

    public static void generateSeededCourse(GolfCourse course, long seed) {
        
        PerlinNoise.initializeSeed(seed);
        
        Random seededRandom = new Random(seed);
        
        // Because the randomizer is seeded, this offset will always be 
        // the exact same number for this specific seed
        course.noiseOffset = seededRandom.nextDouble() * 10000.0;
        
        // The start and target positions will also be identical every time
        double startX = -5.0 + (seededRandom.nextDouble() * 5.0);
        double startY = -5.0 + (seededRandom.nextDouble() * 5.0);
        course.setOriginalStartPosition(startX, startY);
        course.setBallPosition(startX, startY);

        double targetX = 5.0 + (seededRandom.nextDouble() * 5.0);
        double targetY = 5.0 + (seededRandom.nextDouble() * 5.0);
        course.setTargetPosition(targetX, targetY); 
        course.removeObstaclesInWater();
    }
}
