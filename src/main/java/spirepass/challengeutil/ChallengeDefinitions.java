package spirepass.challengeutil;

import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;

import java.util.ArrayList;
import java.util.List;

import static spirepass.Spirepass.makeID;

public class ChallengeDefinitions {

    /**
     * Get all available daily challenges
     */
    public static List<Challenge> getAllDailyChallenges() {
        List<Challenge> challenges = new ArrayList<>();

        // Define all possible daily challenges here
        challenges.add(createChallenge("daily_setup", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_thanks", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_best", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_purification", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_exquisite", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_hobbyist", Challenge.ChallengeType.DAILY, 15));
        challenges.add(createChallenge("daily_punchingup", Challenge.ChallengeType.DAILY, 5));
        challenges.add(createChallenge("daily_buff", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_debuff", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_backtobasics", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_combo", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_risktaker", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_notevenclose", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_bossswap", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_unbeatable", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_youarewinner", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_tagalong", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_silent", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_cactus", Challenge.ChallengeType.DAILY));
        challenges.add(createChallenge("daily_lightsout", Challenge.ChallengeType.DAILY, 5));
        challenges.add(createChallenge("daily_ascender", Challenge.ChallengeType.DAILY, 50));
        challenges.add(createChallenge("daily_philosopher", Challenge.ChallengeType.DAILY, 3));
        challenges.add(createRandomizedStorytimeChallenge());

        return challenges;
    }

    /**
     * Creates the storytime challenge with randomized text sets
     */
    private static Challenge createRandomizedStorytimeChallenge() {
        UIStrings strings = CardCrawlGame.languagePack.getUIString(makeID("Challengedaily_storytime"));

        // Random number between 0 and 2 to choose one of the three sets
        int randomSet = MathUtils.random(0, 2);

        String title, description;

        switch (randomSet) {
            case 0:
                title = strings.TEXT[0];
                description = strings.TEXT[3];
                break;
            case 1:
                title = strings.TEXT[1];
                description = strings.TEXT[4];
                break;
            case 2:
            default:
                title = strings.TEXT[2];
                description = strings.TEXT[5];
                break;
        }

        return new Challenge("daily_storytime", title, description, Challenge.ChallengeType.DAILY);
    }

    /**
     * Get all available weekly challenges
     */
    public static List<Challenge> getAllWeeklyChallenges() {
        List<Challenge> challenges = new ArrayList<>();

        // Define all possible weekly challenges here
        challenges.add(createChallenge("weekly_hair", Challenge.ChallengeType.WEEKLY));
        challenges.add(createChallenge("weekly_strong_start", Challenge.ChallengeType.WEEKLY));
        challenges.add(createChallenge("weekly_hoarder", Challenge.ChallengeType.WEEKLY));
        challenges.add(createChallenge("weekly_collector", Challenge.ChallengeType.WEEKLY, 100));
        challenges.add(createChallenge("weekly_chonky", Challenge.ChallengeType.WEEKLY));
        challenges.add(createChallenge("weekly_slayer", Challenge.ChallengeType.WEEKLY, 150));
        challenges.add(createChallenge("weekly_biggame", Challenge.ChallengeType.WEEKLY, 30));
        challenges.add(createChallenge("weekly_chugger", Challenge.ChallengeType.WEEKLY, 75));
        challenges.add(createChallenge("weekly_unknown", Challenge.ChallengeType.WEEKLY, 30));
        challenges.add(createChallenge("weekly_smith", Challenge.ChallengeType.WEEKLY, 50));
        challenges.add(createChallenge("weekly_snooze", Challenge.ChallengeType.WEEKLY, 25));
        challenges.add(createChallenge("weekly_snooze", Challenge.ChallengeType.WEEKLY, 25));
        challenges.add(createChallenge("weekly_midas", Challenge.ChallengeType.WEEKLY, 70));
        challenges.add(createChallenge("weekly_ghostbuster", Challenge.ChallengeType.WEEKLY));
        challenges.add(createChallenge("weekly_freeloader", Challenge.ChallengeType.WEEKLY));
        challenges.add(createChallenge("weekly_dailymaster", Challenge.ChallengeType.WEEKLY));

        return challenges;
    }

    /**
     * Create a challenge with localization support
     */
    private static Challenge createChallenge(String id, Challenge.ChallengeType type) {
        UIStrings strings = CardCrawlGame.languagePack.getUIString(makeID("Challenge" + id));
        return new Challenge(id, strings.TEXT[0], strings.TEXT[1], type);
    }

    /**
     * Create a challenge with localization support and a progress counter
     */
    private static Challenge createChallenge(String id, Challenge.ChallengeType type, int progressTarget) {
        UIStrings strings = CardCrawlGame.languagePack.getUIString(makeID("Challenge" + id));
        return new Challenge(id, strings.TEXT[0], strings.TEXT[1], type, progressTarget);
    }
}