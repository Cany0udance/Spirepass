package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.shop.ShopScreen;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

import java.lang.reflect.Field;

// Alternative implementation using a different patch approach
// Simplest approach - patch CardGroup's removeCard method
@SpirePatch(clz = CardGroup.class, method = "removeCard", paramtypez = {AbstractCard.class})
public class PurgeCursePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = CurseLocator.class,
            localvars = {"c"}
    )
    public static void onCardRemove(CardGroup __instance, AbstractCard c) {
        // Only check if this is player's master deck and we're in a shop
        if (__instance == AbstractDungeon.player.masterDeck &&
                AbstractDungeon.getCurrRoom() instanceof ShopRoom) {

            // Check if the purification challenge is active and incomplete
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_purification")) {
                // Check if the removed card is a curse
                if (c.type == AbstractCard.CardType.CURSE) {
                    // Complete the challenge
                    ChallengeHelper.completeChallenge("daily_purification");
                }
            }
        }
    }

    // Simple locator that triggers at the start of the method
    private static class CurseLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            return new int[]{0}; // Beginning of method
        }
    }
}