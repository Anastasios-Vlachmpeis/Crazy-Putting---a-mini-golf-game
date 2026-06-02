package SimpleGUI;

import javafx.scene.paint.Color;

public class ColorTerrain {

    public ColorTerrain() {
    }

    public Color isGrassColor(double h, double maxH) {
        if (h >= 3.0) {
            double t = clamp((h - 3.0) / 2.0);
            return Color.rgb(
                (int)(165 + t * 70),
                (int)(155 + t * 80),
                (int)(135 + t * 95),
                1.0
            );
        }

        if (h >= 1.5) {
            double t = clamp((h - 1.5) / 1.5);
            return Color.rgb(
                (int)(130 + t * 35),
                (int)(150 + t * 5),
                (int)(70 + t * 65),
                1.0
            );
        }

        double t = clamp(h / 1.5);
        return Color.rgb(
            (int)(35 + t * 95),
            (int)(110 + t * 105),
            (int)(45 + t * 25),
            1.0
        );
    }

    public Color isWaterColor(double h) {
        double depth = Math.min(Math.abs(h), 1.0); //we cap it at 1 so it doesn't get too dark 
            //if water is deeper blue is darker 
            return Color.rgb( //works like this: Color.rgb(red, green, blue, opacity)
                (int)(30  + depth * 20), //red pigments never very high 
                (int)(100 + depth * 50), //green pigments can be higher for shallower water to make it more like real water
                (int)(180 + depth * 60), //clearly the dominant pigment 
                1.0
            );
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    
    
}
