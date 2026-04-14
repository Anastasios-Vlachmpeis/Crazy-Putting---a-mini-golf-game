package GUI;

import javafx.scene.paint.Color;

public class ColorTerrain {

    public ColorTerrain() {
    }

    public Color isGrassColor(double h, double maxH) {
        double t = Math.min(h / maxH, 1.0); //we want to be between 0 and 1 for the height (we normalize it) -> thats why we also cap it at 1

       return Color.rgb(
            (int)(60  + t * 20),   
            (int)(210 - t * 60),
            (int)(80  + t * 20),   
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

    
    
}
