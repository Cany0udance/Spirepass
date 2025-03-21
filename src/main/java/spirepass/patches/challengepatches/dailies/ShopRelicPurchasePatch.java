package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StoreRelic;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = ShopScreen.class, method = "updateRelics")
public class ShopRelicPurchasePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = RelicPurchaseLocator.class,
            localvars = {"r"}
    )
    public static void onRelicPurchased(ShopScreen __instance, StoreRelic r) {
        // Check if the thanks challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_thanks")) {
            // Check if the purchased relic is a shop-tier relic
            if (r.relic.tier == AbstractRelic.RelicTier.SHOP) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_thanks");
                logger.info("Daily Thanks challenge completed! Purchased shop relic: " + r.relic.name);
            }
        }
    }

    // Locator to find the spot right after checking if a relic is purchased
    private static class RelicPurchaseLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToBePatched) throws Exception {
            Matcher matcher = new Matcher.FieldAccessMatcher(StoreRelic.class, "isPurchased");
            return LineFinder.findInOrder(ctMethodToBePatched, matcher);
        }
    }
}