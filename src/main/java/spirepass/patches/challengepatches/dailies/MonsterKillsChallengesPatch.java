package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.TorchHead;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(
        clz = AbstractMonster.class,
        method = "die",
        paramtypez = {boolean.class}
)
public class MonsterKillsChallengesPatch {

    @SpirePostfixPatch
    public static void onMonsterDeath(AbstractMonster __instance, boolean triggerRelics) {
        if (!triggerRelics) {
            return;
        }

        if (__instance == null) {
            return;
        }

        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_slayer")) {
            ChallengeHelper.updateChallengeProgress("weekly_slayer", 1);
        }

        if (ChallengeHelper.isActiveChallengeIncomplete("daily_lightsout") && TorchHead.ID.equals(__instance.id)) {
            ChallengeHelper.updateChallengeProgress("daily_lightsout", 1);
        }

        if (__instance.type == AbstractMonster.EnemyType.ELITE) {
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_punchingup")) {
                ChallengeHelper.updateChallengeProgress("daily_punchingup", 1);
            }

            if (ChallengeHelper.isActiveChallengeIncomplete("weekly_biggame")) {
                ChallengeHelper.updateChallengeProgress("weekly_biggame", 1);
            }
        }
    }
}