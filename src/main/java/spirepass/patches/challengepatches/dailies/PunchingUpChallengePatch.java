package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.stats.StatsScreen;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(
        clz = AbstractMonster.class,
        method = "die",
        paramtypez = {boolean.class}
)
public class PunchingUpChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = RelicTriggerLocator.class
    )
    public static void onMonsterDeath(AbstractMonster __instance, boolean triggerRelics) {
        // Only proceed if relics are being triggered (real death)
        if (!triggerRelics) {
            return;
        }

        // Check if monster is an Elite
        if (__instance.type == AbstractMonster.EnemyType.ELITE) {
            // Check if the Punching Up challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_punchingup")) {
                // Increment the challenge progress
                ChallengeHelper.updateChallengeProgress("daily_punchingup", 1);
                logger.info("Daily Punching Up challenge progress incremented! Elite defeated: " + __instance.name);
            }
        }
    }

    // Locator to find the spot after relic triggers
    private static class RelicTriggerLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(AbstractRelic.class, "onMonsterDeath");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}