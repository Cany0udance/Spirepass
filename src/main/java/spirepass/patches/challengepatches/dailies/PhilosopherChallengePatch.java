package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(
        clz = ThoughtBubble.class,
        method = SpirePatch.CONSTRUCTOR,
        paramtypez = {
                float.class,
                float.class,
                float.class,
                String.class,
                boolean.class
        }
)
public class PhilosopherChallengePatch {

    @SpirePostfixPatch
    public static void trackPlayerThinking(ThoughtBubble __instance, float x, float y, float duration, String msg, boolean isPlayer) {
        // Only track when it's the player who is thinking
        if (isPlayer) {
            // Check if the daily philosopher challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_philosopher")) {
                // Update the progress for the Philosopher challenge
                ChallengeHelper.updateChallengeProgress("daily_philosopher", 1);
            }
        }
    }
}