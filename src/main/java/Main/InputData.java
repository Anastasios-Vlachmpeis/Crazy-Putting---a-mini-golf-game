package Main;

// Works as a data container for all the user input

public class InputData {
    public final String solver;
    public final String system;
    public final double h;
    public final double tEnd;
    public final double[] params;

    public InputData(String solver, String system, double h, double tEnd, double[] params) {
        this.solver = solver;
        this.system = system;
        this.h = h;
        this.tEnd = tEnd;
        this.params = params;
    }  
}
