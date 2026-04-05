package Systems;

public class GolfCourse {
    public final double friction = 0.15;
    public final double targetX = 10.5;
    public final double targetY = 5.0;
    public final double targetR = 0.2;
    public final double startX = 0.0;
    public final double startY = 0.0;

    public double h(double x, double y) {
        return Math.sin(x) - (y / 7.0) + 0.5;
    }
}