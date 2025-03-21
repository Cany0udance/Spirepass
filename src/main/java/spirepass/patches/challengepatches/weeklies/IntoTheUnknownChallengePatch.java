package spirepass.patches.challengepatches.weeklies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.EventHelper;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;

@SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = {SaveFile.class})
public class IntoTheUnknownChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void trackUnknownRoomVisit(AbstractDungeon __instance, SaveFile saveFile) {
        // This insert runs just after the room is designated as an unknown (?) node
        // Check if the weekly unknown challenge is active and incomplete
        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_unknown")) {
            // Update the progress for the Into the Unknown challenge
            ChallengeHelper.updateChallengeProgress("weekly_unknown", 1);
            logger.info("Weekly Into the Unknown challenge progress incremented! Visited unknown node.");
        }
    }

    // Locator to find the insertion point after the "?" symbol is set
    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(
                    "com.megacrit.cardcrawl.rooms.AbstractRoom", "setMapSymbol");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}