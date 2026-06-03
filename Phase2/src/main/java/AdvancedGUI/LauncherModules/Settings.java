package AdvancedGUI.LauncherModules;

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
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Settings {

    private StackPane settingsContainer;
    private ComboBox<String> botSelection;

    public StackPane getContainer() {
        return settingsContainer;
    }

    public void show() {
        settingsContainer.setVisible(true);
    }

    public void hide() {
        settingsContainer.setVisible(false);
    }

    public String getSelectedBot() {
        return botSelection.getValue();
    }

    public Settings(GolfCourse course){
        //StackPane root = new StackPane();
        this.settingsContainer = new StackPane();
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

        this.botSelection = createStyledBotSelection();

        //Add all title Labels
        Label generalSettingsLabel = new Label("General Settings");
        styleTitleLabel(generalSettingsLabel);

        //Label singlePlayerSettingsLabel = new Label("Singleplayer Settings");
        //styleTitleLabel(singlePlayerSettingsLabel);

        Label multiPlayerSettingsLabel = new Label("Multiplayer Settings");
        styleTitleLabel(multiPlayerSettingsLabel);

        //Building the menu
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(15); 
        grid.setHgap(20); 
        
        // General Settings
        grid.add(generalSettingsLabel, 0, 0, 2, 1);
        GridPane.setHalignment(generalSettingsLabel, javafx.geometry.HPos.CENTER); 

        grid.add(stepSizelabel, 0, 1);
        grid.add(stepSizeText, 1, 1);

        grid.add(MiuK, 0, 2);
        grid.add(MiuKString, 1, 2);

        grid.add(MiuS, 0, 3);
        grid.add(MiuSString, 1, 3);

        grid.add(MiuKSand, 0, 4);
        grid.add(MiuKSandString, 1, 4);

        grid.add(MiuSSand, 0, 5);
        grid.add(MiuSSandString, 1, 5);

        //multiplayer
        GridPane.setMargin(multiPlayerSettingsLabel, new Insets(30, 0, 0, 0)); 
        grid.add(multiPlayerSettingsLabel, 0, 6, 2, 1);
        GridPane.setHalignment(multiPlayerSettingsLabel, javafx.geometry.HPos.CENTER);

        grid.add(botLabel, 0, 7);
        grid.add(botSelection, 1, 7);

        rootBox.setAlignment(Pos.CENTER);
        rootBox.setMaxWidth(600); 
        rootBox.setMaxHeight(600);
        rootBox.setPadding(new Insets(20, 20, 30, 20));
        rootBox.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.75); " +
            "-fx-background-radius: 50;"
        );
        rootBox.getChildren().add(grid);

        //Add background to scene
        Image backgroundImage = new Image("file:src\\main\\java\\AdvancedGUI\\LauncherModules\\Background.png");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(settingsContainer.widthProperty());
        backgroundView.fitHeightProperty().bind(settingsContainer.heightProperty());

        Button closeButton = new Button("Close Settings");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        closeButton.setOnAction(e -> this.hide());
        StackPane.setAlignment(closeButton, Pos.TOP_LEFT);
        StackPane.setMargin(closeButton, new Insets(10, 0, 0, 10));

        settingsContainer.getChildren().addAll(backgroundView, rootBox, closeButton);

        //this.scene = new Scene(settingsContainer, 1000, 800);
        StackPane.setAlignment(rootBox, Pos.TOP_CENTER);
        StackPane.setMargin(rootBox, new Insets(100, 0, 250, 0));
    }

    private void styleTitleLabel(Label label) {
        label.setPadding(new Insets(25, 10, 0, 10));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        label.setStyle("-fx-text-fill: #FFFFFF;");
        label.setEffect(new DropShadow(10, Color.BLACK));
    }

    private void styleSettingsLabel(Label label) {
        label.setPadding(new Insets(10, 20, 15, 10));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        label.setStyle("-fx-text-fill: #FFFFFF;");
        label.setEffect(new DropShadow(10, Color.BLACK));
    }

    private void styleTextBox(TextField textfield) {
        //When not typing
        String idleStyle = 
            "-fx-background-color: rgba(255, 255, 255, 0.95);" + 
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-border-color: #bdc3c7;" + 
            "-fx-border-width: 2;" +
            "-fx-text-fill: #333333;" +    
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 15 10 15;";   

        // While typing
        String focusedStyle = 
            "-fx-background-color: #ffffff;" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-border-color: #f39c12;" + 
            "-fx-border-width: 2;" +
            "-fx-text-fill: #333333;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 15 10 15;";

        textfield.setStyle(idleStyle);

        textfield.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            textfield.setStyle(isNowFocused ? focusedStyle : idleStyle);
        });
        textfield.setMaxWidth(150);
    }

    private ComboBox<String> createStyledBotSelection() {
        ObservableList<String> bot = FXCollections.observableArrayList(
            "Simple Bot", "ML Bot", "Hill Bot", "ManHattan Bot", "Newton Bot", "Search Bot"
        );
        ComboBox<String> comboBox = new ComboBox<>(bot);

        String comboIdleStyle = "-fx-background-color: #f39c12; -fx-background-radius: 15; -fx-padding: 5 15 5 15; -fx-cursor: hand;";
        String comboHoverStyle = "-fx-background-color: #e67e22; -fx-background-radius: 15; -fx-padding: 5 15 5 15; -fx-cursor: hand;";
        
        comboBox.setStyle(comboIdleStyle);

        comboBox.hoverProperty().addListener((obs, wasHover, isHover) -> {
            comboBox.setStyle(isHover ? comboHoverStyle : comboIdleStyle);
        });

        comboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    setTextFill(Color.WHITE);
                }
            }
        });

        comboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    setTextFill(Color.web("#333333"));
                    setStyle("-fx-padding: 10 15; -fx-background-radius: 5; -fx-background-color: transparent;");

                    hoverProperty().addListener((obs, wasHover, isHover) -> {
                        if (isHover) {
                            setStyle("-fx-background-color: #f39c12; -fx-padding: 10 15; -fx-background-radius: 5;");
                            setTextFill(Color.WHITE);
                        } else if (isSelected()) {
                            setStyle("-fx-background-color: #e67e22; -fx-padding: 10 15; -fx-background-radius: 5;");
                            setTextFill(Color.WHITE);
                        } else {
                            setStyle("-fx-background-color: transparent; -fx-padding: 10 15; -fx-background-radius: 5;");
                            setTextFill(Color.web("#333333"));
                        }
                    });
                }
            }
        });

        comboBox.setValue("Simple Bot");
        return comboBox;
    }
}
