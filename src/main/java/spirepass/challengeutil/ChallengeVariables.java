package spirepass.challengeutil;

public class ChallengeVariables {
    public static int dailySetupPowersPlayed = 0;
    public static int dailyComboCardsPlayedThisTurn = 0;

    /**
     * Reset variables at the start of each turn
     * Call this at the start of each turn
     */
    public static void resetVariablesEveryTurn() {
        dailySetupPowersPlayed = 0;
        dailyComboCardsPlayedThisTurn = 0;
    }
}