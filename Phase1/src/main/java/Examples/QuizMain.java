package Examples;

import Solvers.EulerSolver;
import Solvers.RungeKuttaSolver;
import Systems.NewSystem;
import Systems.ODE;

public class QuizMain {
    public static void main(String[] args) {
 
        ODE system = new NewSystem(10, 28, 2.7/* params here */);
 
        double[] y0 = {1.0, 1.0, 0.2 /* initial conditions here */ };
        double tEnd = 10.0/* integration time given in quiz */;
        double h = 0.01; // step size — leave this unless told otherwise
 
        // System.out.println("Runge-Kutta Solver");
        // RungeKuttaSolver rk = new RungeKuttaSolver();
        // rk.solve(system, y0, 0.0, tEnd, h); // final value printed inside solver
 
        System.out.println("\nEuler Solver");
        EulerSolver euler = new EulerSolver();
        euler.solve(system, y0, 0.0, tEnd, h); // final value printed inside solver
    }
    
}
