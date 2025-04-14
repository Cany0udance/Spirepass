package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(
        clz = AbstractCreature.class,
        method = "addBlock",
        paramtypez = {int.class}
)
public class ProtectedChallengePatch {

    @SpirePostfixPatch
    public static void checkBlockTotal(AbstractCreature __instance, int blockAmount) {

        if (!ChallengeHelper.isActiveChallengeIncomplete("daily_protected")) {
            return;
        }

        if (__instance.isPlayer) {
            if (__instance.currentBlock >= 50) {
                ChallengeHelper.completeChallenge("daily_protected");
            }
        }
    }
}