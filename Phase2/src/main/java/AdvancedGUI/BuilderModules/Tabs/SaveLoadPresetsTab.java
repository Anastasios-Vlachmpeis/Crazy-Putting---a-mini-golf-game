package AdvancedGUI.BuilderModules.Tabs;

import AdvancedGUI.BuilderModules.CoursePreview;
import GolfCourseData.GolfCourse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import javafx.scene.control.Separator;

public class SaveLoadPresetsTab extends BorderPane{

    private CoursePreview coursePreview;

    public SaveLoadPresetsTab(GolfCourse course, double[] preViewSize) {

        this.coursePreview = new CoursePreview(course, preViewSize[0], preViewSize[1]);

        VBox rightMenu = new VBox(10);
        rightMenu.setPadding(new Insets(20));
        rightMenu.setAlignment(Pos.TOP_LEFT);
        rightMenu.setPrefWidth(250);
        rightMenu.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Save / Load Presets");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        //Saving
        Button saveButton = new Button("Save");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        TextField presetNameField = new TextField();
        presetNameField.setPromptText("Enter preset name");
        saveButton.setOnAction(e -> {
            try {
                String fileName = presetNameField.getText().trim();
                //if name left empty
                if (fileName.isEmpty()) {
                    fileName = "custom_course";
                }
                // add .json if user forgot
                if (!fileName.toLowerCase().endsWith(".json")) {
                    fileName += ".json";
                }
                // Add an actual filename to the destination path
                String fullPath = "src/main/java/Presets/Phase3Format/" + fileName;
                course.saveToJson(fullPath);
                System.out.println("Successfully saved as: " + fileName);

            } catch (Exception ex) {
                // This catches the exception inside the lambda so Java is happy
                System.out.println("Failed to save the JSON file");
                ex.printStackTrace();
            }
        });

        //Loading
        Button searchButton = new Button("Search & Load Preset");
        searchButton.setMaxWidth(Double.MAX_VALUE); // Stretch button horizontally

        searchButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Search Golf Course Presets");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Golf Course Presets (*.json)", "*.json")
            );
            File presetsDirectory = new File("src/main/java/Presets/Phase3Format");
            if (presetsDirectory.exists() && presetsDirectory.isDirectory()) {
                fileChooser.setInitialDirectory(presetsDirectory);
            }
            Stage currentStage = (Stage) this.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(currentStage);
            if (selectedFile != null) {
                try {
                    // Load the map data into your shared course object memory model
                    course.loadFromJson(selectedFile.getAbsolutePath()); //

                    coursePreview.updatePreview();
            
                    System.out.println("Successfully loaded selected file: " + selectedFile.getName());
            
                } catch (Exception ex) {
                    System.out.println("Error: Failed to safely parse the selected preset file.");
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Search selection canceled by user.");
            }
        });

        Separator sectionDivider1 = new Separator();
        // Adds 15 pixels of breathing room space above and below the line
        sectionDivider1.setPadding(new Insets(15, 0, 15, 0));

        rightMenu.getChildren().addAll(
            title,
            new Label("Save current course to file"),
            new Label("Preset File Name:"), 
            presetNameField,
            saveButton,
            sectionDivider1,
            new Label("Load Preset:"), 
            searchButton
        );

        //combine BorderPane
        this.setCenter(coursePreview);
        this.setRight(rightMenu);
    }

    //refresh preview
    public void refreshView() {
        if (coursePreview != null) {
            coursePreview.updatePreview();
        }
    }
    
}
