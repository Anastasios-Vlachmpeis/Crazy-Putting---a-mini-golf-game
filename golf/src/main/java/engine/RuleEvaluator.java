package engine;

/*
 * Checks the rules after a shot
 * Decides if the ball is in water, out of bounds, in the hole, or if the game is over
 */

import domain.course.GolfCourse;

final class RuleEvaluator {
    private RuleEvaluator() {
    }

    static RuleEvaluation evaluate(
        GolfCourse course,
        double currentBallX,
        double currentBallY,
        boolean isPlayerTurn,
        int playerStrokes,
        int botStrokes,
        int maxStrokes
    ) {
        double[] boundaries = course.getSize();
        boolean outOfBounds = currentBallX < boundaries[0]
            || currentBallX > boundaries[1]
            || currentBallY < boundaries[2]
            || currentBallY > boundaries[3];

        if (outOfBounds) {
            return RuleEvaluation.penalty(
                ShotResult.OUT_OF_BOUNDS,
                "OUT OF BOUNDS! Resetting to the edge.",
                RecoveryType.EDGE
            );
        }

        if (course.isWater(currentBallX, currentBallY)) {
            return RuleEvaluation.penalty(
                ShotResult.WATER,
                "SPLASH! Ball landed in water. Resetting near water edge.",
                RecoveryType.WATER
            );
        }

        double[] target = course.getTargetXYR();
        double distance = course.distanceToTarget(currentBallX, currentBallY);
        if (distance <= target[2]) {
            String winnerName = isPlayerTurn ? "Player" : "Bot";
            int winningStrokes = isPlayerTurn ? playerStrokes : botStrokes;
            return RuleEvaluation.stateChange(
                ShotResult.HOLED_OUT,
                GameState.HOLED_OUT,
                winnerName + " achieved victory in " + winningStrokes + " strokes!"
            );
        }

        int currentStrokes = isPlayerTurn ? playerStrokes : botStrokes;
        if (currentStrokes >= maxStrokes) {
            return RuleEvaluation.stateChange(
                ShotResult.GAME_OVER,
                GameState.GAME_OVER,
                "Defeat: Exceeded maximum stroke limits."
            );
        }

        return RuleEvaluation.normal();
    }
}
