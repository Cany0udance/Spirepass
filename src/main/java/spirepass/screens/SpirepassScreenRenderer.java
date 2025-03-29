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
    private static Float cachedBoxW = null;
    private static final float TOOLTIP_RIGHT_PADDING = 15.0f * Settings.scale;
    private static final float EQUIP_BUTTON_PADDING = 80.0f * Settings.scale;
    private static final float PREMIUM_BUTTON_Y = 40.0f * Settings.scale;
    private static final String PREMIUM_BUTTON_TEXT = "BUY SPIREPASS PREMIUM@!!!1!";
    private static final String PREMIUM_TOOLTIP_HEADER = "Why buy Premium? Great question. Here are some great answers:";
    private static final String PREMIUM_TOOLTIP_BODY = "- Unlock microtransactions (your favorite!) NL NL - Unlock macrotransactions NL NL - Unlock SBMM NL NL - Unlock the krabby patty secret formula NL NL - Unlock more unlocks " +
            "NL NL - Unlock an embarrassing picture of Defect at the Christmas party NL NL - Unlock a new synonym for the word \"Unlock\". i guess they call that a thesaurus hold on let me buy one from amazon EDIT: hey guys it's been 4 business days and i'm back with a brand new thesauraus NL NL - UnLEASH new...cards? Depends on your definition of cards NL NL - Activate hidden neurons inside your brain that" +
            "you didn't even know existed for maximum spireslaying 1000 NL NL - Unlock beta access to Half-Life 3 (note to future Spirepass enjoyers: if Half-Life 3 actually releases (it won't lol), this joke becomes a bit outdated so yo ucan safely ignore this" +
            "entire bullet point thanks! NL NL - Learn why kids love the taste of Cinnamon Toast Crunch NL NL - Learn why kids don't lvoe the taste of Cinnamon Toast Squares NL NL - Learn why adults hate that kids love the taste of Cinnamon Toast Crunch";
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
        float textWidth = FontHelper.getSmartWidth(FontHelper.buttonLabelFont, PREMIUM_BUTTON_TEXT, 9999f, 0f);
        float estimatedButtonWidth = textWidth + 80.0f * Settings.scale;
        float buttonX = (Settings.WIDTH / 2.0f) - (estimatedButtonWidth / 2.0f);
        float buttonY = PREMIUM_BUTTON_Y;

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
                PREMIUM_BUTTON_TEXT,
                buttonX,
                buttonY,
                Color.YELLOW,
                Color.GOLD,
                FontHelper.buttonLabelFont,
                null,
                clickConsumer
        );

        try {
            Field hbField = ModLabeledButton.class.getDeclaredField("hb");
            hbField.setAccessible(true);
            Hitbox buttonHb = (Hitbox) hbField.get(this.premiumButton);
            if (buttonHb != null && buttonHb.width > 0) {
                float actualButtonWidth = buttonHb.width;
                float correctButtonX = (Settings.WIDTH / 2.0f) - (actualButtonWidth / 2.0f);
                this.premiumButton.set(correctButtonX, buttonY);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed reflection for premium button hitbox adjustment: " + e.getMessage());
        }
    }

    // ==================== UPDATE & RENDER METHODS ====================

    public void updateUI() {
        if (equipButton != null) {
            equipButton.update();
        }
        if (premiumButton != null) {
            premiumButton.update();
        }
    }

    public void renderUIElements(SpriteBatch sb) {
        if (premiumButton != null) {
            premiumButton.render(sb);
        }
        renderPremiumTooltip();
    }

    private void renderPremiumTooltip() {
        if (premiumButton == null) return;
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
                float tipX = Settings.WIDTH * 0.75f;
                tipX = Math.min(tipX, Settings.WIDTH - boxWidth - TOOLTIP_RIGHT_PADDING);
                tipX = Math.max(tipX, TOOLTIP_RIGHT_PADDING);
                float tipY = Settings.HEIGHT - 50.0f * Settings.scale;
                TipHelper.renderGenericTip(tipX, tipY, PREMIUM_TOOLTIP_HEADER, PREMIUM_TOOLTIP_BODY);
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

        FontHelper.renderFontCentered(sb, FontHelper.bannerNameFont, uiStrings.TEXT[0],
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
        String buttonText = uiStrings.TEXT[3];
        Color buttonColor = Color.GRAY;
        Color hoverColor = Color.DARK_GRAY;
        float buttonY = SpirepassPositionSettings.REWARD_BUTTON_Y - 25.0f;

        float textWidth = FontHelper.getSmartWidth(FontHelper.buttonLabelFont, buttonText, 9999f, 0f);
        float estimatedButtonWidth = textWidth + EQUIP_BUTTON_PADDING;
        float buttonX = (Settings.WIDTH / 2.0f) - (estimatedButtonWidth / 2.0f);

        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            CardCrawlGame.sound.play("UI_CLICK_2");
        };

        if (equipButton == null) {
            equipButton = new ModLabeledButton(buttonText, buttonX, buttonY,
                    buttonColor, hoverColor, FontHelper.buttonLabelFont, null, clickConsumer);
        } else {
            equipButton.label = buttonText;
            equipButton.color = buttonColor;
            equipButton.colorHover = hoverColor;
            equipButton.set(buttonX, buttonY);
            try {
                Field clickField = ModLabeledButton.class.getDeclaredField("click");
                clickField.setAccessible(true);
                clickField.set(equipButton, clickConsumer);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        adjustButtonCentering(equipButton, buttonY);
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

        String buttonText = isEquipped ? uiStrings.TEXT[2] : uiStrings.TEXT[1];
        Color buttonColor = isEquipped ? Color.ORANGE : Color.WHITE;
        Color hoverColor = isEquipped ? Color.YELLOW : Color.GREEN;
        float buttonY = SpirepassPositionSettings.REWARD_BUTTON_Y - 25.0f;

        float textWidth = FontHelper.getSmartWidth(FontHelper.buttonLabelFont, buttonText, 9999f, 0f);
        float estimatedButtonWidth = textWidth + EQUIP_BUTTON_PADDING;
        float buttonX = (Settings.WIDTH / 2.0f) - (estimatedButtonWidth / 2.0f);

        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            if (isUnlocked && reward != null) {
                rewardManager.toggleRewardEquipped(reward);
                CardCrawlGame.sound.play("UI_CLICK_1");
            } else {
                CardCrawlGame.sound.play("UI_CLICK_2");
            }
        };

        if (equipButton == null) {
            equipButton = new ModLabeledButton(buttonText, buttonX, buttonY,
                    buttonColor, hoverColor, FontHelper.buttonLabelFont, null, clickConsumer);
        } else {
            equipButton.label = buttonText;
            equipButton.color = buttonColor;
            equipButton.colorHover = hoverColor;
            equipButton.set(buttonX, buttonY);
            try {
                Field clickField = ModLabeledButton.class.getDeclaredField("click");
                clickField.setAccessible(true);
                clickField.set(equipButton, clickConsumer);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        adjustButtonCentering(equipButton, buttonY);
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