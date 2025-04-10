package spirepass.screens;

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
    // ==================== CONSTANTS & STATIC VARIABLES ====================

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("SpirepassScreen"));
    private static final String[] TEXT = uiStrings.TEXT;
    private static Float cachedBoxW = null;
    private static final float TOOLTIP_RIGHT_PADDING = 15.0f * Settings.scale;
    private static final float EQUIP_BUTTON_PADDING = 80.0f * Settings.scale;
    private static final float FIXED_BUTTON_WIDTH = 200.0f;
    private static final float PREMIUM_BUTTON_Y = 40.0f * Settings.scale;
    private static final String PREMIUM_TARGET_URL = "https://store.steampowered.com/app/2868840/Slay_the_Spire_2/";

    // ==================== INSTANCE VARIABLES ====================

    // Textures
    private Texture backgroundTexture;
    private Texture levelBoxTexture;
    private Texture currentLevelBoxTexture;
    private Texture lockedLevelBoxTexture;
    private Map<String, Texture> customBackgroundTextures = new HashMap<>();

    // UI elements
    private ModLabeledButton equipButton;
    private ModLabeledButton premiumButton;

    // Component managers
    private SpirepassRewardManager rewardManager;
    private SpirepassAnimationManager animationManager;

    // ==================== INITIALIZATION ====================

    public SpirepassScreenRenderer() {
        try {
            this.backgroundTexture = ImageMaster.loadImage("spirepass/images/screen/SpirepassBackground.jpg");
            this.levelBoxTexture = ImageMaster.OPTION_YES;
            this.currentLevelBoxTexture = ImageMaster.OPTION_YES;
            this.lockedLevelBoxTexture = ImageMaster.OPTION_NO;
            this.rewardManager = new SpirepassRewardManager();
            this.animationManager = new SpirepassAnimationManager();
            initializePremiumButton();
        } catch (Exception e) {
            System.err.println("Failed to load SpirePass textures or init buttons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializePremiumButton() {
        // Don't initialize if premium button is hidden in settings
        if (Spirepass.hideSpirepassPremium) {
            this.premiumButton = null;
            return;
        }

        // Calculate text width without scaling for accurate measurement
        float textWidth = FontHelper.getSmartWidth(FontHelper.buttonLabelFont, TEXT[4], 9999f, 0f);

        // Calculate button position - CRITICAL: divide by Settings.scale because ModLabeledButton applies it internally
        float estimatedButtonWidth = textWidth + 80.0f; // No scaling - ModLabeledButton handles it
        float buttonX = ((Settings.WIDTH / 2.0f) - (estimatedButtonWidth / 2.0f)) / Settings.scale;
        float buttonY = PREMIUM_BUTTON_Y / Settings.scale; // Remove scaling as ModLabeledButton will apply it

        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(PREMIUM_TARGET_URL));
                    CardCrawlGame.sound.play("UI_CLICK_1");
                } else {
                    System.err.println("Desktop browsing not supported.");
                    CardCrawlGame.sound.play("UI_CLICK_2");
                }
            } catch (IOException | URISyntaxException ex) {
                System.err.println("Failed to open URL: " + PREMIUM_TARGET_URL + " - " + ex.getMessage());
                ex.printStackTrace();
                CardCrawlGame.sound.play("UI_CLICK_2");
            }
        };

        this.premiumButton = new ModLabeledButton(
                TEXT[4],
                buttonX,
                buttonY,
                Color.YELLOW,
                Color.GOLD,
                FontHelper.buttonLabelFont,
                null,
                clickConsumer
        );

        // No need for additional centering - button should be properly positioned now
    }

    // ==================== UPDATE & RENDER METHODS ====================

    public void updateUI() {
        if (equipButton != null) {
            equipButton.update();
        }
        if (premiumButton != null && !Spirepass.hideSpirepassPremium) {
            premiumButton.update();
        }
    }

    public void renderUIElements(SpriteBatch sb) {
        if (premiumButton != null && !Spirepass.hideSpirepassPremium) {
            premiumButton.render(sb);
            renderPremiumTooltip();
        }
    }

    private void renderPremiumTooltip() {
        if (premiumButton == null || Spirepass.hideSpirepassPremium) return;

        try {
            Field hbField = ModLabeledButton.class.getDeclaredField("hb");
            hbField.setAccessible(true);
            Hitbox buttonHb = (Hitbox) hbField.get(premiumButton);

            if (buttonHb != null && buttonHb.hovered) {
                if (cachedBoxW == null) {
                    try {
                        Field boxWField = TipHelper.class.getDeclaredField("BOX_W");
                        boxWField.setAccessible(true);
                        cachedBoxW = (Float) boxWField.get(null);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        System.err.println("Failed reflection for TipHelper.BOX_W: " + e.getMessage());
                        cachedBoxW = 320.0f * Settings.scale;
                    }
                }

                float boxWidth = cachedBoxW;

                // Use relative positioning instead of fixed coordinates
                float tipX = Settings.WIDTH * 0.75f;
                tipX = Math.min(tipX, Settings.WIDTH - boxWidth - TOOLTIP_RIGHT_PADDING);
                tipX = Math.max(tipX, TOOLTIP_RIGHT_PADDING);

                // Use relative height instead of fixed coordinates
                float tipY = Settings.HEIGHT - (50.0f * Settings.scale);

                TipHelper.renderGenericTip(tipX, tipY, TEXT[5], TEXT[6]);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Error logging omitted
        } catch (Exception e) {
            // Error logging omitted
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
                } catch (Exception e) {
                    System.err.println("Failed to load custom background: " + backgroundId);
                    e.printStackTrace();
                }
            }
            if (customBackgroundTextures.containsKey(backgroundId)) {
                currentBackground = customBackgroundTextures.get(backgroundId);
            }
        }

        if (currentBackground != null) {
            sb.setColor(Color.WHITE);
            sb.draw(currentBackground, 0, 0, 0, 0, Settings.WIDTH, Settings.HEIGHT, 1, 1, 0, 0, 0,
                    currentBackground.getWidth(), currentBackground.getHeight(), false, false);
        }

        FontHelper.renderFontCentered(sb, FontHelper.bannerNameFont, TEXT[0],
                Settings.WIDTH / 2.0f, SpirepassPositionSettings.TITLE_Y, Color.WHITE);

        renderLevelBoxes(sb, screen, scrollX, edgePadding, levelBoxes);
    }

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
            if (equipButton != null) equipButton.render(sb);
        } else {
            updateLockedEquipButton(sb);
            if (equipButton != null) equipButton.render(sb);
        }
    }

    // ==================== BUTTON MANAGEMENT ====================

    private void updateLockedEquipButton(SpriteBatch sb) {
        String buttonText = TEXT[3]; // "Locked"
        Color buttonColor = Color.GRAY;
        Color hoverColor = Color.DARK_GRAY;

        // Remove scaling from button position - ModLabeledButton will apply it
        float buttonY = (SpirepassPositionSettings.REWARD_BUTTON_Y - 25.0f) / Settings.scale;

        // Use fixed width instead of calculating based on text
        float buttonX = ((Settings.WIDTH / 2.0f) - (FIXED_BUTTON_WIDTH / 2.0f)) / Settings.scale;

        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            CardCrawlGame.sound.play("UI_CLICK_2");
        };

        if (equipButton == null) {
            // Create new button
            equipButton = new ModLabeledButton(buttonText, buttonX, buttonY,
                    buttonColor, hoverColor, FontHelper.buttonLabelFont, null, clickConsumer);

            // Force fixed width after creation
            setButtonFixedWidth(equipButton, FIXED_BUTTON_WIDTH);
        } else {
            // Update existing button
            equipButton.label = buttonText;
            equipButton.color = buttonColor;
            equipButton.colorHover = hoverColor;
            equipButton.set(buttonX, buttonY);

            // Force fixed width after update
            setButtonFixedWidth(equipButton, FIXED_BUTTON_WIDTH);

            try {
                Field clickField = ModLabeledButton.class.getDeclaredField("click");
                clickField.setAccessible(true);
                clickField.set(equipButton, clickConsumer);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateEquipButton(SpriteBatch sb, SpirepassLevelBox selectedBox) {
        int level = selectedBox.getLevel();
        SpirepassRewardData reward = rewardManager.getRewardData(level);
        if (reward != null && reward.getType() == SpirepassRewardData.RewardType.TEXT) {
            equipButton = null;
            return;
        }

        boolean isUnlocked = selectedBox.isUnlocked();
        boolean isEquipped = reward != null && rewardManager.isRewardEquipped(reward);
        String buttonText = isEquipped ? TEXT[2] : TEXT[1]; // "Unequip" or "Equip"
        Color buttonColor = isEquipped ? Color.ORANGE : Color.WHITE;
        Color hoverColor = isEquipped ? Color.YELLOW : Color.GREEN;

        // Remove scaling from button position - ModLabeledButton will apply it
        float buttonY = (SpirepassPositionSettings.REWARD_BUTTON_Y - 25.0f) / Settings.scale;

        // Use fixed width instead of calculating based on text
        float buttonX = ((Settings.WIDTH / 2.0f) - (FIXED_BUTTON_WIDTH / 2.0f)) / Settings.scale;

        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            if (isUnlocked && reward != null) {
                rewardManager.toggleRewardEquipped(reward);
                CardCrawlGame.sound.play("UI_CLICK_1");
            } else {
                CardCrawlGame.sound.play("UI_CLICK_2");
            }
        };

        if (equipButton == null) {
            // Create new button
            equipButton = new ModLabeledButton(buttonText, buttonX, buttonY,
                    buttonColor, hoverColor, FontHelper.buttonLabelFont, null, clickConsumer);

            // Force fixed width after creation
            setButtonFixedWidth(equipButton, FIXED_BUTTON_WIDTH);
        } else {
            // Update existing button
            equipButton.label = buttonText;
            equipButton.color = buttonColor;
            equipButton.colorHover = hoverColor;
            equipButton.set(buttonX, buttonY);

            // Force fixed width after update
            setButtonFixedWidth(equipButton, FIXED_BUTTON_WIDTH);

            try {
                Field clickField = ModLabeledButton.class.getDeclaredField("click");
                clickField.setAccessible(true);
                clickField.set(equipButton, clickConsumer);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void setButtonFixedWidth(ModLabeledButton button, float fixedWidth) {
        if (button == null) return;

        try {
            // Access button's width and middle_width fields
            Field wField = ModLabeledButton.class.getDeclaredField("w");
            Field middleWidthField = ModLabeledButton.class.getDeclaredField("middle_width");
            Field hbField = ModLabeledButton.class.getDeclaredField("hb");

            wField.setAccessible(true);
            middleWidthField.setAccessible(true);
            hbField.setAccessible(true);

            // Calculate middle_width based on desired total width
            // The original button width is calculated as:
            // w = (textureLeft.getWidth() + textureRight.getWidth()) * Settings.scale + middle_width
            float textureWidth = ((float)ImageMaster.loadImage("img/ButtonLeft.png").getWidth() +
                    (float)ImageMaster.loadImage("img/ButtonRight.png").getWidth()) *
                    Settings.scale;

            // Force the new width
            float newMiddleWidth = fixedWidth - textureWidth;
            if (newMiddleWidth < 0) newMiddleWidth = 0;

            // Set the width fields
            wField.set(button, fixedWidth);
            middleWidthField.set(button, newMiddleWidth);

            // Update hitbox width
            Hitbox hb = (Hitbox) hbField.get(button);
            if (hb != null) {
                hb.width = fixedWidth - 2.0f * Settings.scale;
                hb.cX = hb.x + hb.width / 2.0f;
            }
        } catch (Exception e) {
            System.err.println("Failed to set fixed button width: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void adjustButtonCentering(ModLabeledButton button, float buttonY) {
        if (button == null) return;
        try {
            Field hbField = ModLabeledButton.class.getDeclaredField("hb");
            hbField.setAccessible(true);
            Hitbox buttonHb = (Hitbox) hbField.get(button);
            if (buttonHb != null && buttonHb.width > 0) {
                float actualButtonWidth = buttonHb.width;
                float correctButtonX = (Settings.WIDTH / 2.0f) - (actualButtonWidth / 2.0f);
                button.set(correctButtonX, buttonY);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed reflection for equip button hitbox adjustment: " + e.getMessage());
        }
    }

    // ==================== GETTERS ====================

    public Texture getLevelBoxTexture() {
        return levelBoxTexture;
    }

    public Texture getCurrentLevelBoxTexture() {
        return currentLevelBoxTexture;
    }

    public Texture getLockedLevelBoxTexture() {
        return lockedLevelBoxTexture;
    }

    public ModLabeledButton getEquipButton() {
        return equipButton;
    }

    public SpirepassAnimationManager getAnimationManager() {
        return animationManager;
    }

    public SpirepassRewardManager getRewardManager() {
        return rewardManager;
    }

    // ==================== CLEANUP ====================

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