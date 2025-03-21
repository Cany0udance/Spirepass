package spirepass.patches.challengepatches.weeklies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.shop.Merchant;
import com.megacrit.cardcrawl.vfx.SpeechBubble;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

import java.util.ArrayList;

@SpirePatch(clz = Merchant.class, method = "update")
public class HairComplimentPatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = SpeechBubbleLocator.class,
            localvars = {"msg"}
    )
    public static void onSpeechBubble(Merchant __instance, String msg) {
        try {
            // Get the CharacterStrings to find the hair compliment text
            CharacterStrings characterStrings = CardCrawlGame.languagePack.getCharacterString("Merchant");
            String hairComplimentText = characterStrings.TEXT[1];

            // Check if this is the hair compliment message
            if (msg.equals(hairComplimentText)) {
                logger.info("Merchant complimented your haircut!");

                // Complete the challenge if it's active and incomplete
                if (ChallengeHelper.isActiveChallengeIncomplete("weekly_hair")) {
                    ChallengeHelper.completeChallenge("weekly_hair");
                    logger.info("Weekly Nice Hair challenge completed!");
                }
            }
        } catch (Exception e) {
            logger.error("Error in HairComplimentPatch: " + e.getMessage(), e);
        }
    }

    // Locator to find where the speech bubble is added to the effect list
    private static class SpeechBubbleLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(ArrayList.class, "add");
            // Find where it adds something to the effect list
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}