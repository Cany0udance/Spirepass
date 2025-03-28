package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

import static basemod.BaseMod.logger;

@SpirePatch(clz = ApplyPowerAction.class, method = "update")
public class BuffDebuffChallengePatch {

    @SpirePostfixPatch
    public static void checkChallengesAfterPowerAppliedByAction(ApplyPowerAction __instance) {

        AbstractCreature target = __instance.target;

        if (target != null && target.isPlayer && __instance.isDone) {

            int buffCount = 0;
            int debuffCount = 0;

            for (AbstractPower p : target.powers) {
                if (p.type == AbstractPower.PowerType.BUFF) {
                    buffCount++;
                } else if (p.type == AbstractPower.PowerType.DEBUFF) {
                    debuffCount++;
                }
            }

            if (buffCount >= 5) {
                if (ChallengeHelper.isActiveChallengeIncomplete("daily_buff")) {
                    ChallengeHelper.completeChallenge("daily_buff");
                }
            }

            if (debuffCount >= 4) {
                if (ChallengeHelper.isActiveChallengeIncomplete("daily_debuff")) {
                    ChallengeHelper.completeChallenge("daily_debuff");
                }
            }
        }
    }
}