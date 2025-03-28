package spirepass.patches;

import basemod.ModLabeledButton;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;
import spirepass.challengeutil.Challenge;
import spirepass.challengeutil.ChallengeHelper;
import spirepass.challengeutil.ChallengeManager;

import java.awt.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainMenuChallengePatch {
    // private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    private static ModLabeledButton dailyButton;
    private static ModLabeledButton weeklyButton;
    // private static ModLabeledButton devRefreshButton;
    private static ModLabeledButton adButton;
    private static boolean initialized = false;

    // Button position and dimension cache
    private static float dailyButtonX;
    private static float dailyButtonY;
    private static float weeklyButtonX;
    private static float weeklyButtonY;
    private static float devButtonX;
    private static float devButtonY;
    private static float adButtonX;
    private static float adButtonY;
    private static float buttonWidth;
    private static float buttonHeight;
    private static boolean isAdButtonVisible = true;

    // Prank video URLs
    private static final List<String> PRANK_VIDEOS = Arrays.asList(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/watch?v=JRHARtLZLk8",
            "https://www.youtube.com/watch?v=ttVtllHkb4E",
            "https://www.youtube.com/watch?v=krDFltgjLtE",
            "https://www.youtube.com/watch?v=HbdygECc6MU"
    );

    // --- Patches ---

    @SpirePatch(clz = MainMenuScreen.class, method = "render")
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void renderButtons(MainMenuScreen instance, SpriteBatch sb) {
            // Add this check
            if (!Spirepass.enableMainMenuElements) {
                return;
            }
            // Original code follows
            if (CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU) {
                sb.setColor(Color.WHITE);
                if (weeklyButton != null) weeklyButton.render(sb);
                if (dailyButton != null) dailyButton.render(sb);
                // if (devRefreshButton != null) devRefreshButton.render(sb);
                if (adButton != null && isAdButtonVisible) adButton.render(sb);
                sb.setColor(Color.WHITE);
            }
        }
    }

    @SpirePatch(clz = MainMenuScreen.class, method = "update")
    public static class UpdatePatch {
        @SpirePostfixPatch
        public static void updateButtons(MainMenuScreen instance) {
            // Add this check
            if (!Spirepass.enableMainMenuElements) {
                // Reset initialized flag if elements are disabled, so they re-init if re-enabled
                if (initialized) {
                    dailyButton = null;
                    weeklyButton = null;
                    adButton = null;
                    initialized = false;
                }
                return;
            }
            // Original code follows
            if (CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU) {
                if (!initialized) {
                    initializeButtons();
                    initialized = true;
                }

                if (dailyButton != null) dailyButton.label = getDailyButtonText();
                if (weeklyButton != null) weeklyButton.label = getWeeklyButtonText();

                updateButtonColors();
                updateAdButtonVisibility();

                if (dailyButton != null) dailyButton.update();
                if (weeklyButton != null) weeklyButton.update();
                // if (devRefreshButton != null) devRefreshButton.update();
                if (adButton != null && isAdButtonVisible) adButton.update();

                checkAndRenderTooltips();
            }
        }
    }

    // --- Initialization ---

    private static void initializeButtons() {
        float buttonY = Settings.HEIGHT * 0.8f;
        float buttonX = Settings.WIDTH * 0.2f;
        float buttonSpacing = 120.0f * Settings.scale;

        dailyButtonX = buttonX; dailyButtonY = buttonY + buttonSpacing;
        weeklyButtonX = buttonX; weeklyButtonY = buttonY;
        adButtonX = buttonX; adButtonY = buttonY - buttonSpacing;
        devButtonX = Settings.WIDTH * 0.8f; devButtonY = buttonY - buttonSpacing;

        buttonWidth = 300.0f * Settings.scale; buttonHeight = 40.0f * Settings.scale;

        dailyButton = new ModLabeledButton(getDailyButtonText(), dailyButtonX, dailyButtonY, Settings.CREAM_COLOR, Color.GREEN, FontHelper.buttonLabelFont, null, (button) -> CardCrawlGame.sound.play("UI_CLICK_1"));
        weeklyButton = new ModLabeledButton(getWeeklyButtonText(), weeklyButtonX, weeklyButtonY, Settings.CREAM_COLOR, Color.GREEN, FontHelper.buttonLabelFont, null, (button) -> CardCrawlGame.sound.play("UI_CLICK_1"));
    //    devRefreshButton = new ModLabeledButton("DEV: Refresh Challenges", devButtonX, devButtonY, Color.ORANGE, Color.RED, FontHelper.buttonLabelFont, null, (button) -> { CardCrawlGame.sound.play("UI_CLICK_2"); refreshAllChallenges(); });
        adButton = new ModLabeledButton("Watch Ad To Refresh Challenges", adButtonX, adButtonY, Color.GOLD, Color.YELLOW, FontHelper.buttonLabelFont, null, (button) -> { CardCrawlGame.sound.play("UI_CLICK_2"); openRandomPrankVideo(); });

        try {
            Field hbField = ModLabeledButton.class.getDeclaredField("hb");
            hbField.setAccessible(true);
            Hitbox hb = (Hitbox) hbField.get(dailyButton);
            if (hb != null) { buttonWidth = hb.width; buttonHeight = hb.height; }
        } catch (Exception e) { /* Reflection failed, use defaults */ }
    }

    // --- Tooltip Logic ---

    private static void checkAndRenderTooltips() {
        try {
            // Reflection to access hitbox for hover checks
            Field hbField = ModLabeledButton.class.getDeclaredField("hb");
            hbField.setAccessible(true);

            // Calculate standard tooltip position offset
            float tooltipOffsetX = buttonWidth + 20.0f * Settings.scale;
            float tooltipOffsetY = buttonHeight / 2f; // Vertically center tooltip relative to button

            // Render tooltip for Daily button if hovered
            if (dailyButton != null && ((Hitbox) hbField.get(dailyButton)).hovered) {
                renderDailyChallengeTooltip(dailyButtonX + tooltipOffsetX, dailyButtonY + tooltipOffsetY);
            }

            // Render tooltip for Weekly button if hovered
            if (weeklyButton != null && ((Hitbox) hbField.get(weeklyButton)).hovered) {
                renderWeeklyChallengeTooltip(weeklyButtonX + tooltipOffsetX, weeklyButtonY + tooltipOffsetY);
            }

            // Tooltip for Ad button has been removed.

            // Optional: Tooltip for Dev button (remains commented out unless needed)
            // if (devRefreshButton != null && ((Hitbox) hbField.get(devRefreshButton)).hovered) {
            //     // Render tooltip to the left of the dev button due to its position
            //     TipHelper.renderGenericTip(devButtonX - 350f * Settings.scale, devButtonY + tooltipOffsetY, "Developer Tool", "Instantly refreshes Daily and Weekly challenges.");
            // }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            // logger.error("Reflection failed for Hitbox in checkAndRenderTooltips: " + e.getMessage());
            // Tooltips might not appear if reflection fails.
        } catch (Exception e) { // Catch any other unexpected errors
            // logger.error("Unexpected error in checkAndRenderTooltips: " + e.getMessage());
        }
    }

    /** Adds a color prefix to each word in a string. */
    private static String colorEveryWord(String text, String colorPrefix) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String[] words = text.split(" ");
        StringBuilder coloredText = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            // Append the prefix only if the word is not empty
            if (!words[i].isEmpty()) {
                coloredText.append(colorPrefix).append(words[i]);
                // Add a space after the word if it's not the last word
                if (i < words.length - 1) {
                    coloredText.append(" ");
                }
            } else if (i < words.length - 1) {
                // Preserve multiple spaces if present
                coloredText.append(" ");
            }
        }
        return coloredText.toString();
    }

    /** Renders the tooltip for Daily Challenges. */
    private static void renderDailyChallengeTooltip(float tipX, float tipY) {
        ChallengeManager manager = ChallengeManager.getInstance();
        List<Challenge> dailyChallenges = manager.getDailyChallenges();
        StringBuilder tipBody = new StringBuilder();

        if (dailyChallenges.isEmpty()) {
            tipBody.append("No active daily challenges.");
        } else {
            for (int i = 0; i < dailyChallenges.size(); i++) {
                Challenge challenge = dailyChallenges.get(i);
                boolean completed = manager.isCompleted(challenge.getId());
                String completionMarker = completed ? " #g(Complete)" : "";
                String coloredName = colorEveryWord(challenge.getName(), "#y");

                tipBody.append("- ")
                        .append(coloredName)
                        .append(": ")
                        .append(challenge.getDescription())
                        .append(completionMarker)
                        .append(" NL ");

                if (completed) {
                    tipBody.append("  Progress: #gDone!");
                } else {
                    if ("daily_unbeatable".equals(challenge.getId())) {
                        tipBody.append("  Progress: #b").append(challenge.getCurrentProgress()).append("/LOL");
                    } else {
                        tipBody.append("  Progress: #b").append(challenge.getCurrentProgress())
                                .append("/").append(challenge.getMaxProgress());
                    }
                }

                if (i < dailyChallenges.size() - 1) {
                    tipBody.append(" NL NL ");
                }
            }
        }

        TipHelper.renderGenericTip(tipX, tipY,
                "Daily challenges grant 25 Spirepass XP when completed.",
                tipBody.toString()
        );
    }

    /** Renders the tooltip for Weekly Challenges. */
    private static void renderWeeklyChallengeTooltip(float tipX, float tipY) {
        ChallengeManager manager = ChallengeManager.getInstance();
        List<Challenge> weeklyChallenges = manager.getWeeklyChallenges();
        StringBuilder tipBody = new StringBuilder();

        if (weeklyChallenges.isEmpty()) {
            tipBody.append("No active weekly challenges.");
        } else {
            for (int i = 0; i < weeklyChallenges.size(); i++) {
                Challenge challenge = weeklyChallenges.get(i);
                boolean completed = manager.isCompleted(challenge.getId());
                String completionMarker = completed ? " #g(Complete)" : "";

                // Color every word of the challenge name yellow
                String coloredName = colorEveryWord(challenge.getName(), "#y");

                // Append the fully colored name, then colon, description, etc.
                tipBody.append("- ")
                        .append(coloredName) // Use the helper method result
                        .append(": ") // Append colon and space (should be default color)
                        .append(challenge.getDescription()) // Description (should be default color)
                        .append(completionMarker)
                        .append(" NL ");

                // Progress line
                if (completed) {
                    tipBody.append("  Progress: #gDone!");
                } else {
                    tipBody.append("  Progress: #b").append(challenge.getCurrentProgress())
                            .append("/").append(challenge.getMaxProgress());
                }

                if (i < weeklyChallenges.size() - 1) {
                    tipBody.append(" NL NL ");
                }
            }
        }

        TipHelper.renderGenericTip(tipX, tipY,
                "Weekly challenges grant 75 Spirepass XP when completed.", // Header
                tipBody.toString()
        );
    }

    // --- Button State Updates ---

    /** Updates button text color based on completion status. */
    private static void updateButtonColors() {
        ChallengeManager manager = ChallengeManager.getInstance();
        boolean allDailyCompleted = !manager.getDailyChallenges().isEmpty() && manager.getDailyChallenges().stream().allMatch(c -> manager.isCompleted(c.getId()));
        boolean allWeeklyCompleted = !manager.getWeeklyChallenges().isEmpty() && manager.getWeeklyChallenges().stream().allMatch(c -> manager.isCompleted(c.getId()));

        try {
            Field textColorField = ModLabeledButton.class.getDeclaredField("color");
            textColorField.setAccessible(true);
            if (dailyButton != null) textColorField.set(dailyButton, allDailyCompleted ? Color.GREEN : Settings.CREAM_COLOR);
            if (weeklyButton != null) textColorField.set(weeklyButton, allWeeklyCompleted ? Color.GREEN : Settings.CREAM_COLOR);
        } catch (Exception e) { /* Reflection failed */ }
    }

    /** Updates visibility and color of the 'Watch Ad' button. */
    private static void updateAdButtonVisibility() {
        if (adButton == null) return;
         boolean shouldShowButton = areAllChallengesCompleted();
        // DEV OVERRIDE:
        // boolean shouldShowButton = true;
        isAdButtonVisible = shouldShowButton;

        try {
            Field textColorField = ModLabeledButton.class.getDeclaredField("color");
            textColorField.setAccessible(true);
            textColorField.set(adButton, shouldShowButton ? Color.GOLD : Color.DARK_GRAY);
        } catch (Exception e) { /* Reflection failed */ }
    }

    /** Checks if all daily AND weekly challenges are completed. */
    private static boolean areAllChallengesCompleted() {
        ChallengeManager manager = ChallengeManager.getInstance();
        List<Challenge> daily = manager.getDailyChallenges();
        List<Challenge> weekly = manager.getWeeklyChallenges();
        if (daily.isEmpty() || weekly.isEmpty()) return false;
        return daily.stream().allMatch(c -> manager.isCompleted(c.getId())) &&
                weekly.stream().allMatch(c -> manager.isCompleted(c.getId()));
    }

    // --- Actions & Utilities ---

    private static void refreshAllChallenges() {
        Spirepass.generateDailyChallenges();
        Spirepass.generateWeeklyChallenges();
        updateButtonColors();
        updateAdButtonVisibility();
    }

    private static void openRandomPrankVideo() {
        try {
            Random random = new Random();
            String url = PRANK_VIDEOS.get(random.nextInt(PRANK_VIDEOS.size()));
            Desktop.getDesktop().browse(new URI(url));
            CardCrawlGame.sound.play("DEATH_STARE");
            refreshAllChallenges();
        } catch (Exception e) { /* Failed to open URL or refresh */ }
    }

    private static String getDailyButtonText() { return "Daily Challenges (" + getTimeUntilDailyReset() + ")"; }
    private static String getWeeklyButtonText() { return "Weekly Challenges (" + getTimeUntilWeeklyReset() + ")"; }

    // --- Time Formatting ---

    private static String getTimeUntilDailyReset() {
        Calendar now = Calendar.getInstance();
        Calendar resetTime = (Calendar) now.clone();
        resetTime.set(Calendar.HOUR_OF_DAY, Spirepass.REFRESH_HOUR_LOCAL);
        resetTime.set(Calendar.MINUTE, Spirepass.REFRESH_MINUTE_LOCAL);
        resetTime.set(Calendar.SECOND, 0); resetTime.set(Calendar.MILLISECOND, 0);
        if (now.after(resetTime)) resetTime.add(Calendar.DAY_OF_YEAR, 1);
        return formatDurationFromMillis(resetTime.getTimeInMillis() - now.getTimeInMillis());
    }

    private static String getTimeUntilWeeklyReset() {
        Calendar now = Calendar.getInstance();
        Calendar nextMondayReset = (Calendar) now.clone();
        nextMondayReset.set(Calendar.HOUR_OF_DAY, Spirepass.REFRESH_HOUR_LOCAL);
        nextMondayReset.set(Calendar.MINUTE, Spirepass.REFRESH_MINUTE_LOCAL);
        nextMondayReset.set(Calendar.SECOND, 0); nextMondayReset.set(Calendar.MILLISECOND, 0);
        int currentDayOfWeek = nextMondayReset.get(Calendar.DAY_OF_WEEK);
        int daysUntilMonday = (Calendar.MONDAY - currentDayOfWeek + 7) % 7;
        if (daysUntilMonday == 0 && now.after(nextMondayReset)) daysUntilMonday = 7;
        if(daysUntilMonday > 0) nextMondayReset.add(Calendar.DAY_OF_YEAR, daysUntilMonday);
        return formatDurationFromMillis(nextMondayReset.getTimeInMillis() - now.getTimeInMillis());
    }

    private static String formatDurationFromMillis(long millis) {
        if (millis < 0) millis = 0;
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}