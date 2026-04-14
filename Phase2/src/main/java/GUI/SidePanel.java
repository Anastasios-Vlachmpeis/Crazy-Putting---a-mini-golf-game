package GUI;

import GolfCourseData.*;
import Physics.CourseProfile;
import Main.*;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;
 
import java.io.File;

public class SidePanel {
    //needed to fire refreshCourse (after loading new course) and trigger shots and bot runs from the side panel
    private final GUI_phase2 gui; //don't do this ---------------------------
    private final GameCanvas gameCanvas;

    //elements about course info and loading
    private TextField heightFormulaField;
    private TextField miuKField; //kinetic friction
    private TextField miuSField; //static friction
    private TextField targetXField;
    private TextField targetYField;
    private TextField targetRField; //radius of target area
    private TextField startXField;
    private TextField startYField;
    private Label courseStatusLabel; //displays info about the current course (e.g. "Course loaded successfully!" or "Error loading course: ...")

    //elements about the current shot and game state
    private Label shotCountLabel;
    private Label ballPosLabel;
    private Label distanceLabel;
    private Label shotResultLabel;
    private TextField manualVxField;
    private TextField manualVyField;

    private int shotCount = 0;

    public SidePanel(GUI_phase2 gui, GameCanvas gameCanvas) {
        this.gui = gui;
        this.gameCanvas = gameCanvas;
    }

    /**
     * Builds top half of the panel:
     *   1. "Load from file" button -> opens FileChooser pointing at .txt files + calls CourseInputModule.generateFromFile(), then GUI_phase2.refreshCourse()
     *   2. Text fields pre-filled from currently loaded course -> user can change individual values without reloading a file
     *   3. "guily" button -> reads fields and calls refreshCourse()
     */
    public VBox buildCourseSection() {
        VBox section = new VBox(6);
 
        Label title = sectionLabel("Course Info & Loading");
 
        //loading from file
        Button loadFileBtn = new Button("Load course file");
        loadFileBtn.setMaxWidth(Double.MAX_VALUE);
        loadFileBtn.setOnAction(e -> onLoadFile());
 
        courseStatusLabel = smallLabel("No file loaded");
 
        //manual fields
        //pre-fill from current course so user sees sensible defaults immediately
        GolfCourse c = gui.getCourseRelated();
        double[] frictions = c.getFrictions();
        double[] target = c.getTargetXYR();
        double[] start = c.getStartPosition();
 
        heightFormulaField = field("(sin(x - y) / 7.0) + 0.5");
        miuKField = field(String.valueOf(frictions[0]));
        miuSField = field(String.valueOf(frictions[1]));
        targetXField = field(String.valueOf(target[0]));
        targetYField = field(String.valueOf(target[1]));
        targetRField = field(String.valueOf(target[2]));
        startXField = field(String.valueOf(start[0]));
        startYField = field(String.valueOf(start[1]));
 
        Button guilyBtn = new Button("guily");
        guilyBtn.setMaxWidth(Double.MAX_VALUE);
        guilyBtn.setOnAction(e -> onguilyManual());
 
        section.getChildren().addAll(
            title,
            loadFileBtn,
            courseStatusLabel,
            new Separator(),
            smallLabel("Height formula h(x,y):"), heightFormulaField,
            smallLabel("µK (kinetic friction):"), miuKField,
            smallLabel("µS (static friction):"),  miuSField,
            smallLabel("Target x:"), targetXField,
            smallLabel("Target y:"), targetYField,
            smallLabel("Target radius:"), targetRField,
            smallLabel("Start x:"), startXField,
            smallLabel("Start y:"), startYField,
            guilyBtn
        );

        return section;
    }

    /**
     * Opens FileChooser + calls CourseInputModule.generateFromFile() on chosen .txt, then refreshes GUI
     *
     * Watch out: 
     *      generateFromFile() rewrites GeneratedCourse.java on disk BUT does notrecompile it  
     *      -> work around this by also calling onguilyManual():
     *          builds a CourseProfile directly from the parsed values —> terrain displayed always correct even without a recompile
     */
    private void onLoadFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Course File");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Course files", "*.txt")
        );
 
        File file = chooser.showOpenDialog(null);
        if (file == null) return; // user cancelled
 
        try {
            new CourseInputModule().generateFromFile(file.getAbsolutePath());
            courseStatusLabel.setText("Loaded: " + file.getName());
            courseStatusLabel.setTextFill(Color.LIGHTGREEN);
 
            //refresh using newly written GeneratedCourse
            gui.refreshCourse(new GolfCourse(), 
                              new CourseProfile(Double.parseDouble(miuKField.getText().trim()), Double.parseDouble(miuSField.getText().trim())
                            ));
        } 
        catch (Exception ex) {
            courseStatusLabel.setText("Error: " + ex.getMessage());
            courseStatusLabel.setTextFill(Color.SALMON);
        }
    }

    //reads manual input fields and calls refreshCourse() with a new CourseProfile built from those values
    private void onguilyManual() {
        try {
            double miuK = Double.parseDouble(miuKField.getText().trim());
            double miuS = Double.parseDouble(miuSField.getText().trim());
 
            if (miuK <= 0 || miuS <= 0 || miuS <= miuK) {
                courseStatusLabel.setText("Need µS > µK > 0");
                courseStatusLabel.setTextFill(Color.SALMON);
                return;
            }
 
            gui.refreshCourse(new GolfCourse(), new CourseProfile(miuK, miuS));
 
            courseStatusLabel.setText("guilied.");
            courseStatusLabel.setTextFill(Color.LIGHTGREEN);

        } 
        catch (NumberFormatException ex) {
            courseStatusLabel.setText("Invalid number in fields.");
            courseStatusLabel.setTextFill(Color.SALMON);
        }
    }

    /**
     * Builds top half of the panel:
     *   1. Labels to show current shot count, ball position, distance to target
     *   2. Shot result (e.g. "In water!", "On green, 2.3m from target", "In hole!") 
     *   3. Manual velocity input + "Fire" button as an alternative to mouse-drag
     *   4. Bot buttons: "SimpleBot shot" and "MLBot shot" to play against 
     */
    public VBox buildShotSection() {
        VBox section = new VBox(6);
 
        Label title = sectionLabel("Shot Info & Game State");
 
        // initial values, updated after every shot in onShotLanded() and after reset in onReset()
        shotCountLabel = hudLabel("Shots: 0");
        ballPosLabel = hudLabel("Ball: (0.00, 0.00)");
        distanceLabel = hudLabel("Distance to target: —");
        shotResultLabel = new Label("");
        shotResultLabel.setFont(Font.font(13));
        shotResultLabel.setWrapText(true);
 
        //manual velocity input
        manualVxField = field("0.0");
        manualVyField = field("0.0");
 
        Button fireBtn = new Button("Fire");
        fireBtn.setMaxWidth(Double.MAX_VALUE);
        fireBtn.setOnAction(e -> onFireManual());
 
        //bot buttons
        Button simpleBotBtn = new Button("SimpleBot shot");
        simpleBotBtn.setMaxWidth(Double.MAX_VALUE);
        //simpleBotBtn.setOnAction(e -> onSimpleBotShot());
        simpleBotBtn.setOnAction(e -> shotResultLabel.setText("SimpleBot not yet connected."));
 
        Button mlBotBtn = new Button("MLBot shot");
        mlBotBtn.setMaxWidth(Double.MAX_VALUE);
        //mlBotBtn.setOnAction(e -> onMLBotShot());
        mlBotBtn.setOnAction(e -> shotResultLabel.setText("MLBot not yet connected."));
 
        Button resetBtn = new Button("Reset ball");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(e -> onReset());
 
        section.getChildren().addAll(
            title,
            shotCountLabel,
            ballPosLabel,
            distanceLabel,
            shotResultLabel,
            new Separator(),
            smallLabel("Manual vx (m/s):"), manualVxField,
            smallLabel("Manual vy (m/s):"), manualVyField,
            fireBtn,
            new Separator(),
            simpleBotBtn,
            mlBotBtn,
            new Separator(),
            resetBtn
        );

        return section;
    }

    //reads manual velocity fields and triggers a shot with those values
    private void onFireManual() {
        try {
            double vx = Double.parseDouble(manualVxField.getText().trim());
            double vy = Double.parseDouble(manualVyField.getText().trim());
            recordShot();
            gameCanvas.fireShot(vx, vy);
        } 
        catch (NumberFormatException ex) {
            shotResultLabel.setText("Enter valid numbers for vx and vy.");
            shotResultLabel.setTextFill(Color.SALMON);
        }
    }

    //Asks bots for a shot, then triggers it
    // private void onSimpleBotShot() {
    //     bots.SimpleBot bot = new bots.SimpleBot(gui.getCourseRelated());
    //     double[] vel = bot.chooseNextShot();
    //     recordShot();
    //     gameCanvas.fireShot(vel[0], vel[1]);
    // }
    // private void onMLBotShot() {
    //     bots.MLBot bot = new bots.MLBot(gui.getCourseRelated(), gui.getGolfODE());
    //     double[] vel = bot.chooseNextShot(gui.getBall().getState());
    //     recordShot();
    //     gameCanvas.fireShot(vel[0], vel[1]);
    // }

    //Resets ball to starting position and clears shot count and result label
    private void onReset() {
        double[] startPos = gui.getCourseRelated().getStartPosition();
        double[] resetState = new double[]{ startPos[0], startPos[1], 0.0, 0.0 };

        gui.getBall().setPos(resetState);
        gameCanvas.drawObjects(resetState, null, 0.0, 0.0);

        shotResultLabel.setText("");
        
        update(resetState, shotCount, gui.distanceToTarget(resetState));
    }

    //called by GameCanvas after every shot completes to update the shot count, ball position, distance to target, and shot result message
    public void onShotLanded(double[] finalState, double dist, boolean inWater, boolean won) {
        update(finalState, shotCount, dist);
 
        if (won) {
            shotResultLabel.setText("Hole in one! (" + shotCount + " shots)");
            shotResultLabel.setTextFill(Color.LIGHTGREEN);
        } 
        else if (inWater) {
            shotResultLabel.setText("In the water! +1 penalty. Replaying from start.");
            shotResultLabel.setTextFill(Color.CORNFLOWERBLUE);
            
            //penalty: count an extra shot and reset position
            recordShot(); 
            onReset();
        } 
        else {
            shotResultLabel.setText(String.format("Resting %.2f m from target.", dist));
            shotResultLabel.setTextFill(Color.BLACK);
        }
    }

    //updates all labels 
    public void update(double[] ballState, int shots, double dist) {
        shotCountLabel.setText("Shots: " + shots);

        ballPosLabel.setText(String.format("Ball: (%.2f, %.2f)", ballState[0], ballState[1]));

        distanceLabel.setText(String.format("Distance: %.2f m", dist));
    }

    //increment shot count and update the label
    public void recordShot() {
        shotCount++;
        shotCountLabel.setText("Shots: " + shotCount);
    }

    public void resetShotCount() {
        shotCount = 0;
        shotCountLabel.setText("Shots: 0");
        shotResultLabel.setText("");
    }
    
    //create section titles
    private Label sectionLabel(String text) {
        Label label = new Label(text);

        label.setFont(Font.font(17));
        label.setTextFill(Color.BLACK);

        return label;
    }

    //create small labels for fields
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
