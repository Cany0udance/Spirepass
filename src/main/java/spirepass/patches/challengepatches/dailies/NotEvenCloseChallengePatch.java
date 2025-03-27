package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = AbstractRoom.class, method = "endBattle")
public class NotEvenCloseChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = BattleVictoryLocator.class
    )
    public static void onBattleVictory(AbstractRoom __instance) {
        // Check if player has 10 HP or less
        if (AbstractDungeon.player.currentHealth <= 10) {

            // Check if the Not Even Close challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_notevenclose")) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_notevenclose");
            }
        }
    }

    // Locator to find the spot after the "SHRUG_IT_OFF" achievement check
    private static class BattleVictoryLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(UnlockTracker.class, "unlockAchievement");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}