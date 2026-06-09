package engine;

/*
 * Stores the result of checking the rules
 * GameManager uses it to know what should happen after a shot
 */

final class RuleEvaluation {
    private final ShotResult shotResult;
    private final GameState nextState;
    private final String consoleMessage;
    private final RecoveryType recoveryType;

    private RuleEvaluation(
        ShotResult shotResult,
        GameState nextState,
        String consoleMessage,
        RecoveryType recoveryType
    ) {
        this.shotResult = shotResult;
        this.nextState = nextState;
        this.consoleMessage = consoleMessage;
        this.recoveryType = recoveryType;
    }

    static RuleEvaluation normal() {
        return new RuleEvaluation(ShotResult.NORMAL, null, null, null);
    }

    static RuleEvaluation penalty(ShotResult shotResult, String consoleMessage, RecoveryType recoveryType) {
        return new RuleEvaluation(shotResult, null, consoleMessage, recoveryType);
    }

    static RuleEvaluation stateChange(ShotResult shotResult, GameState nextState, String consoleMessage) {
        return new RuleEvaluation(shotResult, nextState, consoleMessage, null);
    }

    boolean isPenalty() {
        return recoveryType != null;
    }

    ShotResult shotResult() {
        return shotResult;
    }

    GameState nextState() {
        return nextState;
    }

    String consoleMessage() {
        return consoleMessage;
    }

    RecoveryType recoveryType() {
        return recoveryType;
    }
}
