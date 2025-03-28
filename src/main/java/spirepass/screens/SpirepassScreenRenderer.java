package spirepass.screens;

import basemod.BaseMod;
import basemod.ModLabeledButton;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import spirepass.Spirepass;
import spirepass.elements.SpirepassLevelBox;
import spirepass.spirepassutil.SkinManager;
import spirepass.spirepassutil.SpirepassPositionSettings;
import spirepass.spirepassutil.SpirepassRewardData;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static spirepass.Spirepass.makeID;

public class SpirepassScreenRenderer {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("SpirepassScreen"));
    // Textures for level boxes
    private Texture backgroundTexture;
    private Texture levelBoxTexture;
    private Texture currentLevelBoxTexture;
    private Texture lockedLevelBoxTexture;
    private Map<String, Texture> customBackgroundTextures = new HashMap<>();

    // UI elements
    private ModLabeledButton equipButton;
    private ModLabeledButton premiumButton; // New button for Premium

    // Component managers
    private SpirepassRewardManager rewardManager;
    private SpirepassAnimationManager animationManager;

    // Constants for the Premium Button
    private static final float PREMIUM_BUTTON_Y = 80.0f * Settings.scale; // Position from bottom
    private static final String PREMIUM_BUTTON_TEXT = "BUY SPIREPASS PREMIUM"; // Use final string
    private static final String PREMIUM_TOOLTIP_HEADER = "Why buy Premium?";
    private static final String PREMIUM_TOOLTIP_BODY = "Get early access to: NL -Test 1 NL -Test 2";
    private static final String PREMIUM_TARGET_URL = "https://store.steampowered.com/app/2868840/Slay_the_Spire_2/";

    public SpirepassScreenRenderer() {
        // Load the textures
        try {
            this.backgroundTexture = ImageMaster.loadImage("spirepass/images/screen/SpirepassBackground.jpg");
            this.levelBoxTexture = ImageMaster.OPTION_YES;
            this.currentLevelBoxTexture = ImageMaster.OPTION_YES;
            this.lockedLevelBoxTexture = ImageMaster.OPTION_NO;

            this.rewardManager = new SpirepassRewardManager();
            this.animationManager = new SpirepassAnimationManager();

            // Initialize the Premium Button
            initializePremiumButton();

        } catch (Exception e) {
            System.err.println("Failed to load SpirePass textures or init buttons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializePremiumButton() {
        // Calculate position (centered horizontally)
        // ModLabeledButton constructor takes final scaled coordinates
        float buttonX = Settings.WIDTH / 2.0f;
        float buttonY = PREMIUM_BUTTON_Y; // Use the constant

        // Define the click action
        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(PREMIUM_TARGET_URL));
                    CardCrawlGame.sound.play("UI_CLICK_1"); // Play a confirmation sound
                } else {
                    // Log error or inform user if browsing is not supported
                    System.err.println("Desktop browsing not supported.");
                    CardCrawlGame.sound.play("UI_CLICK_2"); // Play error/cancel sound
                }
            } catch (IOException | URISyntaxException ex) {
                System.err.println("Failed to open URL: " + PREMIUM_TARGET_URL + " - " + ex.getMessage());
                ex.printStackTrace();
                CardCrawlGame.sound.play("UI_CLICK_2");
            }
        };

        // Create the button
        this.premiumButton = new ModLabeledButton(
                PREMIUM_BUTTON_TEXT,
                buttonX, // Already scaled center X
                buttonY, // Already scaled Y
                Color.YELLOW,      // Base text color
                Color.GOLD,        // Hover text color
                FontHelper.buttonLabelFont,
                null,              // No specific panel context needed usually
                clickConsumer
        );
    }

    // New method to update UI elements like buttons
    public void updateUI() {
        if (equipButton != null) {
            equipButton.update();
        }
        if (premiumButton != null) {
            premiumButton.update();
        }
    }

    // New method to render UI elements that should be on top (like tooltips)
    public void renderUIElements(SpriteBatch sb) {
        if (premiumButton != null) {
            premiumButton.render(sb);
        }
        renderPremiumTooltip();
    }

    // Helper method to render the premium button's tooltip
    private void renderPremiumTooltip() {
        if (premiumButton == null) return;
        try {
            Field hbField = ModLabeledButton.class.getDeclaredField("hb");
            hbField.setAccessible(true);
            Hitbox buttonHb = (Hitbox) hbField.get(premiumButton);
            if (buttonHb != null && buttonHb.hovered) {
                float tipX = buttonHb.cX + (buttonHb.width / 2f) + (15.0f * Settings.scale);
                float tipY = buttonHb.cY;
                TipHelper.renderGenericTip(tipX, tipY, PREMIUM_TOOLTIP_HEADER, PREMIUM_TOOLTIP_BODY);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed reflection for premium button hitbox: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error rendering premium tooltip: " + e.getMessage());
        }
    }


    public void render(SpriteBatch sb, SpirepassScreen screen, float scrollX, float edgePadding, ArrayList<SpirepassLevelBox> levelBoxes) {
        String backgroundId = SkinManager.getInstance().getAppliedSkin(SkinManager.BACKGROUND_SCREEN);
        Texture currentBackground = this.backgroundTexture;
        if (backgroundId != null && !backgroundId.isEmpty()) {
            if (!customBackgroundTextures.containsKey(backgroundId)) {
                try {
                    Texture customTexture = ImageMaster.loadImage(backgroundId);
                    customBackgroundTextures.put(backgroundId, customTexture);
                } catch (Exception e) { System.err.println("Failed to load custom background: " + backgroundId); e.printStackTrace(); }
            }
            if (customBackgroundTextures.containsKey(backgroundId)) {
                currentBackground = customBackgroundTextures.get(backgroundId);
            }
        }
        if (currentBackground != null) {
            sb.setColor(Color.WHITE);
            sb.draw(currentBackground, 0, 0, 0, 0, Settings.WIDTH, Settings.HEIGHT, 1, 1, 0, 0, 0, currentBackground.getWidth(), currentBackground.getHeight(), false, false);
        }

        FontHelper.renderFontCentered(sb, FontHelper.bannerNameFont, uiStrings.TEXT[0], Settings.WIDTH / 2.0f, SpirepassPositionSettings.TITLE_Y, Color.WHITE);

        renderLevelBoxes(sb, screen, scrollX, edgePadding, levelBoxes);
    }

    // (Keep renderLevelProgressBar, renderLevelBoxes, renderSelectedLevelReward,
    // updateLockedEquipButton, updateEquipButton as they were)
    // ... existing methods ...
    public void renderLevelProgressBar(SpriteBatch sb, int fromLevel, int toLevel, float xPos, float yPos, float width) {
        int currentLevel = Spirepass.getCurrentLevel();
        if (fromLevel > currentLevel) {
            return;
        }
        float progress = 0.0f;
        boolean isCompletedLevel = false;
        if (fromLevel == currentLevel) {
            int xpForNextLevel = Spirepass.getXPForNextLevel();
            int xpPerLevel = Spirepass.XP_PER_LEVEL;
            progress = 1.0f - ((float)xpForNextLevel / xpPerLevel);
            progress = MathUtils.clamp(progress, 0.0f, 1.0f);
        } else if (fromLevel < currentLevel) {
            progress = 1.0f;
            isCompletedLevel = true;
        }
        sb.setColor(SpirepassPositionSettings.PROGRESS_BAR_BG_COLOR);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG,
                xPos,
                yPos,
                width,
                SpirepassPositionSettings.PROGRESS_BAR_HEIGHT);
        if (isCompletedLevel) {
            sb.setColor(SpirepassPositionSettings.PROGRESS_BAR_COMPLETED_COLOR);
        } else {
            sb.setColor(SpirepassPositionSettings.PROGRESS_BAR_CURRENT_COLOR);
        }
        sb.draw(ImageMaster.WHITE_SQUARE_IMG,
                xPos,
                yPos,
                width * progress,
                SpirepassPositionSettings.PROGRESS_BAR_HEIGHT);
    }

    private void renderLevelBoxes(SpriteBatch sb, SpirepassScreen screen, float scrollX, float edgePadding, ArrayList<SpirepassLevelBox> levelBoxes) {
        int firstVisibleLevel = Math.max(0, (int) ((scrollX - edgePadding) / screen.getLevelBoxSpacing()) - 1);
        int lastVisibleLevel = Math.min(screen.getMaxLevel(), (int) ((scrollX + Settings.WIDTH - edgePadding) / screen.getLevelBoxSpacing()) + 1);
        for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
            if (i < levelBoxes.size() - 1) {
                SpirepassLevelBox currentBox = levelBoxes.get(i);
                SpirepassLevelBox nextBox = levelBoxes.get(i + 1);
                float barWidth = (nextBox.getX() - currentBox.getX() - currentBox.getBoxSize()) * (2.2f * Settings.scale);
                float barX = currentBox.getX() + (currentBox.getBoxSize() / 2) - (barWidth - (nextBox.getX() - currentBox.getX() - currentBox.getBoxSize())) / 2;
                barX -= SpirepassPositionSettings.PROGRESS_BAR_X_OFFSET;
                float barY = currentBox.getY() - (currentBox.getBoxSize() / 2) - SpirepassPositionSettings.PROGRESS_BAR_Y_OFFSET;
                renderLevelProgressBar(sb, i, i + 1, barX, barY, barWidth);
            }
        }
        for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
            if (i < levelBoxes.size()) {
                levelBoxes.get(i).render(sb);
            }
        }
    }

    public void renderSelectedLevelReward(SpriteBatch sb, SpirepassLevelBox selectedBox) {
        int level = selectedBox.getLevel();
        boolean isUnlocked = selectedBox.isUnlocked();
        rewardManager.renderReward(sb, level, isUnlocked);
        if (isUnlocked) {
            updateEquipButton(sb, selectedBox);
            if (equipButton != null) equipButton.render(sb); // Render equip button if unlocked
        } else {
            updateLockedEquipButton(sb);
            if (equipButton != null) equipButton.render(sb); // Render locked button
        }
    }

    private void updateLockedEquipButton(SpriteBatch sb) {
        String buttonText = uiStrings.TEXT[3];
        Color buttonColor = Color.GRAY;
        Color hoverColor = Color.DARK_GRAY;
        float buttonX = Settings.WIDTH / 2.0f - 80.0f; // Assuming ModLabeledButton centers itself?
        float buttonY = SpirepassPositionSettings.REWARD_BUTTON_Y - 25.0f;
        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            CardCrawlGame.sound.play("UI_CLICK_2");
        };
        if (equipButton == null) {
            equipButton = new ModLabeledButton(buttonText, buttonX, buttonY, // Pass scaled coords
                    buttonColor, hoverColor, FontHelper.buttonLabelFont, null, clickConsumer);
        } else {
            equipButton.label = buttonText;
            equipButton.color = buttonColor;
            equipButton.colorHover = hoverColor;
            equipButton.set(buttonX, buttonY); // Use set with scaled coords

            try {
                Field clickField = ModLabeledButton.class.getDeclaredField("click");
                clickField.setAccessible(true);
                clickField.set(equipButton, clickConsumer);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // Rendering moved to renderSelectedLevelReward
        // if (equipButton != null) {
        //     equipButton.render(sb);
        // }
    }

    private void updateEquipButton(SpriteBatch sb, SpirepassLevelBox selectedBox) {
        boolean isUnlocked = selectedBox.isUnlocked();
        int level = selectedBox.getLevel();
        SpirepassRewardData reward = rewardManager.getRewardData(level);
        if (reward != null && reward.getType() == SpirepassRewardData.RewardType.TEXT) {
            equipButton = null;
            return;
        }
        boolean isEquipped = rewardManager.isRewardEquipped(reward);
        String buttonText = isUnlocked ? (isEquipped ? uiStrings.TEXT[2] : uiStrings.TEXT[1]) : uiStrings.TEXT[3];
        Color buttonColor = isUnlocked ? (isEquipped ? Color.ORANGE : Color.WHITE) : Color.GRAY;
        Color hoverColor = isUnlocked ? (isEquipped ? Color.YELLOW : Color.GREEN) : Color.DARK_GRAY;
        float buttonX = Settings.WIDTH / 2.0f - 80.0f; // Assuming ModLabeledButton centers itself? Needs adjustment if not.
        float buttonY = SpirepassPositionSettings.REWARD_BUTTON_Y - 25.0f;
        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            if (isUnlocked && reward != null) {
                rewardManager.toggleRewardEquipped(reward);
            }
        };
        if (equipButton == null) {
            equipButton = new ModLabeledButton(buttonText, buttonX, buttonY, // Pass scaled coords
                    buttonColor, hoverColor, FontHelper.buttonLabelFont, null, clickConsumer);
        } else {
            equipButton.label = buttonText;
            equipButton.color = buttonColor;
            equipButton.colorHover = hoverColor;
            equipButton.set(buttonX, buttonY); // Use set with scaled coords
            try {
                Field clickField = ModLabeledButton.class.getDeclaredField("click");
                clickField.setAccessible(true);
                clickField.set(equipButton, clickConsumer);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // Rendering moved to renderSelectedLevelReward
        // if (equipButton != null) {
        //     equipButton.render(sb);
        // }
    }

    // Getters
    public Texture getLevelBoxTexture() { return levelBoxTexture; }
    public Texture getCurrentLevelBoxTexture() { return currentLevelBoxTexture; }
    public Texture getLockedLevelBoxTexture() { return lockedLevelBoxTexture; }
    public ModLabeledButton getEquipButton() { return equipButton; }
    public SpirepassAnimationManager getAnimationManager() { return animationManager; }
    public SpirepassRewardManager getRewardManager() { return rewardManager; }


    public void dispose() {
        if (animationManager != null) {
            animationManager.dispose();
        }
        if (rewardManager != null) {
            rewardManager.dispose();
        }

        if (customBackgroundTextures != null) {
            for (Texture texture : customBackgroundTextures.values()) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            customBackgroundTextures.clear();
        }

        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }

        // Dispose box textures only if they are custom-loaded, not base game assets
        if (levelBoxTexture != null && levelBoxTexture != ImageMaster.OPTION_YES && levelBoxTexture != ImageMaster.OPTION_NO) {
            levelBoxTexture.dispose();
        }
        if (currentLevelBoxTexture != null && currentLevelBoxTexture != ImageMaster.OPTION_YES && currentLevelBoxTexture != ImageMaster.OPTION_NO) {
            currentLevelBoxTexture.dispose();
        }
        if (lockedLevelBoxTexture != null && lockedLevelBoxTexture != ImageMaster.OPTION_YES && lockedLevelBoxTexture != ImageMaster.OPTION_NO) {
            lockedLevelBoxTexture.dispose();
        }

        // Nullify references
        animationManager = null;
        rewardManager = null;
        customBackgroundTextures = null;
        backgroundTexture = null;
        levelBoxTexture = null;
        currentLevelBoxTexture = null;
        lockedLevelBoxTexture = null;
    }
}