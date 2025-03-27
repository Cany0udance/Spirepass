package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.Challenge;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(
        clz = AbstractMonster.class,
        method = "die",
        paramtypez = {boolean.class}
)
public class MonsterKillsChallengesPatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpirePostfixPatch
    public static void onMonsterDeath(AbstractMonster __instance, boolean triggerRelics) {
        // Only proceed if relics are being triggered (real death)
        if (!triggerRelics) {
            return;
        }

        logger.info("DEATH: " + __instance.id + " / " + __instance.name +
                " / isDying=" + __instance.isDying +
                " / HP=" + __instance.currentHealth +
                " / Type=" + __instance.type);

        // Check if the weekly slayer challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_slayer")) {
            Challenge challenge = ChallengeHelper.getActiveChallenge("weekly_slayer");
            int before = challenge != null ? challenge.getCurrentProgress() : -1;

            ChallengeHelper.updateChallengeProgress("weekly_slayer", 1);

            challenge = ChallengeHelper.getActiveChallenge("weekly_slayer");
            int after = challenge != null ? challenge.getCurrentProgress() : -1;

            logger.info("SLAYER: " + before + " -> " + after + " / " +
                    (challenge != null ? challenge.getMaxProgress() : "??") +
                    " (Enemy: " + __instance.name + ") Completed: " +
                    (challenge != null ? challenge.isCompleted() : false));
        }

        // Check if monster is an Elite
        if (__instance.type == AbstractMonster.EnemyType.ELITE) {
            // Check if the daily punching up challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_punchingup")) {
                // Increment the challenge progress
                ChallengeHelper.updateChallengeProgress("daily_punchingup", 1);
                logger.info("Daily Punching Up challenge progress incremented! Elite defeated: " + __instance.name);
            }

            // Check if the weekly big game hunter challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("weekly_biggame")) {
                // Increment the challenge progress
                ChallengeHelper.updateChallengeProgress("weekly_biggame", 1);
                logger.info("Weekly Big Game Hunter challenge progress incremented! Elite defeated: " + __instance.name);
            }
        }
    }
}