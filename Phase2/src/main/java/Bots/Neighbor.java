package Bots;

public class Neighbor {
    private double vx;
    private double vy;

    // kthNeighbor is the k-th neigbor of the current position, from k = 0 to as
    // many as you want
    public Neighbor(double vx, double vy, double kthNeighbor, double totalNeighbors) {
        double angle = 2 * Math.PI * kthNeighbor / totalNeighbors; // angle of shot of the neighbor
        this.vx = vx + 0.01 * Math.cos(angle); // change the current vx by a little bit
        this.vy = vy + 0.01 * Math.sin(angle);
    }

    public double[] getVelocity() {
        return new double[] { vx, vy };
    }

}
