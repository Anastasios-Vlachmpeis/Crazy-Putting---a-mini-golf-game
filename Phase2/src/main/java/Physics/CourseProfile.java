package Physics;

public class CourseProfile {
    private final double miuK; // kinetic friction coeficient
    private final double miuS; // static friction coeficient, theoretically its more like a constant. I just put it here to have both mius in one place
    

    public CourseProfile(double miuK, double miuS) {
        this.miuK = miuK; 
        this.miuS = miuS; 
    }

    // function from the eg. in the manual 3.3 bullet point 2
    public double height(double x, double y) {
        return Math.sin((x + y) / 7.0) + 0.5;
    }

    // the derivative of height with respect to x (aka slope of x, aka downhill forse in the direction of x)
    public double dhdx(double x, double y) {
        //return -0.0001; Used this for bugfixing ~Stan
        return Math.cos((x + y) / 7.0) / 7.0;
    }

    // same but for y. the formula is the same tho. I added it just to make more sense later
    public double dhdy(double x, double y) {
        //return -0.0001; Used this for bugfixing ~Stan
        return Math.cos((x + y) / 7.0) / 7.0;
    }

    // check if the spot is in water
    public boolean isWater(double x, double y) {
        return height(x, y) < 0;
    }

    public double getMiuK() {
        return miuK;
    }

    public double getMiuS() {
        return miuS;
    }

    // later we can add getters for the tartet also
}

