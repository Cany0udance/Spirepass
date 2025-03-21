package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = PotionPopUp.class, method = "updateTargetMode")
public class RareTargetPotionPatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = TargetPotionUseLocator.class,
            localvars = {"potion"}
    )
    public static void onTargetPotionUse(PotionPopUp __instance, AbstractPotion potion) {
        // Check if the exquisite challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_exquisite")) {
            // Check if the used potion is rare
            if (potion.rarity == AbstractPotion.PotionRarity.RARE) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_exquisite");
                logger.info("Daily Exquisite challenge completed! Used rare target potion: " + potion.name);
            }
        }
    }

    // Locator to find the spot after targeted potion use
    private static class TargetPotionUseLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(AbstractPotion.class, "use");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}