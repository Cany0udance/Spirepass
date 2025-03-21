package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.neow.NeowReward;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = NeowReward.class, method = "activate")
public class BossSwapRelicPatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = BossRelicSwapLocator.class
    )
    public static void onBossRelicSwap(NeowReward __instance) {
        // We've found the boss relic swap case
        logger.info("Player swapped starter relic for a boss relic");

        // Check if the Boss Swap challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_bossswap")) {
            // Complete the challenge
            ChallengeHelper.completeChallenge("daily_bossswap");
            logger.info("Daily Snecko? Pyramid?? challenge completed! Swapped starter relic for a boss relic.");
        }
    }

    // Locator to find the boss relic swap case
    private static class BossRelicSwapLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(AbstractPlayer.class, "loseRelic");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}