package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = PotionPopUp.class, method = "updateInput")
public class PotionUseChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = PotionUseLocator.class,
            localvars = {"potion"}
    )
    public static void onPotionUse(PotionPopUp __instance, AbstractPotion potion) {
        // Check for the weekly chugger challenge
        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_chugger")) {
            // Increment the challenge progress for any potion use
            ChallengeHelper.updateChallengeProgress("weekly_chugger", 1);
        }

        // Check if the exquisite challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_exquisite")) {
            // Check if the used potion is rare
            if (potion.rarity == AbstractPotion.PotionRarity.RARE) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_exquisite");
            }
        }
    }

    // Locator to find the spot after potion use but before relic triggers
    private static class PotionUseLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(AbstractPotion.class, "use");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}