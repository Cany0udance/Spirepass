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
import org.apache.logging.log4j.Logger;
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

public class MainMenuChallengePatch {
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);
    private static ModLabeledButton dailyButton;
    private static ModLabeledButton weeklyButton;
    private static ModLabeledButton devRefreshButton; // New refresh button
    private static ModLabeledButton adButton; // New ad button
    private static boolean initialized = false;
    // Store button positions since we can't access them directly
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

    // List of prank video URLs
    private static final List<String> PRANK_VIDEOS = Arrays.asList(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/watch?v=JRHARtLZLk8",
            "https://www.youtube.com/watch?v=ttVtllHkb4E",
            "https://www.youtube.com/watch?v=krDFltgjLtE",
            "https://www.youtube.com/watch?v=HbdygECc6MU"
    );

    @SpirePatch(clz = MainMenuScreen.class, method = "render")
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void renderButtons(MainMenuScreen instance, SpriteBatch sb) {
            if (CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU) {
                // Force original drawing color
                sb.setColor(Color.WHITE);

                // Render regular buttons
                if (weeklyButton != null) {
                    weeklyButton.render(sb);
                }
                if (dailyButton != null) {
                    // Reset batch state before rendering
                    sb.setColor(Color.WHITE);
                    dailyButton.render(sb);
                }
                if (devRefreshButton != null) {
                    sb.setColor(Color.WHITE);
                    devRefreshButton.render(sb);
                }

                // Only render ad button if it's visible
                if (adButton != null && isAdButtonVisible) {
                    sb.setColor(Color.WHITE);
                    adButton.render(sb);
                }
            }
        }
    }


    @SpirePatch(clz = MainMenuScreen.class, method = "update")
    public static class UpdatePatch {
        @SpirePostfixPatch
        public static void updateButtons(MainMenuScreen instance) {
            if (CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU) {
                // Initialize buttons if not already done
                if (!initialized) {
                    initializeButtons();
                    initialized = true;
                }
                // Update button text to reflect time changes
                if (dailyButton != null) {
                    dailyButton.label = getDailyButtonText();
                }
                if (weeklyButton != null) {
                    weeklyButton.label = getWeeklyButtonText();
                }
                // Update button colors based on completion status
                updateButtonColors();

                // Check if ad button should be shown or hidden
                updateAdButtonVisibility();

                // Update the buttons
                if (dailyButton != null) {
                    dailyButton.update();
                }
                if (weeklyButton != null) {
                    weeklyButton.update();
                }
                if (devRefreshButton != null) {
                    devRefreshButton.update();
                }
                // Only update ad button if it's visible
                if (adButton != null && isAdButtonVisible) {
                    adButton.update();
                }
                // Check for hovering to show tooltips
                checkAndRenderTooltips();
            }
        }
    }



    /**
     * Initialize buttons one time
     */
    private static void initializeButtons() {
        // Position values - make sure they're clearly separated
        float buttonY = Settings.HEIGHT * 0.8f;
        float buttonX = Settings.WIDTH * 0.2f;
        float buttonSpacing = 120.0f * Settings.scale;


        // Store positions for tooltip rendering
        dailyButtonX = buttonX;
        dailyButtonY = buttonY + buttonSpacing;
        weeklyButtonX = buttonX;
        weeklyButtonY = buttonY;
        adButtonX = buttonX;
        adButtonY = buttonY - buttonSpacing; // Position where dev button was
        devButtonX = Settings.WIDTH * 0.8f; // Move dev button far to the right
        devButtonY = buttonY - buttonSpacing;


        buttonWidth = 300.0f * Settings.scale; // Estimated width
        buttonHeight = 40.0f * Settings.scale; // Estimated height


        // Create daily button with explicit parameters
        dailyButton = new ModLabeledButton(
                getDailyButtonText(),
                dailyButtonX,
                dailyButtonY,
                Settings.CREAM_COLOR,
                Color.GREEN,
                FontHelper.buttonLabelFont,
                null,
                (button) -> {
                    // Button click action - could add functionality later
                    CardCrawlGame.sound.play("UI_CLICK_1");
                }
        );


        weeklyButton = new ModLabeledButton(
                getWeeklyButtonText(),
                weeklyButtonX,
                weeklyButtonY,
                Settings.CREAM_COLOR,
                Color.GREEN,
                FontHelper.buttonLabelFont,
                null,
                (button) -> {
                    // Button click action - could add functionality later
                    CardCrawlGame.sound.play("UI_CLICK_1");
                }
        );


        // Create dev refresh button
        devRefreshButton = new ModLabeledButton(
                "DEV: Refresh Challenges",
                devButtonX,
                devButtonY,
                Color.ORANGE, // Different color to make it distinct
                Color.RED,
                FontHelper.buttonLabelFont,
                null,
                (button) -> {
                    CardCrawlGame.sound.play("UI_CLICK_2"); // Different sound for feedback
                    refreshAllChallenges();
                   // completeAllChallenges(); //
                }
        );

        // Create ad button (initially hidden)
        adButton = new ModLabeledButton(
                "Watch Ad To Refresh Challenges",
                adButtonX,
                adButtonY,
                Color.GOLD, // Bright gold color to make it stand out
                Color.YELLOW,
                FontHelper.buttonLabelFont,
                null,
                (button) -> {
                    CardCrawlGame.sound.play("UI_CLICK_2");
                    openRandomPrankVideo();
                }
        );


        // Store width and height from hitboxes if possible
        try {
            // Using reflection to access the hitbox
            Field hbField = ModLabeledButton.class.getDeclaredField("hb");
            hbField.setAccessible(true);
            Hitbox dailyHb = (Hitbox)hbField.get(dailyButton);
            Hitbox weeklyHb = (Hitbox)hbField.get(weeklyButton);
            // Store width and height
            buttonWidth = dailyHb.width;
            buttonHeight = dailyHb.height;
        } catch (Exception e) {
            logger.error("Failed to access ModLabeledButton fields: " + e.getMessage());
        }
    }

    /**
     * Mark all active challenges as complete for testing
     */
    private static void completeAllChallenges() {
        ChallengeManager manager = ChallengeManager.getInstance();

        // Complete all daily challenges
        for (Challenge challenge : manager.getDailyChallenges()) {
            ChallengeHelper.completeChallenge(challenge.getId());
        }

        // Complete all weekly challenges
        for (Challenge challenge : manager.getWeeklyChallenges()) {
            ChallengeHelper.completeChallenge(challenge.getId());
        }

        // Log completion
        logger.info("DEV: Marked all challenges as complete");
    }

    /**
     * Check if buttons are being hovered and display tooltips
     */
    private static void checkAndRenderTooltips() {
        try {
            // Using reflection to access the hitbox
            Field hbField = ModLabeledButton.class.getDeclaredField("hb");
            hbField.setAccessible(true);

            if (dailyButton != null) {
                Hitbox dailyHb = (Hitbox)hbField.get(dailyButton);
                if (dailyHb.hovered) {
                    renderDailyChallengeTooltip();
                }
            }

            if (weeklyButton != null) {
                Hitbox weeklyHb = (Hitbox)hbField.get(weeklyButton);
                if (weeklyHb.hovered) {
                    renderWeeklyChallengeTooltip();
                }
            }

            // Only check ad button tooltip if it's visible
            if (adButton != null && isAdButtonVisible) {
                Hitbox adHb = (Hitbox)hbField.get(adButton);
                if (adHb.hovered) {
                    TipHelper.renderGenericTip(
                            adButtonX + buttonWidth + 20.0f * Settings.scale,
                            adButtonY,
                            "",
                            "Watch an advertisement to instantly refresh ALL challenges!! Yay capitalism."
                    );
                }
            }

            if (devRefreshButton != null) {
                Hitbox devHb = (Hitbox)hbField.get(devRefreshButton);
                // Could add tooltip for dev button here if needed
            }
        } catch (Exception e) {
            logger.error("Failed to access ModLabeledButton hitbox: " + e.getMessage());
        }
    }


    /**
     * Force refresh all challenges for development purposes
     */
    private static void refreshAllChallenges() {
        logger.info("DEV: Manually refreshing all challenges - BEFORE clear, completed count: " +
                ChallengeManager.getInstance().completedChallenges.size());


        // Clear all completion statuses first
        ChallengeManager.getInstance().clearAllCompletionStatus();


        logger.info("DEV: AFTER clear, completed count: " +
                ChallengeManager.getInstance().completedChallenges.size());


        // Then generate new daily and weekly challenges
        Spirepass.generateDailyChallenges();
        Spirepass.generateWeeklyChallenges();


        logger.info("DEV: AFTER generating challenges, completed count: " +
                ChallengeManager.getInstance().completedChallenges.size());


        // Play a sound to indicate success
        CardCrawlGame.sound.play("POWER_INTANGIBLE");
    }

    /**
     * Open a random prank video in the user's browser and refresh challenges
     */
    private static void openRandomPrankVideo() {
        try {
            // Select a random URL from the list
            Random random = new Random();
            String url = PRANK_VIDEOS.get(random.nextInt(PRANK_VIDEOS.size()));

            // Open the URL in the default browser
            logger.info("Opening prank video: " + url);
            Desktop.getDesktop().browse(new URI(url));

            // Play a fun sound
            CardCrawlGame.sound.play("DEATH_STARE");

            // Also refresh the challenges
            refreshAllChallenges();
        } catch (Exception e) {
            logger.error("Failed to open prank video: " + e.getMessage());
        }
    }

    /**
     * Update the visibility of the ad button based on challenge completion
     * DEVELOPMENT MODE: Button is always visible for testing
     */
    private static void updateAdButtonVisibility() {
        if (adButton == null) return;

        // DEVELOPMENT MODE: Always show the button
       // boolean showButton = true;

        // PRODUCTION MODE: Uncomment the line below to only show when all challenges are completed
         boolean showButton = areAllChallengesCompleted();

        // Store visibility state in the static field
        isAdButtonVisible = showButton;

        // Update the button text color based on visibility
        try {
            Field textColorField = ModLabeledButton.class.getDeclaredField("color");
            textColorField.setAccessible(true);
            textColorField.set(adButton, showButton ? Color.GOLD : Color.GRAY);
        } catch (Exception e) {
            logger.error("Failed to update ad button color: " + e.getMessage());
        }
    }

    /**
     * Check if all challenges (daily and weekly) are completed
     */
    private static boolean areAllChallengesCompleted() {
        ChallengeManager manager = ChallengeManager.getInstance();

        // Check daily challenges
        List<Challenge> dailyChallenges = manager.getDailyChallenges();
        if (dailyChallenges.isEmpty()) {
            return false; // If no daily challenges, return false
        }

        for (Challenge challenge : dailyChallenges) {
            if (!manager.isCompleted(challenge.getId())) {
                return false;
            }
        }

        // Check weekly challenges
        List<Challenge> weeklyChallenges = manager.getWeeklyChallenges();
        if (weeklyChallenges.isEmpty()) {
            return false; // If no weekly challenges, return false
        }

        for (Challenge challenge : weeklyChallenges) {
            if (!manager.isCompleted(challenge.getId())) {
                return false;
            }
        }

        // All challenges are completed
        return true;
    }



    /**
     * Update the color of buttons based on challenge completion status
     */
    private static void updateButtonColors() {
        ChallengeManager manager = ChallengeManager.getInstance();


        // Check if all daily challenges are completed
        boolean allDailyCompleted = !manager.getDailyChallenges().isEmpty();
        for (Challenge challenge : manager.getDailyChallenges()) {
            if (!manager.isCompleted(challenge.getId())) {
                allDailyCompleted = false;
                break;
            }
        }


        // Check if all weekly challenges are completed
        boolean allWeeklyCompleted = !manager.getWeeklyChallenges().isEmpty();
        for (Challenge challenge : manager.getWeeklyChallenges()) {
            if (!manager.isCompleted(challenge.getId())) {
                allWeeklyCompleted = false;
                break;
            }
        }


        // Set colors
        if (dailyButton != null) {
            try {
                // Use reflection to access text color field
                Field textColorField = ModLabeledButton.class.getDeclaredField("color");
                textColorField.setAccessible(true);
                textColorField.set(dailyButton, allDailyCompleted ? Color.GREEN : Settings.CREAM_COLOR);
            } catch (Exception e) {
                logger.error("Failed to set button text color: " + e.getMessage());
            }
        }


        if (weeklyButton != null) {
            try {
                // Use reflection to access text color field
                Field textColorField = ModLabeledButton.class.getDeclaredField("color");
                textColorField.setAccessible(true);
                textColorField.set(weeklyButton, allWeeklyCompleted ? Color.GREEN : Settings.CREAM_COLOR);
            } catch (Exception e) {
                logger.error("Failed to set button text color: " + e.getMessage());
            }
        }
    }


    /**
     * Generate text for daily button including time until reset
     */
    private static String getDailyButtonText() {
        return "Daily Challenges (" + getTimeUntilDailyReset() + ")";
    }


    /**
     * Generate text for weekly button including time until reset
     */
    private static String getWeeklyButtonText() {
        return "Weekly Challenges (" + getTimeUntilWeeklyReset() + ")";
    }

    /**
     * Render the buttons based on their visibility state
     * This method is called from the main render method
     */
    private static void renderVisibleButtons(SpriteBatch sb) {
        // Force original drawing color
        sb.setColor(Color.WHITE);

        // Render regular buttons
        if (weeklyButton != null) {
            weeklyButton.render(sb);
        }
        if (dailyButton != null) {
            sb.setColor(Color.WHITE);
            dailyButton.render(sb);
        }
        if (devRefreshButton != null) {
            sb.setColor(Color.WHITE);
            devRefreshButton.render(sb);
        }

        // Only render ad button if it's supposed to be visible
        if (adButton != null && isAdButtonVisible) {
            sb.setColor(Color.WHITE);
            adButton.render(sb);
        }
    }


    /**
     * Render tooltip with daily challenge information
     */
    private static void renderDailyChallengeTooltip() {
        ChallengeManager manager = ChallengeManager.getInstance();
        List<Challenge> dailyChallenges = manager.getDailyChallenges();


        // Build tooltip text
        StringBuilder tipText = new StringBuilder();


        // Remove header as requested, button already shows this information


        if (dailyChallenges.isEmpty()) {
            tipText.append("No active daily challenges.");
        } else {
            for (int i = 0; i < dailyChallenges.size(); i++) {
                Challenge challenge = dailyChallenges.get(i);
                String completion = manager.isCompleted(challenge.getId()) ? " #g(Complete)" : "";
                tipText.append("- ").append(challenge.getName()).append(": ")
                        .append(challenge.getDescription())
                        .append(completion).append(" NL ");


                // Display different progress text based on completion status
                if (challenge.isCompleted() || manager.isCompleted(challenge.getId())) {
                    // For completed challenges, show "Done!" in green
                    tipText.append("  Progress: #gDone!");
                } else {
                    // For incomplete challenges, show numeric progress in blue
                    int current = challenge.getCurrentProgress();
                    int max = challenge.getMaxProgress();
                    tipText.append("  Progress: #b").append(current).append("/").append(max);
                }


                // Only add double line break if not the last challenge
                // This prevents the extra gap at the end
                if (i < dailyChallenges.size() - 1) {
                    tipText.append(" NL NL ");
                }
            }
        }


        // Render tooltip near button position but with empty title
        TipHelper.renderGenericTip(
                dailyButtonX + buttonWidth + 20.0f * Settings.scale,
                dailyButtonY,
                "", // Empty title as requested
                tipText.toString()
        );
    }


    /**
     * Render tooltip with weekly challenge information
     */
    private static void renderWeeklyChallengeTooltip() {
        ChallengeManager manager = ChallengeManager.getInstance();
        List<Challenge> weeklyChallenges = manager.getWeeklyChallenges();


        // Build tooltip text
        StringBuilder tipText = new StringBuilder();


        // Remove header as requested, button already shows this information


        if (weeklyChallenges.isEmpty()) {
            tipText.append("No active weekly challenges.");
        } else {
            for (int i = 0; i < weeklyChallenges.size(); i++) {
                Challenge challenge = weeklyChallenges.get(i);
                String completion = manager.isCompleted(challenge.getId()) ? " #g(Complete)" : "";
                tipText.append("- ").append(challenge.getName()).append(": ")
                        .append(challenge.getDescription())
                        .append(completion).append(" NL ");


                // Display different progress text based on completion status
                if (challenge.isCompleted() || manager.isCompleted(challenge.getId())) {
                    // For completed challenges, show "Done!" in green
                    tipText.append("  Progress: #gDone!");
                } else {
                    // For incomplete challenges, show numeric progress in blue
                    int current = challenge.getCurrentProgress();
                    int max = challenge.getMaxProgress();
                    tipText.append("  Progress: #b").append(current).append("/").append(max);
                }


                // Only add double line break if not the last challenge
                // This prevents the extra gap at the end
                if (i < weeklyChallenges.size() - 1) {
                    tipText.append(" NL NL ");
                }
            }
        }


        // Render tooltip near button position but with empty title
        TipHelper.renderGenericTip(
                weeklyButtonX + buttonWidth + 20.0f * Settings.scale,
                weeklyButtonY,
                "", // Empty title as requested
                tipText.toString()
        );
    }


    /**
     * Calculate time until daily challenge reset
     */
    private static String getTimeUntilDailyReset() {
        // Get local time and set it to next reset time
        Calendar now = Calendar.getInstance();
        Calendar resetTime = (Calendar) now.clone();
        resetTime.set(Calendar.HOUR_OF_DAY, Spirepass.REFRESH_HOUR_LOCAL);
        resetTime.set(Calendar.MINUTE, Spirepass.REFRESH_MINUTE_LOCAL);
        resetTime.set(Calendar.SECOND, 0);
        resetTime.set(Calendar.MILLISECOND, 0);


        // If it's already past reset time, use tomorrow
        if (now.after(resetTime)) {
            resetTime.add(Calendar.DAY_OF_YEAR, 1);
        }


        // Calculate duration in milliseconds
        long diffMs = resetTime.getTimeInMillis() - now.getTimeInMillis();
        return formatDurationFromMillis(diffMs);
    }


    /**
     * Calculate time until weekly challenge reset
     */
    private static String getTimeUntilWeeklyReset() {
        // Get local time and figure out next Monday
        Calendar now = Calendar.getInstance();
        Calendar nextMonday = (Calendar) now.clone();


        nextMonday.set(Calendar.HOUR_OF_DAY, Spirepass.REFRESH_HOUR_LOCAL);
        nextMonday.set(Calendar.MINUTE, Spirepass.REFRESH_MINUTE_LOCAL);
        nextMonday.set(Calendar.SECOND, 0);
        nextMonday.set(Calendar.MILLISECOND, 0);


        // If not Monday, go to next Monday
        if (nextMonday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            nextMonday.add(Calendar.DAY_OF_WEEK,
                    (Calendar.MONDAY + 7 - nextMonday.get(Calendar.DAY_OF_WEEK)) % 7);
        }
        // If it's Monday but after reset time, go to next Monday
        else if (now.after(nextMonday)) {
            nextMonday.add(Calendar.DAY_OF_YEAR, 7);
        }


        // Calculate duration in milliseconds
        long diffMs = nextMonday.getTimeInMillis() - now.getTimeInMillis();
        return formatDurationFromMillis(diffMs);
    }


    /**
     * Format duration from milliseconds as hours:minutes:seconds
     */
    private static String formatDurationFromMillis(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (millis % (1000 * 60)) / 1000;


        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    /**
     * Format duration as hours:minutes:seconds
     */
    private static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();


        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}