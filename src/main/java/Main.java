public class Main {
    public static void main(String[] args) {
        // //Example calculation
        // Solver ExamplePopulation = new Solver(); //Create Solver object
        // ODE populationGrowth = new ExampleModelPopulation(); //chosen model

        // int samplesPerSecond = 10;
        // double[] y0 = new double[] { 150 };
        // ExamplePopulation.integrate(populationGrowth, y0, 0, 10, samplesPerSecond);

        // Lotka Volterra example calculation
        Solver lotkaVolterraSolver = new Solver();
        ODE ecosistem = new LotkaVolterra(1, 0.1, 0.075, 1.5);
        int samplesPerSecond = 10;
        double[] y0 = new double[] { 150, 20 }; // {prey, predators}
        lotkaVolterraSolver.integrate(ecosistem, y0, 0, 10, samplesPerSecond);
    }

}
