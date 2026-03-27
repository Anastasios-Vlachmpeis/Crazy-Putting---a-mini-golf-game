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

    private InputModule inputModule;

    @Override 
    public void start(Stage primaryStage) {
        VBox leftPanel = buildLeftPanel();

        inputModule = new InputModule(stepSizeField, integrationTimeField, solverSelection, odeSelection, paramPanel);

        //make sure charts update automatically when user changes input
        autoRun();

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

    private void autoRun() {
        //text fields —> trigger when user leaves field
        stepSizeField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) tryRunSimulation();
        });

        integrationTimeField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) tryRunSimulation();
        });

        //for dropdowns —> trigger immediately
        solverSelection.valueProperty().addListener((obs, oldVal, newVal) -> tryRunSimulation());
        odeSelection.valueProperty().addListener((obs, oldVal, newVal) -> tryRunSimulation());
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
        psX.setLabel("Variable 1");
        psY.setLabel("Variable 2");
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

        //for the default ODE:
        updateParameterFields(odeSelection.getValue(), paramPanel);

        //update paramPanel when ODE selection changes:
        odeSelection.setOnAction(e -> updateParameterFields(odeSelection.getValue(), paramPanel));

        leftPanel.getChildren().addAll(
            systemLabel, odeSelection,
            new Separator(),
            solverLabel, solverSelection,
            new Separator(),
            simLabel,
            stepLabel, stepSizeField,
            timeLabel, integrationTimeField,
            new Separator(),
            paramLabel, paramPanel,
            new Separator()
        );

        return leftPanel;
    }


    private void updateParameterFields(String system, VBox paramPanel) {
        paramPanel.getChildren().clear();

        switch (system) {
        
            case "Lotka-Volterra" -> {
                addParamField("x₀ (prey):", "10", paramPanel);
                addParamField("y₀ (predator):", "5", paramPanel);
                addParamField("α (prey growth rate):", "1.0", paramPanel);
                addParamField("β (predation):", "0.1", paramPanel); //rate at which predators eat prey (prey loss)
                addParamField("γ (predator death rate):", "1.5", paramPanel);
                addParamField("δ (predator growth rate):", "0.075", paramPanel);
            }
            case "SIR" -> {
                addParamField("S₀ (susceptible):", "0.99", paramPanel);
                addParamField("I₀ (infected):", "0.01", paramPanel);
                addParamField("R₀ (recovered):", "0.0", paramPanel);
                addParamField("k (infection rate):", "2.0", paramPanel);
                addParamField("γ (recovery rate):", "1.0", paramPanel);
                addParamField("µ (birth/death):", "0.001", paramPanel);
            }
            case "FitzHugh-Nagumo" -> {
                addParamField("V₀ (voltage):", "0.0", paramPanel);
                addParamField("W₀ (recovery):", "0.0", paramPanel);
                addParamField("a:", "1.0", paramPanel);                   // Proper
                addParamField("b:", "1.0", paramPanel);
                addParamField("ε (time scale):", "0.05", paramPanel);
                addParamField("I_ext (stimulus):", "0.5", paramPanel);
            }
        }
    }

    private void addParamField(String labelText, String defaultValue, VBox panel) {
        TextField field = new TextField(defaultValue);
        field.setMaxWidth(Double.MAX_VALUE);
        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) tryRunSimulation();
        });
        panel.getChildren().addAll(new Label(labelText), field);
    }

    private void tryRunSimulation() {
        // Don't run if user hasn't selected a system or solver yet
        if (odeSelection.getValue().equals("--Select--") ||
            solverSelection.getValue().equals("--Select--")) return;

        try {
            runSimulation();
        } catch (NumberFormatException e) {
            
        }
    }

    private void runSimulation() {
        InputData input;

        try {
            input = inputModule.readInput();
        } catch (IllegalArgumentException e) {
            displayAlert(e.getMessage());
            return;
        }

        String solver = input.solver;
        String system = input.system;
        double h = input.h;
        double tEnd = input.tEnd;
        double[] params = input.params;

        ODE ode;
        double[] y0;

        try {
            ode = ODEFactory.createODE(system, params);
        } catch (IllegalArgumentException e) {
            displayAlert(e.getMessage());
            return;
        }
        
        switch (system) {
            case "Lotka-Volterra" -> y0 = new double[]{params[0], params[1]};
            case "SIR" -> y0 = new double[]{params[0], params[1], params[2]};
            case "FitzHugh-Nagumo" -> y0 = new double[]{params[0], params[1]};
            default -> {
                displayAlert("Unknown system.");
                return;
            }
        }

        double[][] results;

        switch (solver) {
            case "Euler" -> {
                EulerSolver solverObj = new EulerSolver();
                results = solverObj.integrate(ode, y0, 0.0, tEnd, h);
            }

            case "Runge-Kutta" -> {
                RungeKuttaSolver solverObj = new RungeKuttaSolver();
                results = solverObj.solve(ode, y0, 0.0, tEnd, h);
            }

            default -> {
                displayAlert("Unknown solver.");
                return;
            }
        }  

        updateCharts(system, results);
           
    }
    
    private void displayAlert(String message) { // Alert to be given in case one of the previous conditions are met
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);// This removes the default header
        alert.setContentText(message); // Main message shown (changes)
        alert.showAndWait(); // Displays alert & Pauses execution until user closes it
    }

    private void updateCharts(String system, double[][] results) {
        timeSeriesChart.getData().clear();
        phaseSpaceChart.getData().clear();

        //need to set x-axis because of Math.ceil() in borh solvers (or else we have a weird white space at the end of the time series chart)
            // Get actual start and end time from the results
            double tStart = results[0][0];
            double tEnd   = results[results.length - 1][0];

            // Lock the x-axis to exactly [tStart, tEnd]
            NumberAxis xAxis = (NumberAxis) timeSeriesChart.getXAxis();
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(tStart);
            xAxis.setUpperBound(tEnd);
            xAxis.setTickUnit((tEnd - tStart) / 10);

        //variable names per system 
        String [] varNames = switch (system) {
            case "Lotka-Volterra" -> new String[]{"Prey (x)", "Predator (y)"};
            case "SIR" -> new String[]{"Susceptible (S)", "Infected (I)", "Recovered (R)"};
            case "FitzHugh-Nagumo" -> new String[]{"Voltage (V)", "Recovery (W)"};
            default -> new String[]{"y0", "y1", "y2"};
        };

        int numVars = results[0].length - 1; //first column is time

        //time series: one line per variable 
        XYChart.Series<Number, Number>[] timeSeries = new XYChart.Series[numVars];
        for (int i = 0; i < numVars; i++) {
            timeSeries[i] = new XYChart.Series<>();
            timeSeries[i].setName(varNames[i]);
        }

        for (double[] row : results) {
            double t = row[0]; //in both Euler and RK time is in 1. column 
            for (int i = 0; i < numVars; i++) {
                timeSeries[i].getData().add(new XYChart.Data<>(t, row[i + 1]));
            }
        }

        for (XYChart.Series<Number, Number> series : timeSeries) {
            timeSeriesChart.getData().add(series);
        }

        //phase space: var[1] vs var[2] (skip time at index 0)
        XYChart.Series<Number, Number> phaseSeries = new XYChart.Series<>();
        phaseSeries.setName(varNames[0] + " vs " + varNames[1]);
        phaseSpaceChart.getXAxis().setLabel(varNames[0]);
        phaseSpaceChart.getYAxis().setLabel(varNames[1]);

        for (double [] row : results) {
            phaseSeries.getData().add(new XYChart.Data<>(row[1], row[2]));
        }
        phaseSpaceChart.getData().add(phaseSeries);

    }

    public static void main(String[] args) {
        launch(args);
    }

}


