package bots;

public class Neighbor {
    private double vx;
    private double vy;

    // kthNeighbor is the k-th neigbor of the current position, from k = 0 to as
    // many as you want
    // impact is how much the shot is deviated from the main one
    public Neighbor(double vx, double vy, double impact, double kthNeighbor, double totalNeighbors) {
        double angle = 2 * Math.PI * kthNeighbor / totalNeighbors; // angle of shot of the neighbor
        this.vx = vx + impact * Math.cos(angle); // change the current vx 
        this.vy = vy + impact * Math.sin(angle);
    }

    public double[] getVelocity() {
        return new double[] { vx, vy };
    }

}
