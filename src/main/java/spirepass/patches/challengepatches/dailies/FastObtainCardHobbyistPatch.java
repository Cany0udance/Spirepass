package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.Challenge;
import spirepass.challengeutil.ChallengeHelper;
import spirepass.challengeutil.ChallengeManager;

@SpirePatch(clz = FastCardObtainEffect.class, method = "update")
public class FastObtainCardHobbyistPatch {

    @SpireInsertPatch(
            locator = PostSoulsObtainLocator.class
    )
    public static void insertAfterCardObtain(FastCardObtainEffect __instance) {
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_hobbyist")) {
            ChallengeHelper.updateChallengeProgress("daily_hobbyist", 1);
        }

        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_collector")) {
            ChallengeHelper.updateChallengeProgress("weekly_collector", 1);
        }
    }

    private static class PostSoulsObtainLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher iteratorMatcher = new Matcher.MethodCallMatcher(java.util.ArrayList.class, "iterator");
            int[] allOccurrences = LineFinder.findAllInOrder(ctMethodToPatch, iteratorMatcher);

            if (allOccurrences.length < 2) {
            }
            return new int[]{allOccurrences[1]};
        }
    }
}