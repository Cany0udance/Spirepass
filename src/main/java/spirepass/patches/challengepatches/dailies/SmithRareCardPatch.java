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
public class SmithRareCardPatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = UpgradeCardLocator.class,
            localvars = {"c"}
    )
    public static void onCardUpgrade(CampfireSmithEffect __instance, AbstractCard c) {
        // Check if the best challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_best")) {
            // Check if the upgraded card is rare
            if (c.rarity == AbstractCard.CardRarity.RARE) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_best");
                logger.info("Daily Best of the Best challenge completed! Upgraded rare card: " + c.name);
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