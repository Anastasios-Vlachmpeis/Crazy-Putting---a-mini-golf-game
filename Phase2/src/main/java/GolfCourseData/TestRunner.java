package GolfCourseData;

public class TestRunner {
    public static void main(String[] args) throws Exception {
<<<<<<< HEAD:Phase2/src/main/java/GolfCourseData/TestRunner.java
        new CourseInputModule().generateFromFile("GolfCourseData/Course.txt");
        System.out.println("Test Complete!");
=======
        int method = 1;

        if(method == 1){
            new CourseInputModule().generateFromFile("src/main/java/GolfCourseData/Course.txt");
            System.out.println("Test Complete!");
        }
        if(method == 1){
            //{friction}{target}{start} 
            String[][] inputValuesGUI = {{"0.75", "0.1", "1.0"}, {"15.5", "5.11", "2.2"}, {"23.0", "56.0", "0.0"}};
            new CourseInputModule().generateFromGUI("(sin(3*x-y)/ 3.0) + 2.5", inputValuesGUI);
            System.out.println("Test Complete!");
        }

>>>>>>> 952e02469608e3fc977683f94ad3feb762837101:src/main/java/GolfCourseData/TestRunner.java
    }
}

//////////////
//file is only used to generate the "GeneratedCourse.java" file. Later, CourseInputModule needs to be called from the GUI