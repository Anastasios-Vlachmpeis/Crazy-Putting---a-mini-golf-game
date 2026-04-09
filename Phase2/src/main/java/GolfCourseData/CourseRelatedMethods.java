package GolfCourseData;

public class CourseRelatedMethods {
//Here we extend the methods for actually using the generated course
    public final double epsilon = 1e-7;
    GeneratedCourse course = new GeneratedCourse();

    public double height(double x, double y){
        return course.h(x,y);
    }
    
    public double[] getDerivative(double x, double y){
        double slopeX = (course.h(x + epsilon, y) - course.h(x, y))/epsilon;
        double slopeY = (course.h(x, y + epsilon) - course.h(x, y))/epsilon;
        return new double[] {slopeX, slopeY};
    }

    public boolean isWater(double x, double y) {
        return course.h(x, y) < 0;
    }

    public double[] getFrictions(){
        double friction1 = course.courseData[0][0];
        double friction2 = course.courseData[0][1];
        double friction3 = course.courseData[0][2];
        return new double[] {friction1, friction2, friction3};
    }

    public double[] getTargetXYR(){
        double x = course.courseData[1][0];
        double y = course.courseData[1][1];
        double radius = course.courseData[1][2];
        return new double[] {x, y, radius};
    }

    public double[] getStartPosition(){
        double x = course.courseData[2][0];
        double y = course.courseData[2][1];
        double height = course.h(x,y);
        return new double[] {x, y, height};
    }
}
