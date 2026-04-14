package Main;

import GUI.*;
import GolfCourseData.*;
import Physics.*;
import Systems.GolfODE;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D; //To


public class GUI_phase2 extends Application{

    private GolfCourse golfCourse = new GolfCourse(0.07, 0.15);
    private CourseProfile courseProfile = new CourseProfile(0.07, 0.15);
    private GolfODE golfODE = new GolfODE(courseProfile);
    private Ball ball;

    //Build in separate classes 
    private GameCanvas gameCanvas;
    private CoursePanel coursePanel;
    private ShotPanel shotPanel;

    @Override
    public void start(Stage primaryStage) {
        //Create ball at start position
        double[] startPos = golfCourse.getStartPosition(); //[x, y, height]
        ball = new Ball(new double[]{ startPos[0], startPos[1], 0.0, 0.0 });

        //Initialize game canvas and side panel
        gameCanvas   = new GameCanvas(golfCourse, golfODE, ball);
        coursePanel  = new CoursePanel(this);
        shotPanel    = new ShotPanel(this, gameCanvas);
  
        //wire back so we can update after every shot
        gameCanvas.setShotPanel(shotPanel);

        //draw background terrain 
        gameCanvas.drawTerrain();

        //initial scene -> target and ball at starting position
        gameCanvas.drawObjects(ball.getState(), null, 0.0, 0.0); //ball state, target position, shot power, shot angle

        BorderPane root = new BorderPane();

        //
        StackPane canvasView = gameCanvas.getView(); //holds both canvases
        VBox.setVgrow(canvasView, Priority.ALWAYS); //want to have have the size grow
        HBox.setHgrow(canvasView, Priority.ALWAYS);
        canvasView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox left  = coursePanel.buildCourseSection();
        root.setLeft(left);
        VBox right = shotPanel.buildShotSection();
        root.setRight(right);

        BorderPane.setMargin(left, new Insets(20));
        BorderPane.setMargin(right, new Insets(20));
        
        root.setCenter(canvasView);
        BorderPane.setMargin(canvasView, new Insets(10));

        //make window fill screen 
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, screen.getWidth(), screen.getHeight(), Color.WHITESMOKE);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Crazy Putting – Phase 2 (team17)");
        primaryStage.setX(screen.getMinX());
        primaryStage.setY(screen.getMinY());

        primaryStage.show();
    }

    /*
      Used by CoursePanel when the user loads a new course
      Re-wires all shared objects so every panel sees the new course without having to be rebuilt
     */
    public void refreshCourse (GolfCourse newCourse, CourseProfile newProfile) {
        golfCourse = newCourse;
        courseProfile = newProfile;
        golfODE = new GolfODE(courseProfile);
 
        //reset ball to the new starting position
        double[] startPos = golfCourse.getStartPosition();
        ball = new Ball(new double[]{ startPos[0], startPos[1], 0.0, 0.0 });
 
        //redraw terrain and objects with new course data
        gameCanvas.setCourse(golfCourse, golfODE, ball);
        gameCanvas.drawTerrain();
        gameCanvas.drawObjects(ball.getState(), null, 0.0, 0.0);
 
        //reset shotPanel values
        shotPanel.resetShotCount();
        shotPanel.update(ball.getState(), 0, golfCourse.distanceToTarget(ball.getState()[0], ball.getState()[1]));
    }

    //Some getters for shot panel and game canvas to access shared objects
    public GolfCourse getCourseRelated() { 
        return golfCourse; 
    }
    public CourseProfile getCourseProfile() { 
        return courseProfile; 
    }
    public GolfODE  getGolfODE() {
        return golfODE; 
    }
    public Ball getBall() { 
        return ball; 
    }

    public double distanceToTarget(double[] ballState) { //also need it here since in shotPanel we call it from GUI
        return golfCourse.distanceToTarget(ballState);
    }   
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
