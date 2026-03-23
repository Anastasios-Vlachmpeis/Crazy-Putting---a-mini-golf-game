/**
 * SIR epidemic model with demographics
 * State: y[0] = S (susceptible), y[1] = I (infected), y[2] = R (recovered)
 */
public class SIRModel implements ODE {

    private final double k;
    private final double g;
    private final double m;

    public SIRModel(double k, double g, double m) {
        this.k = k;
        this.g = g;
        this.m = m;
    }

    @Override
    public double[] getDerivative(double t, double[] y) {
        if (y == null || y.length < 3) {
            throw new IllegalArgumentException("SIRModel expects state y with length larger than 3...");
        }
        double s = y[0];
        double i = y[1];
        double r = y[2];
        double sDot = -k * s * i + m * (1.0 - s);
        double iDot = k * s * i - (g + m) * i;
        double rDot = g * i - m * r;
        return new double[] { sDot, iDot, rDot };
    }
}
