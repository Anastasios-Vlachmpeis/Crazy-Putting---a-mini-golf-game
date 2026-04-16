package GUI;

import GolfCourseData.*;
//import Physics.CourseProfile;
import Main.GUI_phase2;
 
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import java.io.File;

public class CoursePanel {
    private final GUI_phase2 gui;
    
    private TextField heightFormulaField;
    private TextField miuKField; //kinetic friction
    private TextField miuSField; //static friction
    private TextField targetXField;
    private TextField targetYField;
    private TextField targetRField; //radius of target area
    private TextField ballXField;
    private TextField ballYField;
    private Label statusLabel; //displays info about the current course (e.g. "Course loaded successfully!" or "Error loading course: ...")

    public CoursePanel(GUI_phase2 gui) {
        this.gui = gui;
    }

    /**
     * Builds left side of GUI:
     *   1. "Load from file" button -> opens FileChooser pointing at .txt files + calls CourseInputModule.generateFromFile(), then GUI_phase2.refreshCourse()
     *   2. Text fields pre-filled from currently loaded course -> user can change individual values without reloading a file
     *   3. "build" button -> reads fields and calls refreshCourse()
     */
    public VBox buildCourseSection() {
        VBox section = new VBox(6);
        section.setPadding(new Insets(12));
 
        Label title = sectionLabel("Course Info & Loading");
 
        //loading from file
        Button loadFileBtn = new Button("Load course file");
        loadFileBtn.setMaxWidth(Double.MAX_VALUE);
        loadFileBtn.setOnAction(e -> onLoadFile());
 
        statusLabel = smallLabel("No file loaded");
 
        //manual fields
        //pre-fill from current course so user sees sensible defaults immediately
        GolfCourse c = gui.getCourseRelated();
        double[] frictions = c.getFrictions();
        double[] target = c.getTargetXYR();
        double[] ball = c.getStartPosition();
 
        heightFormulaField = field("(sin(x - y) / 7.0) + 0.5");
        miuKField = field(String.valueOf(frictions[0]));
        miuSField = field(String.valueOf(frictions[1]));
        targetXField = field(String.valueOf(target[0]));
        targetYField = field(String.valueOf(target[1]));
        targetRField = field(String.valueOf(target[2]));
        ballXField = field(String.valueOf(ball[0]));
        ballYField = field(String.valueOf(ball[1]));
 
        Button buildBtn = new Button("Build");
        buildBtn.setMaxWidth(Double.MAX_VALUE);
        buildBtn.setOnAction(e -> onBuildManual());

        Separator separator = new Separator();
        separator.setPadding(new Insets(6, 0, 6, 0));
 
        section.getChildren().clear();
        section.getChildren().addAll(
            title,
            loadFileBtn,
            statusLabel,
            separator,
            smallLabel("Height formula h(x,y):"), heightFormulaField,
            smallLabel("µK (kinetic friction):"), miuKField,
            smallLabel("µS (static friction):"),  miuSField,
            smallLabel("Target x:"), targetXField,
            smallLabel("Target y:"), targetYField,
            smallLabel("Target radius:"), targetRField,
            smallLabel("Ball x:"), ballXField,
            smallLabel("Ball y:"), ballYField,
            buildBtn
        );

        return section;
    }

    /**
     * Opens FileChooser + calls CourseInputModule.generateFromFile() on chosen .txt, then refreshes GUI
     *
     * Watch out: 
     *      generateFromFile() rewrites GeneratedCourse.java on disk BUT does notrecompile it  
     *      -> work around this by also calling onBuildManual():
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
            statusLabel.setText("Loaded: " + file.getName());
            statusLabel.setTextFill(Color.LIGHTGREEN);
        /* 
            //refresh using newly written GeneratedCourse
            gui.refreshCourse(new GolfCourse(), 
                new GolfCourse(Double.parseDouble(miuKField.getText().trim()), Double.parseDouble(miuSField.getText().trim())
                ));
        */
        } 
        catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            statusLabel.setTextFill(Color.SALMON);
        }
    }

    //reads manual input fields and calls refreshCourse() with a new CourseProfile built from those values
    private void onBuildManual() {
        try {
            double miuK = Double.parseDouble(miuKField.getText().trim());
            double miuS = Double.parseDouble(miuSField.getText().trim());
 
            if (miuK <= 0 || miuS <= 0 || miuS <= miuK) {
                statusLabel.setText("Need µS > µK > 0");
                statusLabel.setTextFill(Color.SALMON);
                return;
            }
 
            gui.refreshCourse(/*new GolfCourse(), */new GolfCourse(miuK, miuS));//"CourseProfile" was the test engine ~Stan
 
            statusLabel.setText("Built manually");
            statusLabel.setTextFill(Color.LIGHTGREEN);

        } 
        catch (NumberFormatException ex) {
            statusLabel.setText("Invalid number in fields");
            statusLabel.setTextFill(Color.SALMON);
        }
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

    private TextField field(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setMaxWidth(Double.MAX_VALUE);

        return tf;
    }
}
