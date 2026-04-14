package GolfCourseData;

public class GeneratedCourse {
    //This file is constantly rebuild -> changes will not save here
    //{friction}{target}{start} 
    public final double[][] courseData = {{0.75, 0.1, 1.0}, {15.5, 5.11, 2.2}, {0.0, 0.0, 0.0}};

    public double h(double x, double y) {
        return (Math.sin(3*x-3*y)/ 3.0) + 0.5;
    }
    public double[][] courseData() {
        return courseData;
    }
}/////////////////