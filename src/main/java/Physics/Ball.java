package Physics;

public class Ball {
    private double weight = 0.0459; // kg
    private double x;
    private double y;
    private double xVelocity;
    private double yVelocity;


    public Ball(double[] y0) {
        this.x = y0[0];
        this.y = y0[1];
        this.xVelocity = y0[2];
        this.yVelocity = y0[3];
    }

    public double[] getState() {
        return new double[] {x, y, xVelocity, yVelocity};
    }

    public double getWeight() {
        return this.weight;
    }

    public void setPos(double[] nextY) {
        this.x = nextY[0];
        this.xVelocity = nextY[2];
        this.y = nextY[1];
        this.yVelocity = nextY[3];
    }

}
