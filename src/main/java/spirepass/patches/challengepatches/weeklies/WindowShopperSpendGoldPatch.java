package spirepass.patches.challengepatches.weeklies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import spirepass.challengeutil.ChallengeVariables;

@SpirePatch(
        clz = AbstractPlayer.class,
        method = "loseGold"
)
public class WindowShopperSpendGoldPatch {

    @SpirePostfixPatch
    public static void postfix(AbstractPlayer __instance, int goldAmount) {
        if (goldAmount > 0 && AbstractDungeon.getCurrRoom() instanceof ShopRoom) {
            if (ChallengeVariables.enteredShopThisVisit) {
                ChallengeVariables.spentGoldInShopThisVisit = true;
            }
        }
    }
}