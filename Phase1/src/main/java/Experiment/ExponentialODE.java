package Experiment;
import Systems.ODE;
// Defines the ODE dy/dt = y, which has the exact solution y(t) = e^t.
// Used as the test system for the accuracy and runtime experiments.
public class ExponentialODE implements ODE {

    @Override
    public double[] getDerivative(double t, double[] y) {
        return new double[] { y[0] };// derivative equals the current value of y
    }
}