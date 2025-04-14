package spirepass.patches.challengepatches.weeklies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import spirepass.challengeutil.ChallengeHelper;
import spirepass.challengeutil.ChallengeVariables;

@SpirePatch(
        clz = AbstractDungeon.class,
        method = "nextRoomTransition",
        paramtypez = {SaveFile.class}
)
public class WindowShopperRoomTransitionPatch {

    @SpirePrefixPatch
    public static void prefix(AbstractDungeon __instance, SaveFile saveFile) {
        AbstractRoom currentRoom = AbstractDungeon.getCurrRoom();

        // Check if we are leaving a shop room that we had entered normally
        if (currentRoom instanceof ShopRoom && ChallengeVariables.enteredShopThisVisit) {
            if (!ChallengeVariables.spentGoldInShopThisVisit) {
                if (ChallengeHelper.isActiveChallengeIncomplete("weekly_windowshopper")) {
                    ChallengeHelper.updateChallengeProgress("weekly_windowshopper", 1);
                }
            }
            ChallengeVariables.resetShopVisitTracking();
        }
        else if (!(currentRoom instanceof ShopRoom) && ChallengeVariables.enteredShopThisVisit) {
            ChallengeVariables.resetShopVisitTracking();
        }
    }

    @SpirePostfixPatch
    public static void postfix(AbstractDungeon __instance, SaveFile saveFile) {
        AbstractRoom nextRoom = AbstractDungeon.getCurrRoom();

        if (nextRoom instanceof ShopRoom) {
            ChallengeVariables.enteredShopThisVisit = true;
            ChallengeVariables.spentGoldInShopThisVisit = false;
        }
        else {
            if (ChallengeVariables.enteredShopThisVisit) {
                ChallengeVariables.resetShopVisitTracking();
            }
        }
    }
}