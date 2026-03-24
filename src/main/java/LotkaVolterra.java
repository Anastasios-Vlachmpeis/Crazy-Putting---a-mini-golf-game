/**
 * Lotka–Volterra predator–prey model
 * State: y[0] = x (prey), y[1] = y (predators)
 */
public class LotkaVolterra implements ODE {

    private final double a;
    private final double b;
    private final double c;
    private final double d;

    public LotkaVolterra(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    @Override
    public double[] getDerivative(double t, double[] y) {
        if (y == null || y.length < 2) {
            throw new IllegalArgumentException("LotkaVolterra expects state y with length ≥ 2 (prey x, predator y).");
        }
        double x = y[0];
        double pred = y[1];
        double dx = a * x - b * x * pred;
        double dy = c * x * pred - d * pred;
        return new double[] { dx, dy };
    }
}
