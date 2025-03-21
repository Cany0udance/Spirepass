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
public class RarePotionPatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = PotionUseLocator.class,
            localvars = {"potion"}
    )
    public static void onPotionUse(PotionPopUp __instance, AbstractPotion potion) {
        // Check if the exquisite challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_exquisite")) {
            // Check if the used potion is rare
            if (potion.rarity == AbstractPotion.PotionRarity.RARE) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_exquisite");
                logger.info("Daily Exquisite challenge completed! Used rare potion: " + potion.name);
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