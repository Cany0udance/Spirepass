package spirepass.screens;

import basemod.BaseMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import spirepass.elements.SpirepassLevelBox;
import spirepass.util.SpirepassPositionSettings;
import spirepass.util.SpirepassRewardData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import static spirepass.util.SpirepassPositionSettings.REWARD_PREVIEW_Y;

public class SpirepassScreenRenderer {
    private Texture backgroundTexture;
    private Texture levelBoxTexture;
    private Texture currentLevelBoxTexture;
    private Texture lockedLevelBoxTexture;

    // Maps to hold our reward data and textures
    private HashMap<Integer, SpirepassRewardData> rewardData;
    private HashMap<Integer, Texture> rewardTextures;
    private HashMap<String, Texture> backgroundTextures;

    // Import the position settings
    // Use constants from position settings class rather than defining them here
    // This allows for easy adjustment from one place

    private AbstractPlayer scaledIronclad = null;
    private boolean ironCladInitialized = false;
    private float originalAnimationTime = 0f;
    private float customTimeScale = 0.5f; // Adjust this value to control animation speed (lower = slower)
    private long lastRenderTime = 0L;
    private float accumulatedTime = 0f;
    private boolean ironcladPreviewInitialized = false;
    private AbstractPlayer previewIronclad = null;
    private HashMap<String, AbstractPlayer> ironcladModels = new HashMap<>();
    private HashMap<String, Boolean> ironcladInitialized = new HashMap<>();

    public SpirepassScreenRenderer() {
        // Load the textures
        try {
            this.backgroundTexture = ImageMaster.loadImage("spirepass/images/screen/SpirepassBackground.jpg");
            // Using default textures as placeholders - replace with your own
            this.levelBoxTexture = ImageMaster.OPTION_YES;
            this.currentLevelBoxTexture = ImageMaster.OPTION_YES;
            this.lockedLevelBoxTexture = ImageMaster.OPTION_NO;

            // Initialize our structures
            this.rewardData = new HashMap<>();
            this.rewardTextures = new HashMap<>();
            this.backgroundTextures = new HashMap<>();

            // Load background textures
            loadBackgroundTextures();

            // Initialize reward data and textures
            initializeRewardData();
        } catch (Exception e) {
            // Fallback in case images can't be loaded
            System.err.println("Failed to load SpirePass textures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBackgroundTextures() {
        // Load the background textures for different rarities
        String[] paths = {
                "spirepass/images/screen/CommonRewardBackground.png",
                "spirepass/images/screen/UncommonRewardBackground.png",
                "spirepass/images/screen/RareRewardBackground.png"
        };

        for (String path : paths) {
            try {
                backgroundTextures.put(path, ImageMaster.loadImage(path));
            } catch (Exception e) {
                System.err.println("Failed to load background texture: " + path);
            }
        }
    }

    private void initializeRewardData() {
        // Level 1: Ironclad skin (using character model)
        rewardData.put(1, new SpirepassRewardData(
                1,
                "MS Paint Ironclad",
                "A beautiful hand-drawn rendition of the Ironclad",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                "IRONCLAD"
        ));

        // Level 2: Weaponized 115 Ironclad skin
        rewardData.put(2, new SpirepassRewardData(
                2,
                "Weaponized 115 from the hit game Call of Duty®: Black Ops II",
                "Ironclad with a radioactive glow and questionable side effects",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                "IRONCLAD_WEAPONIZED115"
        ));

        // Level 3: Invisible Man Ironclad skin
        rewardData.put(3, new SpirepassRewardData(
                3,
                "Invisible Man",
                "You can't see him, but the enemies still can",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                "IRONCLAD_INVISIBLEMAN"
        ));

        // Level 4: Cyan colorless cardback (image)
        rewardData.put(4, new SpirepassRewardData(
                4,
                "Cyan Colorless Cardback",
                "Slightly off-center blue cardback",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.IMAGE,
                "spirepass/images/rewards/cardbacks/colorless/CyanColorlessCardbackReward.png"
        ));

        // Default reward for all other levels (badge image)
        Texture badgeTexture = ImageMaster.loadImage("spirepass/images/badge.png");

        // Load the image textures for any image-type rewards
        for (Integer level : rewardData.keySet()) {
            SpirepassRewardData data = rewardData.get(level);
            if (data.getType() == SpirepassRewardData.RewardType.IMAGE && data.getImagePath() != null) {
                try {
                    rewardTextures.put(level, ImageMaster.loadImage(data.getImagePath()));
                } catch (Exception e) {
                    System.err.println("Failed to load reward texture for level " + level);
                }
            }
        }

        // For any unspecified levels, use the badge texture as a fallback
        for (int i = 0; i <= 30; i++) {
            if (!rewardTextures.containsKey(i) && !rewardData.containsKey(i)) {
                rewardTextures.put(i, badgeTexture);
            }
        }
    }

    public Texture getRewardTexture(int level) {
        if (rewardTextures.containsKey(level)) {
            return rewardTextures.get(level);
        } else {
            // Return the badge texture as default if level not found
            for (Integer key : rewardTextures.keySet()) {
                return rewardTextures.get(key);  // Return the first one we find
            }
            return null;  // Fallback
        }
    }

    public SpirepassRewardData getRewardData(int level) {
        return rewardData.getOrDefault(level, null);
    }

    public void render(SpriteBatch sb, SpirepassScreen screen, float scrollX, float edgePadding, ArrayList<SpirepassLevelBox> levelBoxes) {
        // Render the background image to fill the entire screen
        if (this.backgroundTexture != null) {
            sb.setColor(Color.WHITE);
            sb.draw(
                    this.backgroundTexture,
                    0, 0,
                    0, 0,
                    Settings.WIDTH, Settings.HEIGHT,
                    1, 1,
                    0,
                    0, 0,
                    this.backgroundTexture.getWidth(), this.backgroundTexture.getHeight(),
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

        // Render the cancel button on top of everything
        screen.cancelButton.render(sb);
    }

    public void renderSelectedLevelReward(SpriteBatch sb, SpirepassLevelBox selectedBox) {
        int level = selectedBox.getLevel();
        boolean isUnlocked = selectedBox.isUnlocked();
        SpirepassRewardData reward = getRewardData(level);

        // If we have specific reward data, use it
        if (reward != null) {
            // Get the background texture path
            String backgroundPath = reward.getBackgroundTexturePath();
            Texture backgroundTexture = backgroundPath != null ? backgroundTextures.get(backgroundPath) : null;

            // Render the background if available
            if (backgroundTexture != null) {
                // Apply the single background scale factor
                float previewHeight = SpirepassPositionSettings.REWARD_PREVIEW_HEIGHT * SpirepassPositionSettings.REWARD_BACKGROUND_SCALE;
                float previewWidth = previewHeight * (backgroundTexture.getWidth() / (float) backgroundTexture.getHeight());

                sb.setColor(Color.WHITE);
                sb.draw(
                        backgroundTexture,
                        Settings.WIDTH / 2.0f - previewWidth / 2.0f,
                        SpirepassPositionSettings.REWARD_PREVIEW_Y - previewHeight / 2.0f,
                        previewWidth,
                        previewHeight
                );
            }

            // Render reward preview based on type
            if (reward.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL) {
                String modelId = reward.getModelId();
                renderIroncladPreview(sb, getVariantFromModelId(modelId));
            } else if (reward.getType() == SpirepassRewardData.RewardType.IMAGE) {
                Texture rewardTexture = getRewardTexture(level);
                if (rewardTexture != null) {
                    // Apply the single content scale factor
                    float previewHeight = SpirepassPositionSettings.REWARD_IMAGE_HEIGHT * SpirepassPositionSettings.REWARD_CONTENT_SCALE;

                    // Calculate width based on the texture's aspect ratio
                    float aspectRatio = rewardTexture.getWidth() / (float) rewardTexture.getHeight();
                    float previewWidth = previewHeight * aspectRatio;

                    sb.setColor(Color.WHITE);
                    sb.draw(
                            rewardTexture,
                            Settings.WIDTH / 2.0f - previewWidth / 2.0f,
                            SpirepassPositionSettings.REWARD_PREVIEW_Y - previewHeight / 2.0f,
                            previewWidth,
                            previewHeight
                    );
                }
            }

            // Render reward title
            FontHelper.renderFontCentered(
                    sb,
                    FontHelper.tipBodyFont,
                    reward.getName(),
                    Settings.WIDTH / 2.0f,
                    SpirepassPositionSettings.REWARD_NAME_Y,
                    Color.WHITE
            );

            // Render description
            FontHelper.renderFontCentered(
                    sb,
                    FontHelper.tipBodyFont,
                    reward.getDescription(),
                    Settings.WIDTH / 2.0f,
                    SpirepassPositionSettings.REWARD_DESCRIPTION_Y,
                    Color.LIGHT_GRAY
            );
        } else {
            // Fallback for levels without specific reward data
            // Use the original rendering code for reward preview
            if (level == 1) {
                renderIroncladPreview(sb, "default");
            } else if (getRewardTexture(level) != null) {
                Texture rewardTexture = getRewardTexture(level);
                if (rewardTexture != null) {
                    // Set desired height for all reward images
                    float previewHeight = 200.0f * Settings.scale;

                    // Calculate width based on the texture's aspect ratio
                    float aspectRatio = rewardTexture.getWidth() / (float)rewardTexture.getHeight();
                    float previewWidth = previewHeight * aspectRatio;

                    sb.setColor(Color.WHITE);
                    sb.draw(
                            rewardTexture,
                            Settings.WIDTH / 2.0f - previewWidth / 2.0f,
                            REWARD_PREVIEW_Y - previewHeight / 2.0f,
                            previewWidth,
                            previewHeight
                    );
                }
            }

            // Render generic title
            FontHelper.renderFontCentered(
                    sb,
                    FontHelper.tipBodyFont,
                    "Level " + level + " Reward",
                    Settings.WIDTH / 2.0f,
                    SpirepassPositionSettings.REWARD_NAME_Y,
                    Color.WHITE
            );
        }

        // Render the equip button
        float buttonWidth = SpirepassPositionSettings.BUTTON_WIDTH;
        float buttonHeight = SpirepassPositionSettings.BUTTON_HEIGHT;

        sb.setColor(isUnlocked ? Color.WHITE : Color.GRAY);
        sb.draw(
                ImageMaster.CANCEL_BUTTON,
                Settings.WIDTH / 2.0f - buttonWidth / 2.0f,
                SpirepassPositionSettings.REWARD_BUTTON_Y - buttonHeight / 2.0f,
                buttonWidth,
                buttonHeight
        );

        // Render button text
        FontHelper.renderFontCentered(
                sb,
                FontHelper.buttonLabelFont,
                isUnlocked ? "EQUIP" : "LOCKED",
                Settings.WIDTH / 2.0f,
                SpirepassPositionSettings.REWARD_BUTTON_Y,
                isUnlocked ? Color.WHITE : Color.DARK_GRAY
        );
    }

    private String getVariantFromModelId(String modelId) {
        if (modelId.equals("IRONCLAD")) {
            return "default";
        } else if (modelId.startsWith("IRONCLAD_")) {
            return modelId.substring("IRONCLAD_".length()).toLowerCase();
        }
        return "default"; // fallback
    }

    private void initializeIroncladPreview(String variant) {
        if (ironcladInitialized.getOrDefault(variant, false)) {
            return; // Already initialized
        }

        try {
            BaseMod.logger.info("Creating scaled Ironclad preview model for variant: " + variant);

            // Create a new Ironclad instance via reflection since the constructor is package-private
            Constructor<?> constructor = Class.forName("com.megacrit.cardcrawl.characters.Ironclad").getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            AbstractPlayer variantIronclad = (AbstractPlayer) constructor.newInstance("PreviewIronclad_" + variant);

            // Define animation paths based on the variant
            String atlasUrl, skeletonUrl;

            if (variant.equals("default")) {
                atlasUrl = "images/characters/ironclad/idle/skeleton.atlas";
                skeletonUrl = "images/characters/ironclad/idle/skeleton.json";
            } else {
                atlasUrl = "spirepass/images/skins/ironclad/" + variant + "/skeleton.atlas";
                skeletonUrl = "spirepass/images/skins/ironclad/" + variant + "/skeleton.json";
            }

            // Load animation with scaled parameter
            Method loadAnimationMethod = findMethod(AbstractCreature.class, "loadAnimation",
                    String.class, String.class, float.class);
            loadAnimationMethod.setAccessible(true);

            // Calculate the scale ratio
            float scaleRatio = 1.0f / SpirepassPositionSettings.CHARACTER_MODEL_SCALE;
            loadAnimationMethod.invoke(variantIronclad, atlasUrl, skeletonUrl, scaleRatio);

            // Set the animation to Idle and loop it
            Field stateField = AbstractCreature.class.getDeclaredField("state");
            stateField.setAccessible(true);
            Object state = stateField.get(variantIronclad);

            Method setAnimationMethod = state.getClass().getMethod("setAnimation", int.class, String.class, boolean.class);
            setAnimationMethod.invoke(state, 0, "Idle", true);

            // Make the animation play at the right speed
            Method getAnimationMethod = state.getClass().getMethod("getCurrent", int.class);
            Object trackEntry = getAnimationMethod.invoke(state, 0);

            // Apply time scaling similar to the original constructor
            if (trackEntry != null) {
                Method setTimeScaleMethod = trackEntry.getClass().getMethod("setTimeScale", float.class);
                // Adjust time scale to compensate for the model scaling
                float adjustedTimeScale = 0.6f * scaleRatio;
                setTimeScaleMethod.invoke(trackEntry, adjustedTimeScale);
            }

            // Store the initialized model
            ironcladModels.put(variant, variantIronclad);
            ironcladInitialized.put(variant, true);

            BaseMod.logger.info("Ironclad preview model for " + variant + " initialized successfully");
        } catch (Exception e) {
            BaseMod.logger.error("Error initializing Ironclad preview for " + variant + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderIroncladPreview(SpriteBatch sb, String variant) {
        // Initialize preview model if needed
        if (!ironcladInitialized.getOrDefault(variant, false)) {
            initializeIroncladPreview(variant);
        }

        AbstractPlayer modelToRender = ironcladModels.get(variant);

        if (modelToRender != null) {
            try {
                // Position the model at the center of the reward preview area
                modelToRender.drawX = Settings.WIDTH / 2.0f;
                modelToRender.drawY = SpirepassPositionSettings.REWARD_PREVIEW_Y - SpirepassPositionSettings.CHARACTER_MODEL_Y_OFFSET;

                // Update the animation state
                Field stateField = AbstractCreature.class.getDeclaredField("state");
                stateField.setAccessible(true);
                Object state = stateField.get(modelToRender);

                Method updateMethod = state.getClass().getMethod("update", float.class);
                updateMethod.invoke(state, Gdx.graphics.getDeltaTime());

                // Render the scaled Ironclad
                modelToRender.renderPlayerImage(sb);
            } catch (Exception e) {
                BaseMod.logger.error("Error rendering Ironclad preview for " + variant + ": " + e.getMessage());
                // Fallback if rendering fails
                renderFallbackText(sb, "Error Rendering Ironclad Preview", Color.RED);
            }
        } else {
            // Fallback if no preview model
            renderFallbackText(sb, variant + " Ironclad Skin Preview", Color.WHITE);
        }
    }

    private void renderFallbackText(SpriteBatch sb, String text, Color color) {
        FontHelper.renderFontCentered(
                sb,
                FontHelper.tipBodyFont,
                text,
                Settings.WIDTH / 2.0f,
                SpirepassPositionSettings.REWARD_PREVIEW_Y,
                color
        );
    }


    // Helper method to find methods in class hierarchy
    private Method findMethod(Class clz, String methodName, Class... parameterTypes) throws NoSuchMethodException {
        try {
            return clz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException var5) {
            Class superClass = clz.getSuperclass();
            if (superClass == null) {
                throw var5;
            } else {
                return findMethod(superClass, methodName, parameterTypes);
            }
        }
    }

    private void renderLevelBoxes(SpriteBatch sb, SpirepassScreen screen, float scrollX, float edgePadding, ArrayList<SpirepassLevelBox> levelBoxes) {
        // Calculate which level boxes are currently visible
        int firstVisibleLevel = Math.max(0, (int)((scrollX - edgePadding) / screen.getLevelBoxSpacing()) - 1);
        int lastVisibleLevel = Math.min(screen.getMaxLevel(), (int)((scrollX + Settings.WIDTH - edgePadding) / screen.getLevelBoxSpacing()) + 1);

        // Render each visible level box
        for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
            if (i < levelBoxes.size()) {
                levelBoxes.get(i).render(sb);
            }
        }
    }

    // Getters for textures
    public Texture getLevelBoxTexture() {
        return levelBoxTexture;
    }

    public Texture getCurrentLevelBoxTexture() {
        return currentLevelBoxTexture;
    }

    public Texture getLockedLevelBoxTexture() {
        return lockedLevelBoxTexture;
    }
}