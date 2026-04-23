package GolfCourseData;

public class GolfCourse {
//Here we extend the methods for actually using the generated course
    public final double epsilon = 1e-7;

    private final double miuK; // kinetic friction coeficient
    private final double miuS; // static friction coeficient, theoretically its more like a constant. I just put it here to have both mius in one place
    
    GeneratedCourse course = new GeneratedCourse();

    public GolfCourse(double miuK, double miuS) {
        this.miuK = miuK; 
        this.miuS = miuS; 
    }

    public double getMiuK() {
        return miuK;
    }

    public double getMiuS() {
        return miuS;
    }

    public double height(double x, double y){
        return course.h(x,y);
    }
    
    public double[] getDerivative(double x, double y){
        double slopeX = (course.h(x + epsilon, y) - course.h(x, y))/epsilon;
        double slopeY = (course.h(x, y + epsilon) - course.h(x, y))/epsilon;
        return new double[] {slopeX, slopeY};
    }

    public double dhdx(double x, double y){
        double slopeX = (course.h(x + epsilon, y) - course.h(x, y))/epsilon;
        return slopeX;
    }

    public double dhdy(double x, double y){
        double slopeY = (course.h(x, y + epsilon) - course.h(x, y))/epsilon;
        return slopeY;
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

    //Set the current ball position for the next stroke
    // Position needs to be updated before the bot shoots again
    public void setBallPosition(double x, double y) {
        course.courseData[2][0] = x;
        course.courseData[2][1] = y;
    }

    //we pass different things around in classes so we nee dthe same method twice but slightly different input
    public double distanceToTarget(double x, double y) {
        double[] target = getTargetXYR(); // [tx, ty, r]
        double dx = x - target[0];
        double dy = y - target[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceToTarget(double[] ballState) {
        return distanceToTarget(ballState[0], ballState[1]);
    }
}
