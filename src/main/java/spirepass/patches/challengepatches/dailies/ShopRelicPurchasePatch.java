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

@SpirePatch(clz = StoreRelic.class, method = "purchaseRelic")
public class ShopRelicPurchasePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = RelicPurchaseLocator.class
    )
    public static void onRelicPurchased(StoreRelic __instance) {
        // Only check after a successful purchase (when player has enough gold)
        if (__instance.relic != null && __instance.relic.tier == AbstractRelic.RelicTier.SHOP) {
            // Check if the thanks challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_thanks")) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_thanks");
                logger.info("Daily Thanks challenge completed! Purchased shop relic: " + __instance.relic.name);
            }
        }
    }

    // Locator to find the spot right after the relic is obtained
    private static class RelicPurchaseLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToBePatched) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(AbstractRelic.class, "instantObtain");
            return LineFinder.findInOrder(ctMethodToBePatched, matcher);
        }
    }
}