package spirepass.challengeutil;

public class ChallengeVariables {
    // daily_setup - Play two Power cards on turn 1
    public static int dailySetupPowersPlayed = 0;

    /**
     * Reset variables at the start of each turn
     * Call this at the start of each turn
     */
    public static void resetVariablesEveryTurn() {
        dailySetupPowersPlayed = 0;
    }
}