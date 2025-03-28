package spirepass.patches;

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
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import spirepass.Spirepass;

public class MainMenuLevelDisplayPatch {

    // Keep references to the desired fonts (now potentially smaller ones)
    private static BitmapFont titleFont;
    private static BitmapFont bodyFont;
    private static BitmapFont progressFont;

    private static boolean initialized = false;

    private static float panelY;
    private static float scaledPanelHeight; // Actual height used for drawing
    private static float scaledHorizontalPadding;
    private static float scaledMinPanelWidth;

    private static final float DESIGN_SCALE_FACTOR = 0.75f;

    private static final float BASE_HORIZONTAL_PADDING = 40.0f;
    private static final float BASE_MIN_PANEL_WIDTH = 280.0f;
    private static final float BASE_PANEL_HEIGHT = 150.0f;

    private static final float TARGET_CENTER_X_RATIO = 0.6f;
    private static final float VERTICAL_OFFSET_Y = 30f;

    private static final Color PANEL_BG_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.8f);
    private static final Color PANEL_BORDER_COLOR = new Color(0.8f, 0.8f, 0.8f, 0.9f);
    private static final Color XP_BAR_BG_COLOR = new Color(0.3f, 0.3f, 0.3f, 0.8f);
    private static final Color XP_BAR_FG_COLOR = new Color(0.2f, 0.6f, 1.0f, 0.9f);
    private static final Color LEVEL_TEXT_COLOR = new Color(1.0f, 0.85f, 0.25f, 1.0f);

    @SpirePatch(clz = MainMenuScreen.class, method = "render")
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void renderLevelDisplay(MainMenuScreen instance, SpriteBatch sb) {
            // Add this check
            if (!Spirepass.enableMainMenuElements) {
                // Reset initialized flag if elements are disabled, so they re-init if re-enabled
                if (initialized) {
                    initialized = false;
                }
                return;
            }
            // Original code follows
            if (CardCrawlGame.mainMenuScreen.screen == MainMenuScreen.CurScreen.MAIN_MENU) {
                if (!initialized) {
                    initializeStaticData();
                    initialized = true;
                }

                int currentLevel = Spirepass.getCurrentLevel();
                int maxLevel = Spirepass.MAX_LEVEL;
                String xpText = getXpText(currentLevel, maxLevel);
                String levelText = "LEVEL " + currentLevel + "/" + maxLevel;
                String titleText = "SPIREPASS";

                // Width calculation uses the assigned fonts
                float xpTextWidth = FontHelper.getSmartWidth(bodyFont, xpText, Settings.WIDTH, 0f);
                float levelTextWidth = FontHelper.getSmartWidth(bodyFont, levelText, Settings.WIDTH, 0f);
                float titleWidth = FontHelper.getSmartWidth(titleFont, titleText, Settings.WIDTH, 0f);

                float requiredWidth = Math.max(xpTextWidth, Math.max(levelTextWidth, titleWidth));
                float dynamicPanelWidth = Math.max(scaledMinPanelWidth, requiredWidth + scaledHorizontalPadding);

                float targetCenterX = Settings.WIDTH * TARGET_CENTER_X_RATIO;
                float dynamicPanelX = targetCenterX - (dynamicPanelWidth / 2.0f);

                Color originalColor = sb.getColor().cpy();
                sb.setColor(Color.WHITE);

                renderLevelPanel(sb, dynamicPanelX, dynamicPanelWidth);
                renderLevelText(sb, dynamicPanelX, dynamicPanelWidth, xpText, levelText, titleText, currentLevel, maxLevel);

                sb.setColor(originalColor);
            }
        }
    }

    private static void initializeStaticData() {
        // Assign the desired smaller fonts
        titleFont = FontHelper.tipHeaderFont; // Was charDescFont
        bodyFont = FontHelper.tipBodyFont;    // Was buttonLabelFont
        progressFont = FontHelper.powerAmountFont; // Was topPanelAmountFont

        // Calculate scaled dimensions (panel layout uses these)
        scaledPanelHeight = BASE_PANEL_HEIGHT * DESIGN_SCALE_FACTOR * Settings.scale;
        scaledHorizontalPadding = BASE_HORIZONTAL_PADDING * DESIGN_SCALE_FACTOR * Settings.scale;
        scaledMinPanelWidth = BASE_MIN_PANEL_WIDTH * DESIGN_SCALE_FACTOR * Settings.scale;

        // Calculate target Y
        float buttonBaseY = Settings.HEIGHT * 0.8f;
        float buttonSpacing = 120.0f * Settings.scale;
        float dailyBtnY = buttonBaseY + buttonSpacing;
        float weeklyBtnY = buttonBaseY;
        float scaledVerticalOffsetY = VERTICAL_OFFSET_Y * DESIGN_SCALE_FACTOR * Settings.scale;
        float midpointY = ((dailyBtnY + weeklyBtnY) / 2.0f) + scaledVerticalOffsetY;
        panelY = midpointY + (scaledPanelHeight / 2.0f);
    }

    private static String getXpText(int currentLevel, int maxLevel) {
        if (currentLevel >= maxLevel) {
            return "MAX LEVEL REACHED!";
        } else {
            int xpForNextLevel = Spirepass.getXPForNextLevel();
            int nextLevel = currentLevel + 1;
            return "XP NEEDED FOR LEVEL " + nextLevel + ": " + xpForNextLevel;
        }
    }

    private static void renderLevelPanel(SpriteBatch sb, float panelX, float panelWidth) {
        float borderThickness = 2.0f * DESIGN_SCALE_FACTOR * Settings.scale;

        sb.setColor(PANEL_BG_COLOR);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG,
                panelX,
                panelY - scaledPanelHeight,
                panelWidth,
                scaledPanelHeight);

        sb.setColor(PANEL_BORDER_COLOR);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, panelX, panelY - borderThickness, panelWidth, borderThickness);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, panelX, panelY - scaledPanelHeight, panelWidth, borderThickness);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, panelX, panelY - scaledPanelHeight, borderThickness, scaledPanelHeight);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, panelX + panelWidth - borderThickness, panelY - scaledPanelHeight, borderThickness, scaledPanelHeight);
    }

    // Uses the newly assigned font variables (titleFont, bodyFont)
    private static void renderLevelText(SpriteBatch sb, float panelX, float panelWidth, String xpText, String levelText, String titleText, int currentLevel, int maxLevel) {

        float initialYOffset = 25.0f * DESIGN_SCALE_FACTOR * Settings.scale;
        // Adjust line spacing if needed based on the new fonts' actual heights
        // May need experimentation - start with the scaled value
        float lineSpacing = 30.0f * DESIGN_SCALE_FACTOR * Settings.scale;

        float currentY = panelY - initialYOffset;
        float centerX = panelX + (panelWidth / 2f);
        float contentX = panelX + (scaledHorizontalPadding / 2f);
        float contentWidth = panelWidth - scaledHorizontalPadding;

        FontHelper.renderFontCentered(sb, titleFont, titleText, centerX, currentY, Settings.GOLD_COLOR); // Use titleFont
        currentY -= lineSpacing * 1.1f; // Adjust multiplier if spacing looks off

        FontHelper.renderFontCentered(sb, bodyFont, levelText, centerX, currentY, LEVEL_TEXT_COLOR); // Use bodyFont
        currentY -= lineSpacing; // Adjust multiplier if spacing looks off

        FontHelper.renderFontLeft(sb, bodyFont, xpText, contentX, currentY, Settings.CREAM_COLOR); // Use bodyFont
        currentY -= lineSpacing * 0.9f; // Adjust multiplier if spacing looks off

        if (currentLevel < maxLevel) {
            float scaledBarHeight = 12.0f * DESIGN_SCALE_FACTOR * Settings.scale;
            float progressBarBottomY = currentY - scaledBarHeight;
            renderXPProgressBar(sb, contentX, progressBarBottomY, contentWidth);
        }
    }

    // Uses the newly assigned font variable (progressFont)
    private static void renderXPProgressBar(SpriteBatch sb, float x, float y, float width) {
        int currentLevel = Spirepass.getCurrentLevel();
        int xpNeededForLevelUp = Spirepass.XP_PER_LEVEL;
        int xpEarnedThisLevel = 0;
        if (currentLevel < Spirepass.MAX_LEVEL) {
            xpEarnedThisLevel = xpNeededForLevelUp - Spirepass.getXPForNextLevel();
        }

        float progress = 0.0f;
        if (xpNeededForLevelUp > 0) {
            progress = (float)xpEarnedThisLevel / (float)xpNeededForLevelUp;
        }
        progress = MathUtils.clamp(progress, 0.0f, 1.0f);

        float barHeight = 12.0f * DESIGN_SCALE_FACTOR * Settings.scale;
        float borderThickness = 1.0f * DESIGN_SCALE_FACTOR * Settings.scale;

        sb.setColor(XP_BAR_BG_COLOR);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, width, barHeight);

        sb.setColor(XP_BAR_FG_COLOR);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, width * progress, barHeight);

        sb.setColor(PANEL_BORDER_COLOR);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y + barHeight - borderThickness, width, borderThickness);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x + width - borderThickness, y, borderThickness, barHeight);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, width, borderThickness);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, borderThickness, barHeight);

        String percentText = Math.round(progress * 100) + "%";
        float textX = x + (width / 2f);
        float textY = y + (barHeight / 2f);
        FontHelper.renderFontCentered(sb, progressFont, percentText, textX, textY, Color.WHITE); // Use progressFont
    }
}