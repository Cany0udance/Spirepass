package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = AbstractCreature.class, method = "addPower")
public class BuffDebuffChallengePatch {
    @SpireInsertPatch(
            locator = AfterPowerAddLocator.class
    )
    public static void onAddPower(AbstractCreature __instance, AbstractPower powerToApply) {
        // Only proceed if this is the player
        if (!__instance.isPlayer) {
            return;
        }

        // Count active buffs and debuffs
        int buffCount = 0;
        int debuffCount = 0;

        for (AbstractPower p : __instance.powers) {
            if (p.type == AbstractPower.PowerType.BUFF) {
                buffCount++;
            } else if (p.type == AbstractPower.PowerType.DEBUFF) {
                debuffCount++;
            }
        }

        // Check buff challenge
        if (buffCount >= 5) {
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_buff")) {
                ChallengeHelper.completeChallenge("daily_buff");
            }
        }

        // Check debuff challenge
        if (debuffCount >= 4) {
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_debuff")) {
                ChallengeHelper.completeChallenge("daily_debuff");
            }
        }
    }

    // Locator to find the spot after the power is added
    private static class AfterPowerAddLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.FieldAccessMatcher(AbstractCreature.class, "powers");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}