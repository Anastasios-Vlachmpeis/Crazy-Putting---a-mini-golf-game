package Examples;
import Systems.ODE;

public class ExampleModelPopulation implements ODE{
    private double growthRate = 0.1; //growthFactor per second

    @Override
    public double[] getDerivative(double t, double[] y){
        //In this case, the derivative is just a constant
        return new double[] {growthRate};
    }
    
}
