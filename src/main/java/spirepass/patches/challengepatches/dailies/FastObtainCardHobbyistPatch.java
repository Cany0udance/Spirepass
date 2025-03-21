package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.Challenge;
import spirepass.challengeutil.ChallengeHelper;
import spirepass.challengeutil.ChallengeManager;

@SpirePatch(clz = FastCardObtainEffect.class, method = "update")
public class FastObtainCardHobbyistPatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = ObtainCardLocator.class,
            localvars = {"r"}
    )
    public static void onCardObtain(FastCardObtainEffect __instance, AbstractRelic r) {
        // Check for daily_hobbyist challenge
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_hobbyist")) {
            // Update the progress for the Hobbyist challenge
            ChallengeHelper.updateChallengeProgress("daily_hobbyist", 1);
            logger.info("Daily Hobbyist challenge progress incremented!");
        }

        // Check for weekly_collector challenge
        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_collector")) {
            // Update the progress for the Collector challenge
            ChallengeHelper.updateChallengeProgress("weekly_collector", 1);
            logger.info("Weekly Card Collector challenge progress incremented!");
        }
    }

    // Locator to find the spot right after the card obtain line
    private static class ObtainCardLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(AbstractRelic.class, "onObtainCard");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}