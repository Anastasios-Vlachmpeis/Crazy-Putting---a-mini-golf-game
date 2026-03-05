public class ExampleModelPopulation implements ODE{
    private double growthRate = 0.1; //growthFactor per second

    @Override
    public double getDerivative(double t, double y){
        //derivative example equation: growthRate * y
        return growthRate * y;
    }
    
}
