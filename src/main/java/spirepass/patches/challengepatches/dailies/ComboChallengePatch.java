package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = GameActionManager.class, method = "getNextAction")
public class ComboChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = ComboLocator.class
    )
    public static void onCardPlayed(GameActionManager __instance) {
        // Check if the number of cards played this turn is exactly 10
        // This ensures we only trigger once when the player reaches 10 cards
        if (__instance.cardsPlayedThisTurn.size() == 10) {
            // Check if the Combo challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_combo")) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_combo");
            }
        }
    }

    // Locator to find the spot right after checking for the INFINITY achievement
    private static class ComboLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(UnlockTracker.class, "unlockAchievement");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}