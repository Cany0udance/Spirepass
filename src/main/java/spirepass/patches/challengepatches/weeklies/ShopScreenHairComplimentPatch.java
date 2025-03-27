package spirepass.patches.challengepatches.weeklies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.shop.ShopScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = ShopScreen.class, method = "getIdleMsg")
public class ShopScreenHairComplimentPatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpirePostfixPatch
    public static String Postfix(String result, ShopScreen __instance) {
        try {
            // Get the CharacterStrings to find the hair compliment text
            CharacterStrings shopStrings = CardCrawlGame.languagePack.getCharacterString("Shop Screen");
            String hairComplimentText = shopStrings.TEXT[1]; // "I like your haircut"

            // Check if this is the hair compliment message
            if (result.equals(hairComplimentText)) {

                // Complete the challenge if it's active and incomplete
                if (ChallengeHelper.isActiveChallengeIncomplete("weekly_hair")) {
                    ChallengeHelper.completeChallenge("weekly_hair");
                }
            }
        } catch (Exception e) {
        }

        // Return the original result regardless
        return result;
    }
}