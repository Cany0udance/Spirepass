package spirepass.challengeutil;

public class ChallengeVariables {
    public static int dailySetupPowersPlayed = 0;
    public static int dailyComboCardsPlayedThisTurn = 0;
    public static boolean enteredShopThisVisit = false; // Tracks if we are currently in a shop room entered via normal transition
    public static boolean spentGoldInShopThisVisit = false; // Tracks if gold was spent since entering the current shop


    /**
     * Reset variables at the start of each turn
     * Call this at the start of each turn
     */
    public static void resetVariablesEveryTurn() {
        dailySetupPowersPlayed = 0;
        dailyComboCardsPlayedThisTurn = 0;
    }

    public static void resetShopVisitTracking() {
        enteredShopThisVisit = false;
        spentGoldInShopThisVisit = false;
    }
}