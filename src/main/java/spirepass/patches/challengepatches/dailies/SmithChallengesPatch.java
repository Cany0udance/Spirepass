package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.vfx.campfire.CampfireSmithEffect;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = CampfireSmithEffect.class, method = "update")
public class SmithChallengesPatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = UpgradeCardLocator.class,
            localvars = {"c"}
    )
    public static void onCardUpgrade(CampfireSmithEffect __instance, AbstractCard c) {
        // Weekly Smith challenge - track any smithing
        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_smith")) {
            // Update the progress for the Smith challenge
            ChallengeHelper.updateChallengeProgress("weekly_smith", 1);
            logger.info("Weekly Smith challenge progress incremented!");
        }

        // Daily Best of the Best challenge - rare card smithing
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_best")) {
            // Check if the upgraded card is rare
            if (c.rarity == AbstractCard.CardRarity.RARE) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_best");
                logger.info("Daily Best of the Best challenge completed! Upgraded rare card: " + c.name);
            }
        }

        // Daily Back to Basics challenge - basic card smithing
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_backtobasics")) {
            // Check if the upgraded card is Basic
            if (c.rarity == AbstractCard.CardRarity.BASIC) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_backtobasics");
                logger.info("Daily Back to Basics challenge completed! Upgraded basic card: " + c.name);
            }
        }
    }

    // Locator to find the spot right after the card upgrade line
    private static class UpgradeCardLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(AbstractCard.class, "upgrade");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}