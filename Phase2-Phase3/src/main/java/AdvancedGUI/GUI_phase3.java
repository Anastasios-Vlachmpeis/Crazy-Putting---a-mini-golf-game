package AdvancedGUI;

import GolfCourseData.GolfCourse;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.stage.Modality;


public class GUI_phase3 extends Application {
    //Main GUI phase 3
    @Override
    public void start(Stage GameStage) {
        // Instantiate and run builder window
        GUI_courseBuilder builderWindow = new GUI_courseBuilder();
        GolfCourse course = new GolfCourse();

        Button builderButton = new Button("Open Builder");
        builderButton.setOnAction(e -> builderWindow.display("BuilderWindow", course));
        //The main GUI is locked when builder window is open

        // Create a layout and add the components
        VBox layout = new VBox(15);
        layout.getChildren().addAll(builderButton);
        layout.setAlignment(Pos.CENTER);

        Stage mainGameGUI = new Stage();
        Scene scene = new Scene(layout);
        mainGameGUI.setTitle("Crazy Putting!");
        mainGameGUI.setMinWidth(300);
        mainGameGUI.setMinHeight(200);
        mainGameGUI.setScene(scene);
        mainGameGUI.show();
    }

    // Add a standard main method to launch the application
    //change in pom.xml <mainClass>SimpleGUI.GUI_phase2</mainClass> to <mainClass>AdvancedGUI.GUI_phase3</mainClass>
    public static void main(String[] args) {
        launch(args);
    }
    
}
