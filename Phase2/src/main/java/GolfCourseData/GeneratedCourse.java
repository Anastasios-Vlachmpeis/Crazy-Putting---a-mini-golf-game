package GolfCourseData;

public class GeneratedCourse {
    //This file is constantly rebuild -> changes will not save here
    // first index is the line level of the data starting from line 2
    //{friction}{target}{start} 
    public final double[][] courseData = {{0.15, 0.5, 0.0}, {10.5, 5.0, 0.2}, {0.0, 0.0, 0.0}};

    public double h(double x, double y) {
        return (Math.sin(x+y)/ 7.0) + 0.5;
    }
    public double[][] courseData() {
        return courseData ;
    }
}/////////////////