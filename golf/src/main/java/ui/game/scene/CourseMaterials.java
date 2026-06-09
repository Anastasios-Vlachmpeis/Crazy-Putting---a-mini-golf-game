package ui.game.scene;

/*
 * Creates materials and small textures for the 3D scene
 * Used for grass, sand, trees, flags, and other objects
 */

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

final class CourseMaterials {
    private static final double TERRAIN_SHADOW_FLOOR = 0.22;
    private static final double OBJECT_SHADOW_FLOOR = 0.18;

    private CourseMaterials() {
    }

    static PhongMaterial createGrassMaterial() {
        PhongMaterial grassMat = new PhongMaterial();
        grassMat.setDiffuseMap(createHeightShadeTexture(1.0));
        grassMat.setSelfIlluminationMap(createHeightShadeTexture(TERRAIN_SHADOW_FLOOR));
        grassMat.setSpecularColor(Color.TRANSPARENT);
        return grassMat;
    }

    static double getHeightShadeTextureX(double elevation) {
        double greenIntensity = 0.4 + (elevation * 0.05);
        greenIntensity = clamp(greenIntensity, 0.1, 0.9);
        return (greenIntensity - 0.1) / 0.8;
    }

    static PhongMaterial createSandMaterial() {
        PhongMaterial material = new PhongMaterial(Color.web("#cfae62"));
        material.setDiffuseMap(createSandTexture(1.0));
        material.setSelfIlluminationMap(createSandTexture(OBJECT_SHADOW_FLOOR));
        material.setSpecularColor(Color.TRANSPARENT);
        return material;
    }

    static PhongMaterial createLitMaterial(Color color) {
        PhongMaterial material = new PhongMaterial(color);
        material.setSelfIlluminationMap(createSolidColorTexture(color, OBJECT_SHADOW_FLOOR));
        material.setSpecularColor(Color.TRANSPARENT);
        return material;
    }

    static PhongMaterial createUnlitMaterial(Color color) {
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(color);
        material.setSelfIlluminationMap(createSolidColorTexture(color, 1.0));
        return material;
    }

    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static WritableImage createHeightShadeTexture(double brightness) {
        int width = 256;
        WritableImage image = new WritableImage(width, 1);
        PixelWriter writer = image.getPixelWriter();

        for (int x = 0; x < width; x++) {
            double greenIntensity = 0.1 + (x / (double) (width - 1)) * 0.8;
            writer.setColor(x, 0, Color.color(0.2 * brightness, greenIntensity * brightness, 0.2 * brightness));
        }

        return image;
    }

    private static WritableImage createSandTexture(double brightness) {
        int size = 96;
        WritableImage image = new WritableImage(size, size);
        PixelWriter writer = image.getPixelWriter();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double grain = pseudoNoise(x, y);
                double ripple = 0.5 + 0.5 * Math.sin((x * 0.22) + (y * 0.08));
                double shade = clamp((grain * 0.75) + (ripple * 0.25), 0.0, 1.0);

                int red = (int) ((180 + shade * 35) * brightness);
                int green = (int) ((145 + shade * 30) * brightness);
                int blue = (int) ((70 + shade * 22) * brightness);
                writer.setColor(x, y, Color.rgb(red, green, blue));
            }
        }

        return image;
    }

    private static WritableImage createSolidColorTexture(Color color, double brightness) {
        WritableImage image = new WritableImage(1, 1);
        image.getPixelWriter().setColor(
            0,
            0,
            Color.color(
                color.getRed() * brightness,
                color.getGreen() * brightness,
                color.getBlue() * brightness,
                color.getOpacity()
            )
        );
        return image;
    }

    private static double pseudoNoise(int x, int y) {
        int value = x * 374761393 + y * 668265263;
        value = (value ^ (value >> 13)) * 1274126177;
        value = value ^ (value >> 16);
        return (value & 0xffff) / 65535.0;
    }
}
