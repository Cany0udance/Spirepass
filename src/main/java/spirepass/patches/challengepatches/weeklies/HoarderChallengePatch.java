package spirepass.patches.challengepatches.weeklies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = AbstractPlayer.class, method = "gainGold")
public class HoarderChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = GainGoldLocator.class
    )
    public static void checkGoldAmount(AbstractPlayer __instance, int amount) {
        // Check if the weekly hoarder challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_hoarder")) {
            // Check if player has at least 600 gold after gaining this amount
            if (__instance.gold >= 600) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("weekly_hoarder");
                logger.info("Weekly Hoarder challenge completed! Current gold: " + __instance.gold);
            }
        }
    }

    // Locator to find the spot right after the player gains gold
    private static class GainGoldLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(AbstractRelic.class, "onGainGold");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}