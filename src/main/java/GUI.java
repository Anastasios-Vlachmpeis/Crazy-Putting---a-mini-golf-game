import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class GUI extends Application  {
    //basic settings 
    private TextField stepSizeField = new TextField("0.01");
    private TextField integrationTimeField = new TextField("10");

    //dropdowns for solver selection and ODE selection
    private ComboBox<String> solverSelection = new ComboBox<>();
    private ComboBox<String> odeSelection = new ComboBox<>();

    //parameter panel - changes based on ODE selection
    private VBox paramPanel = new VBox(5);

    //two type of charts 
    private LineChart<Number, Number> timeSeriesChart; 
    private LineChart<Number, Number> phaseSpaceChart;

    @Override 
    public void start(Stage primaryStage) {
        VBox leftPanel = buildLeftPanel();
        TabPane tabPane = buildTabPane();

        //might change root layout later, when I add charts 
        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(tabPane);

        //make the window full-screen on launch
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());

        primaryStage.setScene(new Scene(root, 900, 600, Color.AZURE));
        primaryStage.setTitle("Phase 1 - Team 17");

        primaryStage.show();
    }

    private TabPane buildTabPane() {
        //time series chart
        NumberAxis tsX = new NumberAxis();
        NumberAxis tsY = new NumberAxis();
        tsX.setLabel("Time");
        tsY.setLabel("Value");
        timeSeriesChart = new LineChart<>(tsX, tsY);
        timeSeriesChart.setTitle("Time Series");
        timeSeriesChart.setAnimated(false);
        timeSeriesChart.setCreateSymbols(false);
 
        //phase space chart
        NumberAxis psX = new NumberAxis();
        NumberAxis psY = new NumberAxis();
        phaseSpaceChart = new LineChart<>(psX, psY);
        phaseSpaceChart.setTitle("Phase Space");
        phaseSpaceChart.setAnimated(false);
        phaseSpaceChart.setCreateSymbols(false);

        //put both charts in different tabs
        Tab timeSeriesTab = new Tab("Time Series", timeSeriesChart);
        timeSeriesTab.setClosable(false); //make sure user doesn't accidentally close the tab 
        Tab phaseSpaceTab = new Tab("Phase Space", phaseSpaceChart);
        phaseSpaceTab.setClosable(false);

        timeSeriesChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        phaseSpaceChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
 
        TabPane tabPane = new TabPane(timeSeriesTab, phaseSpaceTab);
        BorderPane.setMargin(tabPane, new Insets(10));
 
        return tabPane;
    }

    private VBox buildLeftPanel() {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setPrefWidth(220);

        //test system selection
        Label systemLabel = new Label("ODE System:");
        odeSelection.getItems().addAll("Lotka-Volterra", "SIR", "FitzHugh-Nagumo");
        odeSelection.setValue("--Select--");
        odeSelection.setMaxWidth(Double.MAX_VALUE);

        //solver selection
        Label solverLabel = new Label("Solver:");
        solverSelection.getItems().addAll("Euler", "Runge-Kutta");
        solverSelection.setValue("--Select--");
        solverSelection.setMaxWidth(Double.MAX_VALUE);

        //simulation settings
        Label simLabel = new Label("Simulation Settings:");
        Label stepLabel = new Label("Step Size (h):");
        stepSizeField.setMaxWidth(Double.MAX_VALUE);
        Label timeLabel = new Label("Integration Time:");
        integrationTimeField.setMaxWidth(Double.MAX_VALUE);

        //parameter panel - will be updated based on ODE selection
        Label paramLabel = new Label("Initial Conditions & Parameters:");

        //run button
        Button runButton = new Button("Run Simulation");
        runButton.setMaxWidth(Double.MAX_VALUE);
        runButton.setOnAction(e -> runSimulation());

        //for the default ODE:
        updateParameterFields(odeSelection.getValue(), paramPanel);

        //update paramPanel when ODE selection changes:
        odeSelection.setOnAction(e -> updateParameterFields(odeSelection.getValue(), paramPanel));

        leftPanel.getChildren().addAll(
            systemLabel, odeSelection,
            //new Separator(),
            solverLabel, solverSelection,
            //new Separator(),
            simLabel,
            stepLabel, stepSizeField,
            timeLabel, integrationTimeField,
            //new Separator(),
            paramLabel, paramPanel,
            //new Separator(),
            runButton
        );

        return leftPanel;
    }

    private void updateParameterFields(String system, VBox paramPanel) {
        paramPanel.getChildren().clear();

        switch (system) {
        
            case "Lotka-Volterra" -> {
                paramPanel.getChildren().addAll(
                    new Label("x₀ (prey):"), new TextField("10"),
                    new Label("y₀ (predator):"), new TextField("5"),
                    new Label("α (prey growth):"), new TextField("1.0"),
                    new Label("β (predation):"), new TextField("0.1"),
                    new Label("γ (predator loss):"),new TextField("1.5"),
                    new Label("δ (pred. growth):"), new TextField("0.075")
                );
            }
            case "SIR" -> {
                paramPanel.getChildren().addAll(
                    new Label("S₀ (susceptible):"), new TextField("0.99"),
                    new Label("I₀ (infected):"), new TextField("0.01"),
                    new Label("R₀ (recovered):"), new TextField("0.0"),
                    new Label("k (infection rate):"),new TextField("2.0"),
                    new Label("γ (recovery rate):"), new TextField("1.0"),
                    new Label("µ (birth/death):"), new TextField("0.001")
                );
            }
            case "FitzHugh-Nagumo" -> {
                paramPanel.getChildren().addAll(
                    new Label("V₀ (voltage):"), new TextField("0.0"),
                    new Label("W₀ (recovery):"), new TextField("0.0"),
                    new Label("a:"), new TextField("0.7"),
                    new Label("b:"), new TextField("0.8"),
                    new Label("ε (time scale):"), new TextField("0.05"),
                    new Label("I_ext (stimulus):"), new TextField("0.5")
                );
            }
        }
    }

    /* private void runSimulation() {
        //read parameters, run selected solver, and update charts: 
        double h = Double.parseDouble(stepSizeField.getText());
        double tEnd = Double.parseDouble(integrationTimeField.getText());
        String solver = solverSelection.getValue();
        String system = odeSelection.getValue();

        //read dynamic parameter fields in order
        double[] params = paramPanel.getChildren().stream()
            .filter(n -> n instanceof TextField)
            .mapToDouble(n -> Double.parseDouble(((TextField) n).getText()))
            .toArray();

        // TODO: pass params to input module which then passes it to ODE and to solver
        // TODO: Based on what input module/solver gives as results build charts from that 
    }  */

    private void runSimulation() {
        String solver = solverSelection.getValue();
        String system = odeSelection.getValue();

        if (solver.equals("--Select--") || system.equals("--Select--")) {        // Checking if dropdowns are selected
            displayAlert("Please select a solver and an ODE system.");
            return;
        }

        double h;
        double tEnd;
        double[] params;

        try {    // Checking the user is inputting valid numbers
        h = Double.parseDouble(stepSizeField.getText());
        tEnd = Double.parseDouble(integrationTimeField.getText());
        params = paramPanel.getChildren().stream()
            .filter(n -> n instanceof TextField)
            .mapToDouble(n -> Double.parseDouble(((TextField) n).getText()))
            .toArray();

        } catch (NumberFormatException e) {
            displayAlert("Please enter valid numbers.");
            return;
        }

        if (h <= 0 || tEnd <= 0) {
            displayAlert("Step size and integration time must be bigger than 0.");
            return;
        }

        //Building ODE Object
        ODE ode;
        double[] y0;

        // The initial conditions are set in params[0], params[1]. In case of SIR, there's three initial conditions y0 so we have to deal with it.
        switch (system) {
            case "Lotka-Volterra" -> {
               y0 = new double[]{params[0], params[1]};
               ode = new LotkaVolterra(params[2], params[3], params[4], params[5]);
            }
            case "SIR" -> {
                y0 = new double[]{params[0], params[1], params[2]};
                ode = new SIRModel(params[3], params[4], params[5]);
            }
            case "FitzHugh-Nagumo" -> {
                y0 = new double[]{params[0], params[1]};
                ode = new FitzHughNagumo(params[2], params[3], params[4], params[5]);
            }
            default -> {
                displayAlert("Unknown system.");
                return;
            }
        }
        
        Solver solverObj = new Solver();
        double[][] results = solverObj.integrate(ode, y0, 0.0, tEnd, h);
        
        
        // TODO system specific validation, solver selection (RK4?), Results prepared for plotting 
        
}
    
    private void displayAlert(String message) {        // Alert to be given in case one of the previous conditions are met
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Invalid Input");
    alert.setHeaderText(null);       // This removes the default header
    alert.setContentText(message);   // Main message shown (changes)
    alert.showAndWait();             // Displays alert. Pauses execution until user closes it.
    }

    public static void main(String[] args) {
        launch(args);
    }

}


