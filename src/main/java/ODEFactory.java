// This class is created in order to prevent GUI from constructing the ODE (And therefore meeting the "independent" requirement)

public class ODEFactory {

    public static ODE createODE(String system, double[] params) {

        // The initial conditions are set in params[0], params[1]. In case of SIR, there's three initial conditions y0 so we have to deal with it.
        switch (system) {
            case "Lotka-Volterra":
                return new LotkaVolterra(params[2], params[3], params[5], params[4]);   //Switched 4 adn 5 to correct a mistake

            case "SIR":
                return new SIR(params[3], params[4], params[5]);

            case "FitzHugh-Nagumo":
                return new FitzHughNagumo(params[2], params[3], params[4], params[5]);

            /* case "NewSystem":
                return new NewSystem(params[2]); */

            default:
                throw new IllegalArgumentException("Unknown system.");
        }
    }
}
