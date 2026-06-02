package AdvancedGUI.LauncherModules.SettingsModules;

import GolfCourseData.GolfCourse;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import AdvancedGUI.BuilderModules.GUI_courseBuilder;
import GameEngine.GameManager;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import GolfCourseData.GolfCourse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Settings {

    int padding = 5;

    public void display(String title, GolfCourse course){
        StackPane root = new StackPane();
        VBox rootBox = new VBox();


        //stepSize setting
        Label stepSizelabel = new Label("Enter Step Size: ");
        styleSettingsLabel(stepSizelabel); //set style using method below

        TextField stepSizeText = new TextField("0.01"); //Default value in GolfCourse.java
        styleTextBox(stepSizeText);
        ChangeListener<String> stepSizeUpdateListener = (observable, oldValue, newValue) -> {
            try{
                double stepSize = Double.parseDouble(stepSizeText.getText());
                course.setStepSize(stepSize);
                System.out.println("StepSize updated");
            } catch (NumberFormatException ex){
                System.out.println("Entered step size is not a number");
            }
        };
        stepSizeText.textProperty().addListener(stepSizeUpdateListener);


        //friction settings
        Label MiuK = new Label("Enter kinetic friction greens: ");
        styleSettingsLabel(MiuK);
        Label MiuS = new Label("Enter static friction greens: ");
        styleSettingsLabel(MiuS);
        Label MiuKSand = new Label("Enter kinetic friction sand: ");
        styleSettingsLabel(MiuKSand);
        Label MiuSSand = new Label("Enter static friction sand: ");
        styleSettingsLabel(MiuSSand);

        TextField MiuKString = new TextField("0.15");
        styleTextBox(MiuKString);
        TextField MiuSString = new TextField("0.5");
        styleTextBox(MiuSString);
        TextField MiuKSandString = new TextField("0.3");
        styleTextBox(MiuKSandString);
        TextField MiuSSandString = new TextField("0.75");
        styleTextBox(MiuSSandString);

        ChangeListener<String> frictionUpdateListener = (observable, oldValue, newValue) -> {
            try{
                double KValue = Double.parseDouble(MiuKString.getText());
                double SValue = Double.parseDouble(MiuSString.getText());
                double KSandValue = Double.parseDouble(MiuKSandString.getText());
                double SSandValue = Double.parseDouble(MiuSSandString.getText());
                course.setFrictions(KValue, SValue, KSandValue, SSandValue);
                System.out.println("Frictions updated");
            } catch (NumberFormatException ex){
                System.out.println("Entered friction is not valid");
            }
        };
        MiuKString.textProperty().addListener(frictionUpdateListener);
        MiuSString.textProperty().addListener(frictionUpdateListener);
        MiuKSandString.textProperty().addListener(frictionUpdateListener);
        MiuSSandString.textProperty().addListener(frictionUpdateListener);


        //Bot Selection for multiplayer
        Label botLabel = new Label("Select your prefered bot: ");
        styleSettingsLabel(botLabel);
        ObservableList<String> bot = FXCollections.observableArrayList(
            "Simple Bot", 
            "ML Bot", 
            "Hill Bot",
            "ManHattan Bot",
            "Newton Bot",
            "Search Bot"
        );
        ComboBox<String> botSelection = new ComboBox<>(bot);
        botSelection.setValue("Simple Bot");
        //botSelection.setPadding(new Insets(20, 0, 0, 50));

        //Add all title Labels
        Label generalSettingsLabel = new Label("General Settings");
        styleTitleLabel(generalSettingsLabel);

        //Label singlePlayerSettingsLabel = new Label("Single player Settings");
        //styleTitleLabel(singlePlayerSettingsLabel);

        Label multiPlayerSettingsLabel = new Label("Multi Player Settings");
        styleTitleLabel(multiPlayerSettingsLabel);

        //Building the menu
        //We make a VBox for the Labels and another VBox for the Fields/slider so they are side by side and globally alligned
        VBox generalLeft = new VBox(padding);
        generalLeft.setPadding(new Insets(0, 0, 0, 20));
        generalLeft.getChildren().addAll(stepSizelabel, MiuK, MiuS, MiuKSand, MiuSSand);

        VBox generalRight = new VBox(padding*2.2);
        generalRight.setPadding(new Insets(5, 0, 25, 50));
        generalRight.getChildren().addAll(stepSizeText, MiuKString, MiuSString, MiuKSandString, MiuSSandString);


        //VBox singlePlayerLeft = new VBox(padding);
        //VBox singlePlayerRight = new VBox(padding*2.2);


        VBox multiPlayerLeft = new VBox(padding);
        multiPlayerLeft.setPadding(new Insets(0, 0, 0, 20));
        multiPlayerLeft.getChildren().addAll(botLabel);
        VBox multiPlayerRight = new VBox(padding*2.2);
        multiPlayerRight.setPadding(new Insets(10, 0, 0, 100));
        multiPlayerRight.getChildren().addAll(botSelection);

        //put both Left and Right next to each other
        HBox generalBox = new HBox(padding);
        generalBox.getChildren().addAll(generalLeft, generalRight);

        //HBox singlePlayerBox = new HBox(padding);
        //singlePlayerBox.getChildren().addAll(singlePlayerLeft, singlePlayerRight);

        HBox multiPlayerBox = new HBox(padding);
        multiPlayerBox.getChildren().addAll(multiPlayerLeft, multiPlayerRight);
        

        //Add all settings components
        rootBox.setAlignment(Pos.CENTER);
        rootBox.setMaxWidth(600);
        rootBox.setMaxHeight(300);
        rootBox.setPadding(new Insets(0, 10, 30, 10));
        rootBox.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.75); " +
            "-fx-background-radius: 50;"
        );
        rootBox.getChildren().addAll(
            generalSettingsLabel,
            generalBox,
            //singlePlayerSettingsLabel,
            //singlePlayerBox,
            multiPlayerSettingsLabel,
            multiPlayerBox
        );

        //Add background to scene
        Image backgroundImage = new Image("file:src\\main\\java\\AdvancedGUI\\LauncherModules\\Background.png");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(root.widthProperty());
        backgroundView.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().addAll(backgroundView, rootBox);

        Scene scene = new Scene(root, 1000, 800);
        StackPane.setAlignment(rootBox, Pos.TOP_CENTER);
        StackPane.setMargin(rootBox, new Insets(100, 0, 250, 0));

        Stage settingsGUI = new Stage();
        settingsGUI.setTitle(title);
        settingsGUI.setMinWidth(600);
        settingsGUI.setMinHeight(400);
        settingsGUI.setMaximized(true);
        settingsGUI.setScene(scene);
        settingsGUI.show();
    }

    private void styleTitleLabel(Label label) {
        label.setPadding(new Insets(25, 10, 25, 10));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        label.setStyle("-fx-text-fill: #FFFFFF;");
        label.setEffect(new DropShadow(10, Color.BLACK));
    }

    private void styleSettingsLabel(Label label) {
        label.setPadding(new Insets(10, 10, 10, 30));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        label.setStyle("-fx-text-fill: #FFFFFF;");
        label.setEffect(new DropShadow(10, Color.BLACK));
    }

    private void styleTextBox(TextField textfield){
        textfield.setPadding(new Insets(10, 0, 10, 7));
    }
}
