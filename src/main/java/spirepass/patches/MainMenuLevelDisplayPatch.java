package spirepass.patches;

import basemod.ModPanel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;

import java.lang.reflect.Field;

public class MainMenuLevelDisplayPatch {
//     private static final Logger logger = LogManager.getLogger(Spirepass.modID);
    private static BitmapFont levelFont;
    private static ModPanel levelPanel;
    private static boolean initialized = false;
    // Position for the level display
    private static float levelX;
    private static float levelY;
    private static float panelWidth;
    private static float panelHeight;

    @SpirePatch(clz = MainMenuScreen.class, method = "render")
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void renderLevelDisplay(MainMenuScreen instance, SpriteBatch sb) {
            if (CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU) {
                // Initialize if not already done
                if (!initialized) {
                    initializeLevelDisplay();
                    initialized = true;
                }

                // Force original drawing color
                sb.setColor(Color.WHITE);

                // Draw level display panel background
                renderLevelPanel(sb);

                // Draw level text
                renderLevelText(sb);
            }
        }
    }

    /**
     * Initialize level display panel
     */
    private static void initializeLevelDisplay() {
        // Set fixed positions based on screen dimensions
        levelX = Settings.WIDTH * 0.65f;
        levelY = Settings.HEIGHT * 0.8f;

        // Set panel dimensions
        panelWidth = 300.0f * Settings.scale;
        panelHeight = 130.0f * Settings.scale;

        // Setup font for rendering
        levelFont = FontHelper.buttonLabelFont;

//         logger.info("Initialized level display at X: " + levelX + ", Y: " + levelY);
    }

    /**
     * Render the background panel for the level display
     */
    private static void renderLevelPanel(SpriteBatch sb) {
        // Draw a semi-transparent panel background
        sb.setColor(new Color(0.2f, 0.2f, 0.2f, 0.8f));

        // Use existing game methods to draw the panel
        try {
            // Draw panel background
            sb.draw(ImageMaster.WHITE_SQUARE_IMG,
                    levelX,
                    levelY - panelHeight,
                    panelWidth,
                    panelHeight);

            // Draw panel border
            sb.setColor(new Color(0.8f, 0.8f, 0.8f, 0.9f));
            float borderThickness = 2.0f * Settings.scale;

            // Top border
            sb.draw(ImageMaster.WHITE_SQUARE_IMG,
                    levelX,
                    levelY - borderThickness,
                    panelWidth,
                    borderThickness);

            // Right border
            sb.draw(ImageMaster.WHITE_SQUARE_IMG,
                    levelX + panelWidth - borderThickness,
                    levelY - panelHeight,
                    borderThickness,
                    panelHeight);

            // Bottom border
            sb.draw(ImageMaster.WHITE_SQUARE_IMG,
                    levelX,
                    levelY - panelHeight,
                    panelWidth,
                    borderThickness);

            // Left border
            sb.draw(ImageMaster.WHITE_SQUARE_IMG,
                    levelX,
                    levelY - panelHeight,
                    borderThickness,
                    panelHeight);

        } catch (Exception e) {
//             logger.error("Failed to render level panel: " + e.getMessage());
        }
    }

    /**
     * Render the level and XP text on the panel
     */
    private static void renderLevelText(SpriteBatch sb) {
        int currentLevel = Spirepass.getCurrentLevel();
        int xpForNextLevel = Spirepass.getXPForNextLevel();
        int maxLevel = Spirepass.MAX_LEVEL;

        // Title text
        String titleText = "SPIREPASS";
        float titleX = levelX + (panelWidth / 2);
        float titleY = levelY - 20.0f * Settings.scale;

        // Level text with a gold color for emphasis
        String levelText = "LEVEL " + currentLevel + "/" + maxLevel;
        float levelTextX = levelX + 20.0f * Settings.scale;
        float levelTextY = titleY - 30.0f * Settings.scale;
        Color levelColor = new Color(1.0f, 0.85f, 0.25f, 1.0f); // Gold color

        // XP text
        String xpText = "XP TO NEXT LEVEL: " + xpForNextLevel;
        if (currentLevel >= maxLevel) {
            xpText = "MAX LEVEL REACHED!";
        }
        float xpX = levelX + 20.0f * Settings.scale;
        float xpY = levelTextY - 30.0f * Settings.scale;

        // Render text with shadows for better visibility
        FontHelper.renderFontCentered(sb, FontHelper.charDescFont, titleText, titleX, titleY, Settings.GOLD_COLOR);
        FontHelper.renderFontLeft(sb, levelFont, levelText, levelTextX, levelTextY, levelColor);
        FontHelper.renderFontLeft(sb, levelFont, xpText, xpX, xpY, Settings.CREAM_COLOR);

        // If not at max level, render XP progress bar
        if (currentLevel < maxLevel) {
            renderXPProgressBar(sb, xpX, xpY - 25.0f * Settings.scale, panelWidth - 40.0f * Settings.scale);
        }
    }

    /**
     * Render a progress bar showing XP progress to next level
     */
    private static void renderXPProgressBar(SpriteBatch sb, float x, float y, float width) {
        int currentLevel = Spirepass.getCurrentLevel();
        int totalXP = currentLevel * Spirepass.XP_PER_LEVEL; // Total XP at start of level
        int nextLevelXP = (currentLevel + 1) * Spirepass.XP_PER_LEVEL; // XP needed for next level
        int currentTotalXP = totalXP + (Spirepass.XP_PER_LEVEL - Spirepass.getXPForNextLevel()); // Current total XP

        // Calculate progress percentage
        float progress = (float)(currentTotalXP - totalXP) / (float)(nextLevelXP - totalXP);
        progress = MathUtils.clamp(progress, 0.0f, 1.0f);

        // Draw background
        sb.setColor(new Color(0.3f, 0.3f, 0.3f, 0.8f));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, width, 12.0f * Settings.scale);

        // Draw progress
        sb.setColor(new Color(0.2f, 0.6f, 1.0f, 0.9f)); // Blue progress
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, width * progress, 12.0f * Settings.scale);

        // Draw border
        sb.setColor(new Color(0.8f, 0.8f, 0.8f, 0.9f));
        float borderThickness = 1.0f * Settings.scale;

        // Draw borders using four rectangles (top, right, bottom, left)
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y + 12.0f * Settings.scale - borderThickness, width, borderThickness);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x + width - borderThickness, y, borderThickness, 12.0f * Settings.scale);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, width, borderThickness);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, borderThickness, 12.0f * Settings.scale);

        // Draw percentage text
        String percentText = Math.round(progress * 100) + "%";
        float textX = x + (width / 2);
        float textY = y + 6.0f * Settings.scale;
        FontHelper.renderFontCentered(sb, FontHelper.topPanelAmountFont, percentText, textX, textY, Color.WHITE);
    }

    @SpirePatch(clz = MainMenuScreen.class, method = "update")
    public static class UpdatePatch {
        @SpirePostfixPatch
        public static void updateLevelDisplay(MainMenuScreen instance) {
            if (CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU) {
                // Check for mouseover to show tooltip with detailed info
                if (InputHelper.mX >= levelX &&
                        InputHelper.mX <= levelX + panelWidth &&
                        InputHelper.mY >= levelY - panelHeight &&
                        InputHelper.mY <= levelY) {

                    renderLevelTooltip();
                }
            }
        }
    }

    /**
     * Render a tooltip with additional level and XP information
     */
    private static void renderLevelTooltip() {
        int currentLevel = Spirepass.getCurrentLevel();
        int xpForNextLevel = Spirepass.getXPForNextLevel();
        int maxLevel = Spirepass.MAX_LEVEL;

        StringBuilder tipText = new StringBuilder();

        tipText.append("Battle Pass Level: #y").append(currentLevel).append("/").append(maxLevel).append(" NL ");

        if (currentLevel < maxLevel) {
            tipText.append("XP needed for Level ").append(currentLevel + 1).append(": #b")
                    .append(xpForNextLevel).append(" NL NL ");

            tipText.append("Challenge XP Rewards: NL ");
            tipText.append("- Daily Challenge: #g").append(Spirepass.DAILY_CHALLENGE_XP).append(" XP NL ");
            tipText.append("- Weekly Challenge: #g").append(Spirepass.WEEKLY_CHALLENGE_XP).append(" XP");
        } else {
            tipText.append("You've reached the maximum level! NL ");
            tipText.append("Congratulations on completing the Battle Pass!");
        }

        // Render tooltip to the left of the panel
        TipHelper.renderGenericTip(
                levelX - 280.0f * Settings.scale,
                levelY - 40.0f * Settings.scale,
                "SPIREPASS INFO",
                tipText.toString()
        );
    }
}