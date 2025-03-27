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

        // Check if the weekly slayer challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_slayer")) {
            ChallengeHelper.updateChallengeProgress("weekly_slayer", 1);
        }

        // Check if monster is an Elite
        if (__instance.type == AbstractMonster.EnemyType.ELITE) {
            // Check if the daily punching up challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_punchingup")) {
                // Increment the challenge progress
                ChallengeHelper.updateChallengeProgress("daily_punchingup", 1);
            }

            // Check if the weekly big game hunter challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("weekly_biggame")) {
                // Increment the challenge progress
                ChallengeHelper.updateChallengeProgress("weekly_biggame", 1);
            }
        }
    }
}