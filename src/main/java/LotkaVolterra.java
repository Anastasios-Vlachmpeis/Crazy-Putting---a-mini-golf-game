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
        double prey = y[0];
        double pred = y[1];
        // a*prey - prey reproduce on their own. b*prey*pred - how strong predators
        // reduce prey
        // rate of change in prey = prey births - prey deaths due to predators
        double dx = a * prey - b * prey * pred;
        // c*prey*pred - predators grow when eating prey. d*pred - predator death rate
        // rate of change in predators = predator grows from eatin prey - predator death due to starvation/ lack of prey
        double dy = c * prey * pred - d * pred;
        return new double[] { dx, dy };
    }
}
