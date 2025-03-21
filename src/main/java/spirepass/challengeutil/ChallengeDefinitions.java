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
                Challenge.ChallengeType.DAILY
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

        challenges.add(new Challenge(
                "daily_exquisite",
                "Exquisite",
                "Use a Rare potion.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_hobbyist",
                "Hobbyist",
                "Add 15 cards to your deck.",
                Challenge.ChallengeType.DAILY,
                15
        ));

        challenges.add(new Challenge(
                "daily_punchingup",
                "Punching Up",
                "Win 5 Elite combats.",
                Challenge.ChallengeType.DAILY,
                5
        ));

        challenges.add(new Challenge(
                "daily_buff",
                "Enjoyer of Buffs",
                "Have 5 or more buffs active at once.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_debuff",
                "Enjoyer of Debuffs",
                "Have 4 or more debuffs active at once.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_backtobasics",
                "Back to Basics",
                "Smith a Basic card at a Rest Site.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_combo",
                "Combo",
                "Play 10 or more cards in a single turn.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_risktaker",
                "Risk Taker",
                "Enter a Boss fight with 20 or less HP.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_notevenclose",
                "Not Even Close",
                "Win a fight with 10 HP or less remaining.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_bossswap",
                "Snecko? Pyramid??",
                "Give up your starter Relic for a Boss Relic from Neow.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_youarewinner",
                "Victorious",
                "Win a run.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_tagalong",
                "Tagalong",
                "Complete the other 2 daily challenges.",
                Challenge.ChallengeType.DAILY
        ));

        challenges.add(new Challenge(
                "daily_silent",
                "The Silent",
                "Experience silence.",
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
                "Collect the Emerald Key in Exordium.",
                Challenge.ChallengeType.WEEKLY
        ));

        challenges.add(new Challenge(
                "weekly_hoarder",
                "Hoarder",
                "Have at least 600 Gold at once.",
                Challenge.ChallengeType.WEEKLY
        ));

        challenges.add(new Challenge(
                "weekly_collector",
                "Card Collector",
                "Add 100 cards to your deck.",
                Challenge.ChallengeType.WEEKLY,
                100
        ));

        challenges.add(new Challenge(
                "weekly_chonky",
                "Chonky",
                "Have at least 110 max HP during a run.",
                Challenge.ChallengeType.WEEKLY
        ));

        challenges.add(new Challenge(
                "weekly_slayer",
                "Slayer",
                "Defeat 150 enemies.",
                Challenge.ChallengeType.WEEKLY,
                150
                ));

        challenges.add(new Challenge(
                "weekly_biggame",
                "Big Game Hunter",
                "Defeat 30 Elite enemies.",
                Challenge.ChallengeType.WEEKLY,
                30
        ));

        challenges.add(new Challenge(
                "weekly_chugger",
                "Chugger",
                "Use 75 potions.",
                Challenge.ChallengeType.WEEKLY,
                75
        ));

        challenges.add(new Challenge(
                "weekly_unknown",
                "Into the Unknown",
                "Visit 30 Unknown nodes.",
                Challenge.ChallengeType.WEEKLY,
                30
        ));

        challenges.add(new Challenge(
                "weekly_smith",
                "The Smith",
                "Smith 50 times at Rest Sites.",
                Challenge.ChallengeType.WEEKLY,
                50
        ));

        challenges.add(new Challenge(
                "weekly_snooze",
                "The Snoozer",
                "Rest 25 times at Rest Sites.",
                Challenge.ChallengeType.WEEKLY,
                25
        ));

        challenges.add(new Challenge(
                "weekly_freeloader",
                "Freeloader",
                "Complete the other 2 weekly challenges.",
                Challenge.ChallengeType.WEEKLY
        ));

        challenges.add(new Challenge(
                "weekly_dailymaster",
                "Daily Master",
                "Have all 3 daily challenges completed at once.",
                Challenge.ChallengeType.WEEKLY
        ));

        return challenges;
    }
}