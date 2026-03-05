public class Main {
    public static void main(String[] args) {
        //Example calculation
        Solver ExamplePopulation = new Solver(); //Create Solver object
        ODE populationGrowth = new ExampleModelPopulation(); //chosen model

        int samplesPerSecond = 10;
        ExamplePopulation.integrate(populationGrowth, 150, 0, 10, samplesPerSecond);
    }
    
}
