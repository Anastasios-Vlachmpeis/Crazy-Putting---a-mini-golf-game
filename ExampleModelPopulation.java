public class ExampleModelPopulation implements ODE{
    private double growthRate = 0.1; //growthFactor per second

    @Override
    public double getDerivative(double t){
        //In this case, the derivative is just a constant
        return growthRate;
    }
    
}
