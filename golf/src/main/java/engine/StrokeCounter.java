package engine;

/*
 * Stores the stroke counts
 * Separate scores for the player and the bot
 */

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

final class StrokeCounter {
    private final IntegerProperty playerStrokes = new SimpleIntegerProperty(0);
    private final IntegerProperty botStrokes = new SimpleIntegerProperty(0);

    void addStroke(boolean isPlayerTurn) {
        if (isPlayerTurn) {
            playerStrokes.set(playerStrokes.get() + 1);
        } else {
            botStrokes.set(botStrokes.get() + 1);
        }
    }

    void reset() {
        playerStrokes.set(0);
        botStrokes.set(0);
    }

    IntegerProperty playerStrokesProperty() {
        return playerStrokes;
    }

    int getPlayerStrokes() {
        return playerStrokes.get();
    }

    IntegerProperty botStrokesProperty() {
        return botStrokes;
    }

    int getBotStrokes() {
        return botStrokes.get();
    }
}
