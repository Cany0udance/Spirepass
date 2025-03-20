package spirepass.challengeutil;

import java.util.ArrayList;
import java.util.List;

public class ChallengeDefinitions {

    /**
     * Get all available daily challenges
     */
    public static List<Challenge> getAllDailyChallenges() {
        List<Challenge> challenges = new ArrayList<>();

        // Define all possible daily challenges here
        challenges.add(new Challenge(
                "daily_setup",
                "Setup",
                "Play two Power cards on turn 1 of combat.",
                Challenge.ChallengeType.DAILY,
                1
        ));

        challenges.add(new Challenge(
                "daily_thanks",
                "Thaaaanks",
                "Purchase a Shop-tier Relic from the Merchant.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_best",
                "Best of the Best",
                "Upgrade a Rare card at a Rest Site.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_purification",
                "Purification",
                "Remove a Curse at a Merchant.",
                Challenge.ChallengeType.DAILY
        ));

        return challenges;
    }

    /**
     * Get all available weekly challenges
     */
    public static List<Challenge> getAllWeeklyChallenges() {
        List<Challenge> challenges = new ArrayList<>();

        // Define all possible weekly challenges here
        challenges.add(new Challenge(
                "weekly_hair",
                "Nice Hair",
                "Have the Merchant compliment your haircut.",
                Challenge.ChallengeType.WEEKLY
        ));

        challenges.add(new Challenge(
                "weekly_strong_start",
                "Strong Start",
                "Defeat an Emerald Elite in Act 1.",
                Challenge.ChallengeType.WEEKLY
        ));

        challenges.add(new Challenge(
                "weekly_hoarder",
                "Hoarder",
                "Have at least 500 Gold at once.",
                Challenge.ChallengeType.WEEKLY
        ));

        return challenges;
    }
}