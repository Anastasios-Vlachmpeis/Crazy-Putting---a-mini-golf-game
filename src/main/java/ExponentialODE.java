public class ExponentialODE implements ODE {

    @Override
    public double[] getDerivative(double t, double[] y) {
        return new double[] { y[0] };
    }
}