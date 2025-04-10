package spirepass.patches.challengepatches.dailies;

import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = AbstractRoom.class, method = "endBattle")
public class NotEvenCloseChallengePatch {
    public static void Postfix(AbstractRoom __instance) {
        if (AbstractDungeon.player != null && AbstractDungeon.player.currentHealth <= 10) {
            if (ChallengeHelper.isActiveChallengeIncomplete("daily_notevenclose")) {
                ChallengeHelper.completeChallenge("daily_notevenclose");
            }
        }
    }
}