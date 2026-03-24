public class Main {
    public static void main(String[] args) {
        //Example calculation
        Solver ExamplePopulation = new Solver(); //Create Solver object
        ODE populationGrowth = new ExampleModelPopulation(); //chosen model

        int samplesPerSecond = 10;
        double[] y0 = new double[] {150};
        ExamplePopulation.integrate(populationGrowth, y0, 0, 10, samplesPerSecond);
    }
    
}
