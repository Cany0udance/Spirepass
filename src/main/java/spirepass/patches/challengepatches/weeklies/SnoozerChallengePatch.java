package spirepass.patches.challengepatches.weeklies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.ui.campfire.RestOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = RestOption.class, method = "useOption")
public class SnoozerChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpirePostfixPatch
    public static void trackRestUsage(RestOption __instance) {
        // Check if the weekly snoozer challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_snooze")) {
            // Update the progress for the Snoozer challenge
            ChallengeHelper.updateChallengeProgress("weekly_snooze", 1);
        }
    }
}