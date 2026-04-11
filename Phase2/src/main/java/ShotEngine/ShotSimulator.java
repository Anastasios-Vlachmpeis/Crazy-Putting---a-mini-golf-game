package Phase2.src.main.java.ShotEngine;

// Contract for integrating the ball until rest. Implementation will live with the Phase 2 physics loop.
@FunctionalInterface
public interface ShotSimulator {

    ShotSimulation simulate(double v0x, double v0y);
}
