package spirepass.patches.challengepatches.dailies;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.screens.options.Slider;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.ChallengeHelper;
import com.megacrit.cardcrawl.core.Settings;

import java.lang.reflect.Field;

@SpirePatch(clz = Slider.class, method = "modifyVolume")
public class SilentChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);
    private static boolean hasBeenSilent = false;
    private static Field typeField;
    private static Field volumeField;

    // Initialize reflection fields
    static {
        try {
            typeField = Slider.class.getDeclaredField("type");
            typeField.setAccessible(true);

            volumeField = Slider.class.getDeclaredField("volume");
            volumeField.setAccessible(true);
        } catch (Exception e) {
            logger.error("Failed to initialize reflection fields for SilentChallengePatch", e);
        }
    }

    @SpireInsertPatch(
            locator = MasterVolumeLocator.class
    )
    public static void onVolumeChange(Slider __instance) {
        try {
            // Get slider type and volume using reflection
            Slider.SliderType sliderType = (Slider.SliderType) typeField.get(__instance);
            float sliderVolume = volumeField.getFloat(__instance);

            // Check if this is the master volume slider and it's set to 0
            if (sliderType == Slider.SliderType.MASTER && sliderVolume == 0.0f) {
                logger.info("Master volume set to 0 - experiencing silence");

                // Prevent multiple triggers if player sets volume to 0 repeatedly
                if (!hasBeenSilent) {
                    hasBeenSilent = true;

                    // Check if the Silent challenge is active and incomplete
                    if (ChallengeHelper.isActiveChallengeIncomplete("daily_silent")) {
                        // Complete the challenge
                        ChallengeHelper.completeChallenge("daily_silent");
                        logger.info("Daily The Silent challenge completed! Player has experienced silence.");
                    }
                }
            } else if (sliderType == Slider.SliderType.MASTER && sliderVolume > 0.0f) {
                // Reset the flag if volume is raised back up
                hasBeenSilent = false;
            }
        } catch (Exception e) {
            logger.error("Error in SilentChallengePatch.onVolumeChange", e);
        }
    }

    // Locator to find where master volume is set
    private static class MasterVolumeLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.FieldAccessMatcher(Settings.class, "MASTER_VOLUME");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}