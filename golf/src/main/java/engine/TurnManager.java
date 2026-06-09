package engine;

/*
 * Handles turns and bot selection
 * Knows if the game is multiplayer and whose turn it is
 */

import bots.GolfBot;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

final class TurnManager {
    private boolean isMultiplayer = false;
    private GolfBot activeBot = null;
    private final BooleanProperty isPlayerTurn = new SimpleBooleanProperty(true);

    void setMultiplayerMode(boolean isMultiplayer, GolfBot bot) {
        this.isMultiplayer = isMultiplayer;
        this.activeBot = bot;
        resetTurn();
    }

    double[] getBotShot() {
        if (activeBot != null) {
            System.out.println("Active bot: " + activeBot.getClass().getSimpleName());
            return activeBot.shoot();
        }
        return new double[]{0, 0};
    }

    void switchTurn() {
        isPlayerTurn.set(!isPlayerTurn.get());
    }

    void resetTurn() {
        isPlayerTurn.set(true);
    }

    boolean isMultiplayerMode() {
        return isMultiplayer;
    }

    BooleanProperty isPlayerTurnProperty() {
        return isPlayerTurn;
    }

    boolean isPlayerTurn() {
        return isPlayerTurn.get();
    }
}
