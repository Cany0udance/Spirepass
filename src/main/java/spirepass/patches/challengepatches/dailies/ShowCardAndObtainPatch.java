package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.CtBehavior;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = ShowCardAndObtainEffect.class, method = "update")
public class ShowCardAndObtainPatch {

    @SpireInsertPatch(
            locator = ObtainCardLocator.class,
            localvars = {"r", "card"}
    )
    public static void onCardObtain(ShowCardAndObtainEffect __instance, AbstractRelic r, AbstractCard card) { // Add AbstractCard card parameter
        // Existing challenges
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_hobbyist")) {
            ChallengeHelper.updateChallengeProgress("daily_hobbyist", 1);
        }
        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_collector")) {
            ChallengeHelper.updateChallengeProgress("weekly_collector", 1);
        }

        if (ChallengeHelper.isActiveChallengeIncomplete("daily_colorless")) {
            if (card != null && card.color == AbstractCard.CardColor.COLORLESS) {
                ChallengeHelper.updateChallengeProgress("daily_colorless", 1);
            }
        }
    }

    private static class ObtainCardLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(AbstractRelic.class, "onObtainCard");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}