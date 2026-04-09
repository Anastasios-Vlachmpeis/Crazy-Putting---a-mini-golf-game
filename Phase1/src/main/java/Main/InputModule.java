package Main;
import javafx.scene.control.*;
import javafx.scene.layout.*;

// This class is responsible for reading and validating input from The GUI

public class InputModule {
    private final TextField stepSizeField;
    private final TextField integrationTimeField;
    private final ComboBox<String> solverSelection;
    private final ComboBox<String> odeSelection;
    private final VBox paramPanel;

    public InputModule(TextField stepSizeField, TextField integrationTimeField, ComboBox<String> solverSelection, ComboBox<String> odeSelection, VBox paramPanel) {

        this.stepSizeField = stepSizeField;
        this.integrationTimeField = integrationTimeField;
        this.solverSelection = solverSelection;
        this.odeSelection = odeSelection;
        this.paramPanel = paramPanel;
    }  

    public InputData readInput() {
        String solver = solverSelection.getValue();
        String system = odeSelection.getValue();

        if (solver.equals("--Select--") || system.equals("--Select--")) { // Checking if dropdowns are selected
            throw new IllegalArgumentException("Please select a solver and an ODE system.");
        }

        double h;
        double tEnd;
        double[] params;

        try {
            h = Double.parseDouble(stepSizeField.getText());
            tEnd = Double.parseDouble(integrationTimeField.getText());

            params = paramPanel.getChildren().stream()
                .filter(n -> n instanceof TextField)
                .mapToDouble(n -> Double.parseDouble(((TextField) n).getText()))
                .toArray();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter valid numbers.");
        }

        if (h <= 0 || tEnd <= 0) {
            throw new IllegalArgumentException("Step size and integration time must be bigger than 0.");
        }

        return new InputData(solver, system, h, tEnd, params);
    }
}
