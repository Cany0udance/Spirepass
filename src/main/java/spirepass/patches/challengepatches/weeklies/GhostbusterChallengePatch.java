package spirepass.patches.challengepatches.weeklies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.monsters.exordium.Hexaghost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

import java.util.Calendar;

@SpirePatch(clz = Hexaghost.class, method = "die")
public class GhostbusterChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpirePostfixPatch
    public static void checkGhostbusterChallenge(Hexaghost __instance) {
        // Only proceed if the weekly ghostbuster challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_ghostbuster")) {
            // Get the current hour
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

            // Check if current time is between midnight (0) and 6 AM
            if (currentHour >= 0 && currentHour < 6) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("weekly_ghostbuster");
            }
        }
    }
}