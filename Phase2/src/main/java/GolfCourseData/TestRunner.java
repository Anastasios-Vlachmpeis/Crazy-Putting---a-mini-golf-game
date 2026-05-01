/* 
package GolfCourseData;

public class TestRunner {
    public static void main(String[] args) throws Exception {
        int method = 0;
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("Method started");

        if(method == 0){
            new CourseInputModule().generateFromFile("Phase2/src/main/java/GolfCourseData/Course1.txt");
            System.out.println("Test Complete!");
        }
        if(method == 1){
            //{friction}{target}{start} 
            String[][] inputValuesGUI = {{"0.75", "0.1", "1.0"}, {"15.5", "5.11", "2.2"}, {"23.0", "56.0", "0.0"}};
            new CourseInputModule().generateFromGUI("(sin(3*x-y)/ 3.0) + 2.5", inputValuesGUI);
            System.out.println("Test Complete!");
        }
        System.out.println("Check");
    }
}

//////////////
//file is only used to generate the "GeneratedCourse.java" file. Later, CourseInputModule needs to be called from the GUI
//javac -d bin src/main/java/GolfCourseData/*.java
//java -cp bin GolfCourseData.TestRunner
*/