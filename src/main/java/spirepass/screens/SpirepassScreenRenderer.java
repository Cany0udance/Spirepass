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
import com.megacrit.cardcrawl.helpers.ImageMaster;
import spirepass.Spirepass;
import spirepass.elements.SpirepassLevelBox;
import spirepass.spirepassutil.SkinManager;
import spirepass.spirepassutil.SpirepassPositionSettings;
import spirepass.spirepassutil.SpirepassRewardData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SpirepassScreenRenderer {
    // Textures for level boxes
    private Texture backgroundTexture;
    private Texture levelBoxTexture;
    private Texture currentLevelBoxTexture;
    private Texture lockedLevelBoxTexture;
    // Add a map to cache custom background textures
    private Map<String, Texture> customBackgroundTextures = new HashMap<>();

    // UI elements
    private ModLabeledButton equipButton;

    // Component managers
    private SpirepassRewardManager rewardManager;
    private SpirepassAnimationManager animationManager;

    public SpirepassScreenRenderer() {
        // Load the textures
        try {
            this.backgroundTexture = ImageMaster.loadImage("spirepass/images/screen/SpirepassBackground.jpg");
            // Using default textures as placeholders - replace with your own
            this.levelBoxTexture = ImageMaster.OPTION_YES;
            this.currentLevelBoxTexture = ImageMaster.OPTION_YES;
            this.lockedLevelBoxTexture = ImageMaster.OPTION_NO;

            // Initialize component managers
            this.rewardManager = new SpirepassRewardManager();
            this.animationManager = new SpirepassAnimationManager();
        } catch (Exception e) {
            // Fallback in case images can't be loaded
            System.err.println("Failed to load SpirePass textures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void render(SpriteBatch sb, SpirepassScreen screen, float scrollX, float edgePadding, ArrayList<SpirepassLevelBox> levelBoxes) {
        // Get the current background texture
        String backgroundId = SkinManager.getInstance().getAppliedSkin(SkinManager.BACKGROUND_SCREEN);
        Texture currentBackground = this.backgroundTexture;

        // If a custom background is applied, get it from cache or load it
        if (backgroundId != null && !backgroundId.isEmpty()) {
            // Check if we already have this texture cached
            if (!customBackgroundTextures.containsKey(backgroundId)) {
                try {
                    // Load and cache the texture
                    Texture customTexture = ImageMaster.loadImage(backgroundId);
                    customBackgroundTextures.put(backgroundId, customTexture);
                } catch (Exception e) {
                    System.err.println("Failed to load custom background: " + backgroundId);
                    e.printStackTrace();
                }
            }

            // Get from cache if available
            if (customBackgroundTextures.containsKey(backgroundId)) {
                currentBackground = customBackgroundTextures.get(backgroundId);
            }
        }

        // Draw the background texture - THIS WAS MISSING
        if (currentBackground != null) {
            sb.setColor(Color.WHITE);
            sb.draw(
                    currentBackground,
                    0, 0,
                    0, 0,
                    Settings.WIDTH, Settings.HEIGHT,
                    1, 1,
                    0,
                    0, 0,
                    currentBackground.getWidth(), currentBackground.getHeight(),
                    false, false
            );
        }

        // Render title
        FontHelper.renderFontCentered(
                sb,
                FontHelper.bannerNameFont,
                "SPIREPASS",
                Settings.WIDTH / 2.0f,
                SpirepassPositionSettings.TITLE_Y,
                Color.WHITE
        );

        // Render level boxes
        renderLevelBoxes(sb, screen, scrollX, edgePadding, levelBoxes);
    }

    public void renderLevelProgressBar(SpriteBatch sb, int fromLevel, int toLevel, float xPos, float yPos, float width) {
        // Get current level
        int currentLevel = Spirepass.getCurrentLevel();

        // Skip rendering for future levels
        if (fromLevel > currentLevel) {
            return;
        }

        // Calculate progress percentage and determine color
        float progress = 0.0f;
        boolean isCompletedLevel = false;

        if (fromLevel == currentLevel) {
            // Current level progress
            int xpForNextLevel = Spirepass.getXPForNextLevel();
            int xpPerLevel = Spirepass.XP_PER_LEVEL;

            progress = 1.0f - ((float)xpForNextLevel / xpPerLevel);
            progress = MathUtils.clamp(progress, 0.0f, 1.0f);
        } else if (fromLevel < currentLevel) {
            // Completed level
            progress = 1.0f;
            isCompletedLevel = true;
        }

        // Draw background
        sb.setColor(SpirepassPositionSettings.PROGRESS_BAR_BG_COLOR);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG,
                xPos,
                yPos,
                width,
                SpirepassPositionSettings.PROGRESS_BAR_HEIGHT);

        // Draw progress with appropriate color
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
        // Calculate which level boxes are currently visible
        int firstVisibleLevel = Math.max(0, (int) ((scrollX - edgePadding) / screen.getLevelBoxSpacing()) - 1);
        int lastVisibleLevel = Math.min(screen.getMaxLevel(), (int) ((scrollX + Settings.WIDTH - edgePadding) / screen.getLevelBoxSpacing()) + 1);

        // Render progress bars first
        for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
            if (i < levelBoxes.size() - 1) {
                SpirepassLevelBox currentBox = levelBoxes.get(i);
                SpirepassLevelBox nextBox = levelBoxes.get(i + 1);

                // Calculate progress bar position
                float barWidth = (nextBox.getX() - currentBox.getX() - currentBox.getBoxSize()) * (2.2f * Settings.scale);
                float barX = currentBox.getX() + (currentBox.getBoxSize() / 2) - (barWidth - (nextBox.getX() - currentBox.getX() - currentBox.getBoxSize())) / 2;

                // Apply the horizontal offset
                barX -= SpirepassPositionSettings.PROGRESS_BAR_X_OFFSET;
                float barY = currentBox.getY() - (currentBox.getBoxSize() / 2) - SpirepassPositionSettings.PROGRESS_BAR_Y_OFFSET;

                renderLevelProgressBar(sb, i, i + 1, barX, barY, barWidth);
            }
        }

        // Render level boxes on top
        for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
            if (i < levelBoxes.size()) {
                levelBoxes.get(i).render(sb);
            }
        }
    }

    public void renderSelectedLevelReward(SpriteBatch sb, SpirepassLevelBox selectedBox) {
        int level = selectedBox.getLevel();
        boolean isUnlocked = selectedBox.isUnlocked();

        // Delegate to reward manager with unlock status
        rewardManager.renderReward(sb, level, isUnlocked);

        // Only update and render the equip button if the reward is unlocked
        if (isUnlocked) {
            updateEquipButton(sb, selectedBox);
        } else {
            // If reward is locked, hide the equip button or update it to show "LOCKED"
            updateLockedEquipButton(sb);
        }
    }

    private void updateLockedEquipButton(SpriteBatch sb) {
        // Button properties for locked state
        String buttonText = "LOCKED";
        Color buttonColor = Color.GRAY;
        Color hoverColor = Color.DARK_GRAY;
        float buttonX = Settings.WIDTH / 2.0f - 80.0f;
        float buttonY = SpirepassPositionSettings.REWARD_BUTTON_Y - 25.0f;

        // Create consumer for button click - should do nothing for locked rewards
        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            // Play a "can't do that" sound when clicked
            CardCrawlGame.sound.play("UI_CLICK_2");
        };

        // Create or update button
        if (equipButton == null) {
            equipButton = new ModLabeledButton(buttonText, buttonX / Settings.scale, buttonY / Settings.scale,
                    buttonColor, hoverColor, null, clickConsumer);
        } else {
            equipButton.label = buttonText;
            equipButton.color = buttonColor;
            equipButton.colorHover = hoverColor;
            equipButton.set(buttonX / Settings.scale, buttonY / Settings.scale);

            // Update click handler using reflection
            try {
                Field clickField = ModLabeledButton.class.getDeclaredField("click");
                clickField.setAccessible(true);
                clickField.set(equipButton, clickConsumer);
            } catch (NoSuchFieldException | IllegalAccessException e) {
//                 BaseMod.logger.error("Failed to update button click handler: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Render the button
        if (equipButton != null) {
            equipButton.render(sb);
        }
    }

    // Modify updateEquipButton in SpirepassScreenRenderer to hide for TEXT rewards
    private void updateEquipButton(SpriteBatch sb, SpirepassLevelBox selectedBox) {
        boolean isUnlocked = selectedBox.isUnlocked();
        int level = selectedBox.getLevel();
        SpirepassRewardData reward = rewardManager.getRewardData(level);

        // Don't show the button for TEXT rewards
        if (reward != null && reward.getType() == SpirepassRewardData.RewardType.TEXT) {
            // Hide the button by setting it to null
            equipButton = null;
            return;
        }

        // Button properties
        boolean isEquipped = rewardManager.isRewardEquipped(reward);
        String buttonText = isUnlocked ? (isEquipped ? "UNEQUIP" : "EQUIP") : "LOCKED";
        Color buttonColor = isUnlocked ? (isEquipped ? Color.ORANGE : Color.WHITE) : Color.GRAY;
        Color hoverColor = isUnlocked ? (isEquipped ? Color.YELLOW : Color.GREEN) : Color.DARK_GRAY;
        float buttonX = Settings.WIDTH / 2.0f - 80.0f;
        float buttonY = SpirepassPositionSettings.REWARD_BUTTON_Y - 25.0f;

        // Create consumer for button click
        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            if (isUnlocked && reward != null) {
                rewardManager.toggleRewardEquipped(reward);
            }
        };

        // Create or update button
        if (equipButton == null) {
            equipButton = new ModLabeledButton(buttonText, buttonX / Settings.scale, buttonY / Settings.scale,
                    buttonColor, hoverColor, null, clickConsumer);
        } else {
            equipButton.label = buttonText;
            equipButton.color = buttonColor;
            equipButton.colorHover = hoverColor;
            equipButton.set(buttonX / Settings.scale, buttonY / Settings.scale);

            // Update click handler using reflection
            try {
                Field clickField = ModLabeledButton.class.getDeclaredField("click");
                clickField.setAccessible(true);
                clickField.set(equipButton, clickConsumer);
            } catch (NoSuchFieldException | IllegalAccessException e) {
//                 BaseMod.logger.error("Failed to update button click handler: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Render the button
        if (equipButton != null) {
            equipButton.render(sb);
        }
    }

    // Getters
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

    // Add a method to dispose of all textures
    public void dispose() {
        // Dispose any existing animation manager resources
        if (animationManager != null) {
            animationManager.dispose();
        }

        // Dispose of all cached background textures
        for (Texture texture : customBackgroundTextures.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        customBackgroundTextures.clear();
    }
}
