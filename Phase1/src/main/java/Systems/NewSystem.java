package Systems;
public class NewSystem implements ODE {
    private final double s; // whatever parameters the new system needs
    private final double p;
    private final double b;

    public NewSystem(double p1, double p2, double p3) {
        this.s = p1;
        this.p = p2;
        this.b = p3;
    }

    @Override
    public double[] getDerivative(double t, double[] y) {
        if (y == null || y.length < 3) {
            throw new IllegalArgumentException("New System expects state y with length ≥ 3...");
        }
        //variables
        double y0 = y[0];
        double y1 = y[1];
        double y2 = y[2];

        double dy0 = s*(y1 - y0) /* equation for first variable */;
        double dy1 = y0*(p - y2) - y1 /* equation for second variable */;
        double dy2 = (y0 * y1) - (b * y2) /* equation for third variable, if needed */;
        return new double[]{ dy0, dy1, dy2 };
    }
}