package GolfCourseData;

public class TestRunner {
    public static void main(String[] args) throws Exception {
        new CourseInputModule().generateFromFile("src/main/java/GolfCourseData/Course.txt");
        System.out.println("Test Complete!");
    }
}

//////////////
//file is only used to generate the "GeneratedCourseFromFile.java" file