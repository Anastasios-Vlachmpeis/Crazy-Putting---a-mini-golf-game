package GolfCourseData.RandomTerrainGeneration;

import java.util.Random;

public class PerlinNoise {
    private static int[] P = new int[512];
    
    // Call this method whenever a new game starts with a new seed
    public static void initializeSeed(long seed) {
        int[] permutation = new int[256];
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }
        
        // Shuffle the array using the specific seed
        Random seededRandom = new Random(seed);
        for (int i = 0; i < 256; i++) {
            int swapIndex = seededRandom.nextInt(256);
            int temp = permutation[i];
            permutation[i] = permutation[swapIndex];
            permutation[swapIndex] = temp;
        }
        
        // Duplicate the array to prevent overflow errors during terrain math
        for (int i = 0; i < 256; i++) {
            P[i] = permutation[i];
            P[256 + i] = permutation[i];
        }
    }

    public static double noise(double x, double y) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        
        x -= Math.floor(x);
        y -= Math.floor(y);
        
        double u = fade(x);
        double v = fade(y);
        
        int A = P[X] + Y, B = P[X + 1] + Y;
        
        return lerp(v, lerp(u, grad(P[A], x, y), grad(P[B], x - 1, y)),
                       lerp(u, grad(P[A + 1], x, y - 1), grad(P[B + 1], x - 1, y - 1)));
    }

    private static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private static double lerp(double t, double a, double b) { return a + t * (b - a); }
    
    private static double grad(int hash, double x, double y) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : 0;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}