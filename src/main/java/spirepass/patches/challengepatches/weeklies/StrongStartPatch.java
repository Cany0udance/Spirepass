package spirepass.patches.challengepatches.weeklies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.ObtainKeyEffect;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = RewardItem.class, method = "claimReward")
public class StrongStartPatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = EmeraldKeyLocator.class
    )
    public static void onClaimEmeraldKey(RewardItem __instance) {
        try {
            // Check if we're in Exordium (Act 1)
            if (AbstractDungeon.actNum == 1) {

                // Complete the challenge if it's active and incomplete
                if (ChallengeHelper.isActiveChallengeIncomplete("weekly_strong_start")) {
                    ChallengeHelper.completeChallenge("weekly_strong_start");
                }
            }
        } catch (Exception e) {
        }
    }

    // Locator to find the EMERALD_KEY case
    private static class EmeraldKeyLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.FieldAccessMatcher(ObtainKeyEffect.KeyColor.class, "GREEN");
            // Find where it references KeyColor.GREEN
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}