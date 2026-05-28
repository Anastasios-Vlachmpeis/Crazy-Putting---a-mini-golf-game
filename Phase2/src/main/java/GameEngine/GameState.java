package GameEngine;

public enum GameState {
    AIMING,      // Waiting for the player or bot to choose velocity
    ROLLING,     // Physics engine is currently computing/animating frames
    HOLED_OUT,   // Ball successfully made it into the cup target
    GAME_OVER    // Out of shots or out of bounds limits reached
}
