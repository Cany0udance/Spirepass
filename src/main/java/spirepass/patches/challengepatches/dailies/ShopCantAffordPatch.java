package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.shop.ShopScreen;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(
        clz = ShopScreen.class,
        method = "playCantBuySfx"
)
public class ShopCantAffordPatch {

    @SpirePostfixPatch
    public static void onCantAffordSound(ShopScreen __instance) {
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_speech0")) {
            ChallengeHelper.completeChallenge("daily_speech0");
        }
    }
}