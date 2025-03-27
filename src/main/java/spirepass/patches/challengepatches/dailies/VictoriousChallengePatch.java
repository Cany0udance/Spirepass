package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = AbstractMonster.class, method = "onFinalBossVictoryLogic")
public class VictoriousChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpirePostfixPatch
    public static void onRunVictory(AbstractMonster __instance) {
        // This method is called when the player defeats the final boss

        // Check if the Victorious challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_youarewinner")) {
            // Complete the challenge
            ChallengeHelper.completeChallenge("daily_youarewinner");
        }
    }
}