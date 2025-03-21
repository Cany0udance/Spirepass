package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = AbstractPlayer.class, method = "applyStartOfCombatLogic")
public class RiskTakerChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpirePostfixPatch
    public static void onCombatStart(AbstractPlayer __instance) {
        // Check if this is a boss fight
        boolean isBossFight = false;
        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            if (monster.type == AbstractMonster.EnemyType.BOSS) {
                isBossFight = true;
                break;
            }
        }

        // If it's a boss fight and the player has 20 or less HP
        if (isBossFight && __instance.currentHealth <= 20) {
            logger.info("Player entered boss fight with " + __instance.currentHealth + " HP");

            // Check if the Risk Taker challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_risktaker")) {
                // Complete the challenge
                ChallengeHelper.completeChallenge("daily_risktaker");
                logger.info("Daily Risk Taker challenge completed! Entered boss fight with " +
                        __instance.currentHealth + " HP.");
            }
        }
    }
}