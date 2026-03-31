package Systems;
public class NewSystem implements ODE {
    private final double p1; // whatever parameters the new system needs

    public NewSystem(double p1) {
        this.p1 = p1;
    }

    @Override
    public double[] getDerivative(double t, double[] y) {
        double dy0 = 1/* equation for first variable */;
        double dy1 = 1/* equation for second variable */;
        double dy2 = 1/* equation for third variable, if needed */;
        return new double[]{ dy0, dy1, dy2};
    }
}