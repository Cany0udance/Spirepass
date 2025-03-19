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
                2
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

        // Add more daily challenges here
        challenges.add(new Challenge(
                "daily_perfectionist",
                "Perfectionist",
                "Defeat an Elite without taking damage.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_overkill",
                "Overkill",
                "Deal 50+ damage in a single attack.",
                Challenge.ChallengeType.DAILY,
                1
        ));

        challenges.add(new Challenge(
                "daily_hoarder",
                "Card Collector",
                "Add 3 cards to your deck in a single combat reward.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_impervious",
                "Impervious",
                "Gain 30 or more Block in a single turn.",
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

        /*


        challenges.add(new Challenge(
                "weekly_boss",
                "You Are Probably Nothing",
                "Defeat a Boss within three turns.",
                Challenge.ChallengeType.WEEKLY
        ));

        // Add more weekly challenges here
        challenges.add(new Challenge(
                "weekly_collector",
                "Relic Collector",
                "Obtain 5 relics in a single run.",
                Challenge.ChallengeType.WEEKLY,
                5
        ));

        challenges.add(new Challenge(
                "weekly_ascension",
                "Ascension Climber",
                "Win a run at Ascension 5 or higher.",
                Challenge.ChallengeType.WEEKLY
        ));

        challenges.add(new Challenge(
                "weekly_minimalist",
                "Minimalist",
                "Have 15 or fewer cards in your deck by the end of Act 2.",
                Challenge.ChallengeType.WEEKLY
        ));

        challenges.add(new Challenge(
                "weekly_highlander",
                "Highlander",
                "Win a combat with no duplicate cards in your deck.",
                Challenge.ChallengeType.WEEKLY
        ));
        
         */

        return challenges;
    }
}