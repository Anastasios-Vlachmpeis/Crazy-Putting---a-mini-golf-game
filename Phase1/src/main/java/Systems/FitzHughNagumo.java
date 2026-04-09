package Systems;
/**
 * FitzHugh–Nagumo model
 * State: y[0] = V (membrane voltage), y[1] = W (recovery)
 */
public class FitzHughNagumo implements ODE {

    private final double a;
    private final double b;
    private final double e;
    private final double iExt;

    public FitzHughNagumo(double a, double b, double e, double iExt) {
        this.a = a;
        this.b = b;
        this.e = e;
        this.iExt = iExt;
    }

    //Typical values: e - 0.05, a and b - 1, iExt as needed
    public FitzHughNagumo() {
        this(1.0, 1.0, 0.05, 0.0);
    }

    @Override
    public double[] getDerivative(double t, double[] y) {
        if (y == null || y.length < 2) {
            throw new IllegalArgumentException("FitzHughNagumo expects state y with length ≥ 2!");
        }
        double v = y[0];
        double w = y[1];
        double vDot = v - (v * v * v) / 3.0 - w + iExt;
        double wDot = e * (v + a - b * w);
        return new double[] { vDot, wDot };
    }
}
