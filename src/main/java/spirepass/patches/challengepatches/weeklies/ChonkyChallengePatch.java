package spirepass.patches.challengepatches.weeklies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = AbstractCreature.class, method = "increaseMaxHp")
public class ChonkyChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = MaxHpIncreaseLocator.class
    )
    public static void checkMaxHp(AbstractCreature __instance, int amount, boolean showEffect) {
        // Only proceed if this is the player character
        if (__instance instanceof AbstractPlayer) {
            // Check if the weekly chonky challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("weekly_chonky")) {
                // Check if player has at least 100 max HP after the increase
                if (__instance.maxHealth >= 100) {
                    // Complete the challenge
                    ChallengeHelper.completeChallenge("weekly_chonky");
                    logger.info("Weekly Chonky challenge completed! Current max HP: " + __instance.maxHealth);
                }
            }
        }
    }

    // Locator to find the spot right after max health is increased
    private static class MaxHpIncreaseLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.FieldAccessMatcher(AbstractCreature.class, "maxHealth");
            // Get the line after maxHealth is updated
            int[] lines = LineFinder.findAllInOrder(ctMethodToPatch, matcher);
            // We want the first occurrence which is where maxHealth += amount happens
            return new int[]{lines[0] + 1};
        }
    }
}