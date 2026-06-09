package domain.terrain;

/*
 * Calculates the terrain height and slopes
 * It handles both formula terrain and Perlin noise terrain
 */

import domain.terrain.PerlinNoise;
import domain.course.CourseBounds;

public final class TerrainHeightProvider {
    private final TerrainManipulation terrainManipulation;
    private final String terrainFormula;
    private final double[] targetValues;
    private final CourseBounds bounds;
    private final double borderSteepness;
    private final double epsilon;
    private final boolean usePerlinNoise;
    private final double widthScale;
    private final double heightScale;
    private final double noiseOffset;
    private final double globalElevation;

    public TerrainHeightProvider(
        TerrainManipulation terrainManipulation,
        String terrainFormula,
        double[] targetValues,
        CourseBounds bounds,
        double borderSteepness,
        double epsilon,
        boolean usePerlinNoise,
        double widthScale,
        double heightScale,
        double noiseOffset,
        double globalElevation
    ) {
        this.terrainManipulation = terrainManipulation;
        this.terrainFormula = terrainFormula;
        this.targetValues = targetValues;
        this.bounds = bounds;
        this.borderSteepness = borderSteepness;
        this.epsilon = epsilon;
        this.usePerlinNoise = usePerlinNoise;
        this.widthScale = widthScale;
        this.heightScale = heightScale;
        this.noiseOffset = noiseOffset;
        this.globalElevation = globalElevation;
    }

    public double height(double x, double y) {
        if (usePerlinNoise) {
            return PerlinNoise.noise((x + noiseOffset) * widthScale, (y + noiseOffset) * widthScale)
                * heightScale
                + globalElevation
                + 0.25;
        }

        return formulaHeight(x, y);
    }

    public double[] getDerivative(double x, double y) {
        return new double[] {dhdx(x, y), dhdy(x, y)};
    }

    public double dhdx(double x, double y) {
        if (x < bounds.minX()) return -1 * borderSteepness;
        if (x > bounds.maxX()) return borderSteepness;

        return (formulaHeight(x + epsilon, y) - formulaHeight(x, y)) / epsilon;
    }

    public double dhdy(double x, double y) {
        if (y < bounds.minY()) return -1 * borderSteepness;
        if (y > bounds.maxY()) return borderSteepness;

        return (formulaHeight(x, y + epsilon) - formulaHeight(x, y)) / epsilon;
    }

    public double formulaHeight(double x, double y) {
        return terrainManipulation.calculateHeight(terrainFormula, x, y, targetValues);
    }
}
