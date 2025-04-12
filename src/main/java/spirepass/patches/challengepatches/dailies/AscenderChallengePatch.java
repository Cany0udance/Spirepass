package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(
        clz = AbstractDungeon.class,
        method = "nextRoomTransition",
        paramtypez = {
                SaveFile.class
        }
)
public class AscenderChallengePatch {

    @SpirePostfixPatch
    public static void trackFloorAscension(AbstractDungeon __instance, SaveFile saveFile) {
        if (!CardCrawlGame.loadingSave) {
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_ascender")) {
                ChallengeHelper.updateChallengeProgress("daily_ascender", 1);
            }
        }
    }
}