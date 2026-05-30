package SimpleGUI;

import Bots.*;

import GolfCourseData.GolfCourse;
//import ShotEngine.PhysicsShotSimulator;
import Solvers.RungeKuttaSolver;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ShotPanel {
    private GUI_phase2 gui;
    private final GameCanvas gameCanvas;

    private Label shotCountLabel;
    private Label ballPosLabel;
    private Label distanceLabel;
    private Label shotResultLabel;

    private TextField manualVxField;
    private TextField manualVyField;

    private int shotCount = 0;

    public ShotPanel(GUI_phase2 gui, GameCanvas gameCanvas) {
        this.gui = gui;
        this.gameCanvas = gameCanvas;
    }

    /**
     * Builds top half of the panel:
     * 1. Labels to show current shot count, ball position, distance to target
     * 2. Shot result (e.g. "In water!", "On green, 2.3m from target", "In hole!")
     * 3. Manual velocity input + "Fire" button as an alternative to mouse-drag
     * 4. Bot buttons: "SimpleBot shot" and "MLBot shot" to play against
     */
    public VBox buildShotSection() {
        VBox section = new VBox(6);
        section.setPadding(new Insets(12));

        section.setPrefWidth(300);

        Label title = sectionLabel("Shot Info & Game State");

        // initial values, updated after every shot in onShotLanded() and after reset in
        // onReset()
        shotCountLabel = hudLabel("Shots: 0");
        ballPosLabel = hudLabel("Ball: (0.00, 0.00)");
        distanceLabel = hudLabel("Distance to target: —");
        shotResultLabel = new Label("");
        shotResultLabel.setFont(Font.font(13));
        shotResultLabel.setWrapText(true);

        // manual velocity input
        manualVxField = field("0.0");
        manualVyField = field("0.0");

        Button fireBtn = new Button("Fire");
        fireBtn.setMaxWidth(Double.MAX_VALUE);
        fireBtn.setOnAction(e -> onFireManual());

        // bot buttons
        Button simpleBotBtn = new Button("SimpleBot shot");
        simpleBotBtn.setMaxWidth(Double.MAX_VALUE);
        simpleBotBtn.setOnAction(e -> onSimpleBotShot());
        //simpleBotBtn.setOnAction(e -> shotResultLabel.setText("SimpleBot not yet connected."));

        Button mlBotBtn = new Button("MLBot shot");
        mlBotBtn.setMaxWidth(Double.MAX_VALUE);
        mlBotBtn.setOnAction(e -> onMLBotShot());
        // mlBotBtn.setOnAction(e -> shotResultLabel.setText("MLBot not yet
        // connected."));

        Button hillBotBtn = new Button("HillBot shot");
        hillBotBtn.setMaxWidth(Double.MAX_VALUE);
        hillBotBtn.setOnAction(e -> onHillBotShot());

        Button resetBtn = new Button("Reset ball");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(e -> onReset());

        Region spacer = new Region(); // do this so it looks cleaner
        spacer.setPrefHeight(8);

        // Separator separator1 = new Separator();
        // separator1.setPadding(new Insets(6, 0, 6, 0));

        // Separator separator2 = new Separator();
        // separator2.setPadding(new Insets(6, 0, 6, 0));

        // Separator separator3 = new Separator();
        // separator3.setPadding(new Insets(6, 0, 6, 0));

        section.getChildren().clear();
        section.getChildren().addAll(
            title,
            shotCountLabel,
            ballPosLabel,
            //distanceLabel,
            shotResultLabel,
            separator(),
            smallLabel("Manual vx (m/s):"), manualVxField,
            smallLabel("Manual vy (m/s):"), manualVyField,
            fireBtn,
            separator(),
            simpleBotBtn,
            mlBotBtn,
            separator(),
            hillBotBtn,
            separator(),
            resetBtn
        );

        return section;
    }

    

    // helper method
    private Separator separator() {
        Separator sep = new Separator();
        sep.setPadding(new Insets(6, 0, 6, 0));
        return sep;
    }

    // Asks bots for a shot, then triggers it
    private void onSimpleBotShot() {
        GolfCourse course = gui.getCourseRelated();
        double[] ballState = gui.getBall().getState();
        course.setBallPosition(ballState[0], ballState[1]);
        RungeKuttaSolver solver = new RungeKuttaSolver();
        //PhysicsShotSimulator simulator = new PhysicsShotSimulator(course, solver);
        SimpleBot bot = new SimpleBot(course, solver);
        //SimpleBot bot = new SimpleBot(course, simulator);

        double[] velocity = bot.shoot();
        double vx = velocity[0];
        double vy = velocity[1];

        shotResultLabel.setText(String.format("SimpleBot shot: vx = %.5f, vy = %.5f", vx, vy));
        shotResultLabel.setTextFill(Color.BLACK);

        recordShot();
        gameCanvas.fireShot(vx, vy);
    }

    private void onMLBotShot() {
        GolfCourse course = gui.getCourseRelated();
        RungeKuttaSolver solver = new RungeKuttaSolver();
        //PhysicsShotSimulator simulator = new PhysicsShotSimulator(course, solver);
        MLBot bot = new MLBot(course, solver);
        //MLBot bot = new MLBot(course, simulator);

        double[] velocity = bot.shoot();
        double vx = velocity[0];
        double vy = velocity[1];

        shotResultLabel.setText(String.format("MLBot shot: vx = %.5f, vy = %.5f", vx, vy)); // this doesnt work heeelp :( ~Damian
        shotResultLabel.setTextFill(Color.BLACK);

        recordShot();
        gameCanvas.fireShot(vx, vy);

    }

    private void onHillBotShot() {
        GolfCourse course = gui.getCourseRelated();
        double[] ballState = gui.getBall().getState();
        course.setBallPosition(ballState[0], ballState[1]);
        RungeKuttaSolver solver = new RungeKuttaSolver();
        HillBot bot = new HillBot(course, solver);

        double[] velocity = bot.shoot();
        double vx = velocity[0];
        double vy = velocity[1];

        shotResultLabel.setText(String.format("HillBot shot: vx = %.5f, vy = %.5f", vx, vy)); // this doesnt work heeelp :( ~Damian
        shotResultLabel.setTextFill(Color.BLACK);

        recordShot();
        gameCanvas.fireShot(vx, vy);

    }

    // reads manual velocity fields and triggers a shot with those values
    private void onFireManual() {
        try {
            double vx = Double.parseDouble(manualVxField.getText().trim());
            double vy = Double.parseDouble(manualVyField.getText().trim());
            recordShot();
            gameCanvas.fireShot(vx, vy);
        } catch (NumberFormatException ex) {
            shotResultLabel.setText("Enter valid numbers for vx and vy.");
            shotResultLabel.setTextFill(Color.SALMON);
        }
    }

    // resets ball to start position and clears shot count and result label
    private void onReset() {
        GolfCourse course = gui.getCourseRelated();
        double[] startPos = course.getOriginalStartPosition();
        course.setBallPosition(startPos[0], startPos[1]);
        double[] resetState = new double[] { startPos[0], startPos[1], 0.0, 0.0 };
        System.out.println("OnReset triggerd");
        gui.getBall().setPos(resetState);
        gameCanvas.drawObjects(resetState, null, 0.0, 0.0);

        shotResultLabel.setText("");
        
        shotCount = 0; 

        update(resetState, shotCount, gui.distanceToTarget(resetState));
    }

    // called by GameCanvas after every shot completes to update the shot count,
    // ball position, distance to target, and shot result message
    public void onShotLanded(double[] finalState, double dist, boolean inWater, boolean won) {
        update(finalState, shotCount, dist);

        if (won) {
            shotResultLabel.setText("Hole in one! (" + shotCount + " shots)");
            shotResultLabel.setTextFill(Color.LIGHTGREEN);
            System.out.println("game Won");
        } 
        else if (inWater) {
            //penalty: count an extra shot and reset position
            recordShot(); 
            onReset();
            shotResultLabel.setText("In the water! +1 penalty. \nReplaying from build.");
            shotResultLabel.setTextFill(Color.CORNFLOWERBLUE);
            System.out.println("in Water");
            System.out.println(inWater);
        } 
        else {
            shotResultLabel.setText(String.format("Resting %.2f m from target.", dist));
            shotResultLabel.setTextFill(Color.BLACK);
        }
    }

    // updates all labels
    public void update(double[] ballState, int shots, double dist) {
        shotCountLabel.setText("Shots: " + shots);

        ballPosLabel.setText(String.format("Ball: (%.2f, %.2f)", ballState[0], ballState[1]));

        //distanceLabel.setText(String.format("Distance: %.2f m", dist));
    }

    // updates the course for the mlbot
    // public void updateMLBotCourse(double[] ballState, int shots, double dist, GUI_phase2 gui) {
    //     shotCountLabel.setText("Shots: " + shots);
    //     ballPosLabel.setText(String.format("Ball: (%.2f, %.2f)", ballState[0], ballState[1]));
    //     distanceLabel.setText(String.format("Distance: %.2f m", dist));
    //     shotPanelUpdateCourse(gui);
    // }

    // public void shotPanelUpdateCourse(GUI_phase2 gui) {
    //     this.gui = gui;
    // }

    // increment shot count and update the label
    public void recordShot() {
        shotCount++;
        shotCountLabel.setText("Shots: " + shotCount);
    }

    public void resetShotCount() {
        shotCount = 0;
        shotCountLabel.setText("Shots: 0");
        shotResultLabel.setText("");
    }

    // create section titles
    private Label sectionLabel(String text) {
        Label label = new Label(text);

        label.setFont(Font.font(17));
        label.setTextFill(Color.BLACK);

        return label;
    }

    // create small labels for fields
    private Label smallLabel(String text) {
        Label label = new Label(text);

        label.setFont(Font.font(13));
        label.setTextFill(Color.BLACK);

        return label;
    }

    private Label hudLabel(String text) {
        Label label = new Label(text);

        label.setFont(Font.font(13));
        label.setTextFill(Color.BLACK);

        return label;
    }

    private TextField field(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setMaxWidth(Double.MAX_VALUE);

        return tf;
    }

}
