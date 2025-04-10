package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.audio.SoundMaster;
import com.megacrit.cardcrawl.vfx.scene.InteractableTorchEffect;
import javassist.CtBehavior;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(
        clz = InteractableTorchEffect.class,
        method = "update"
)
public class TorchExtinguishChallengePatch {

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void onTorchExtinguish(InteractableTorchEffect __instance) {
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_lightsout")) {
            ChallengeHelper.updateChallengeProgress("daily_lightsout", 1);
        }
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(SoundMaster.class, "play");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}