package spirepass.screens;

import basemod.BaseMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import spirepass.Spirepass;
import spirepass.elements.SpirepassLevelBox;
import spirepass.util.SpirepassPositionSettings;
import spirepass.util.SpirepassRewardData;

import java.util.ArrayList;
import java.util.HashMap;

public class SpirepassScreenRenderer {
    private Texture backgroundTexture;
    private Texture levelBoxTexture;
    private Texture currentLevelBoxTexture;
    private Texture lockedLevelBoxTexture;

    // Maps to hold our reward data and textures
    private HashMap<Integer, SpirepassRewardData> rewardData;
    private HashMap<Integer, Texture> rewardTextures;
    private HashMap<String, Texture> backgroundTextures;

    // Maps for entity previews - generalized for any entity type
    private HashMap<String, HashMap<String, AnimationState>> previewAnimations = new HashMap<>();
    private HashMap<String, HashMap<String, Skeleton>> previewSkeletons = new HashMap<>();
    private HashMap<String, HashMap<String, Boolean>> animationInitialized = new HashMap<>();
    private static SkeletonMeshRenderer skeletonMeshRenderer;
    private PolygonSpriteBatch polyBatch;

    private void initializeAnimationMaps() {
        previewAnimations.put(Spirepass.ENTITY_IRONCLAD, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_WATCHER, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_JAW_WORM, new HashMap<>());

        previewSkeletons.put(Spirepass.ENTITY_IRONCLAD, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_WATCHER, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_JAW_WORM, new HashMap<>());

        animationInitialized.put(Spirepass.ENTITY_IRONCLAD, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_WATCHER, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_JAW_WORM, new HashMap<>());
    }

    private void initializeAnimationPreview(String entityId, String variant) {
        // Skip if already initialized
        if (animationInitialized.get(entityId).getOrDefault(variant, false)) {
            return;
        }
        try {
            BaseMod.logger.info("Creating animation preview for " + entityId + " variant: " + variant);
            // Define animation paths based on variant and entity
            String atlasUrl, skeletonUrl;
            if (variant.equals("default")) {
                // Default game assets
                if (entityId.equals(Spirepass.ENTITY_IRONCLAD)) {
                    atlasUrl = "images/characters/ironclad/idle/skeleton.atlas";
                    skeletonUrl = "images/characters/ironclad/idle/skeleton.json";
                } else if (entityId.equals(Spirepass.ENTITY_WATCHER)) {
                    atlasUrl = "images/characters/watcher/idle/skeleton.atlas";
                    skeletonUrl = "images/characters/watcher/idle/skeleton.json";
                } else if (entityId.equals(Spirepass.ENTITY_JAW_WORM)) {
                    atlasUrl = "images/monsters/theBottom/jawWorm/skeleton.atlas";
                    skeletonUrl = "images/monsters/theBottom/jawWorm/skeleton.json";
                } else {
                    throw new Exception("Unknown entity type: " + entityId);
                }
            } else {
                // Skin asset paths
                String basePath = "";
                if (entityId.equals(Spirepass.ENTITY_IRONCLAD)) {
                    basePath = "spirepass/images/skins/ironclad/" + variant + "/";
                } else if (entityId.equals(Spirepass.ENTITY_WATCHER)) {
                    basePath = "spirepass/images/skins/watcher/" + variant + "/";
                } else if (entityId.equals(Spirepass.ENTITY_JAW_WORM)) {
                    basePath = "spirepass/images/skins/jaw_worm/" + variant + "/";
                }
                atlasUrl = basePath + "skeleton.atlas";
                skeletonUrl = basePath + "skeleton.json";
            }
            // Load the skeleton directly
            TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(atlasUrl));
            SkeletonJson json = new SkeletonJson(atlas);
            SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal(skeletonUrl));

            // Create skeleton
            Skeleton skeleton = new Skeleton(skeletonData);

            // Create animation state
            AnimationStateData stateData = new AnimationStateData(skeletonData);
            AnimationState state = new AnimationState(stateData);

            // Check available animations and determine the correct idle animation name
            String idleAnimation = null;
            BaseMod.logger.info("Available animations for " + entityId + ":");
            for (Animation anim : skeletonData.getAnimations()) {
                BaseMod.logger.info(" - " + anim.getName() + " (duration: " + anim.getDuration() + ")");

                // Look for idle animation with case-insensitive comparison
                if (anim.getName().equalsIgnoreCase("idle")) {
                    idleAnimation = anim.getName();
                }
            }

            // Set appropriate idle animation if found
            if (idleAnimation != null) {
                state.setAnimation(0, idleAnimation, true);

                // Set animation speed
                float scale = getScaleFactor(entityId);
                state.getCurrent(0).setTimeScale(0.6f * scale);

                // Store animation and skeleton
                previewAnimations.get(entityId).put(variant, state);
                previewSkeletons.get(entityId).put(variant, skeleton);
                animationInitialized.get(entityId).put(variant, true);
                BaseMod.logger.info(entityId + " animation preview for " + variant + " initialized successfully with animation: " + idleAnimation);
            } else {
                // If no idle animation was found, try to use the first available animation
                if (skeletonData.getAnimations().size > 0) {
                    String firstAnim = skeletonData.getAnimations().get(0).getName();
                    state.setAnimation(0, firstAnim, true);

                    float scale = getScaleFactor(entityId);
                    state.getCurrent(0).setTimeScale(0.6f * scale);

                    previewAnimations.get(entityId).put(variant, state);
                    previewSkeletons.get(entityId).put(variant, skeleton);
                    animationInitialized.get(entityId).put(variant, true);
                    BaseMod.logger.info(entityId + " animation preview for " + variant + " initialized with fallback animation: " + firstAnim);
                } else {
                    throw new Exception("No animations found in skeleton data");
                }
            }
        } catch (Exception e) {
            BaseMod.logger.error("Error initializing animation for " + entityId + "/" + variant + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void renderAnimationPreview(SpriteBatch sb, String entityId, String variant) {
        // Initialize if needed
        if (!animationInitialized.get(entityId).getOrDefault(variant, false)) {
            initializeAnimationPreview(entityId, variant);
        }

        AnimationState state = previewAnimations.get(entityId).get(variant);
        Skeleton skeleton = previewSkeletons.get(entityId).get(variant);

        if (state != null && skeleton != null) {
            try {
                // Update animation
                state.update(Gdx.graphics.getDeltaTime());
                state.apply(skeleton);

                // Position the skeleton
                skeleton.setPosition(
                        Settings.WIDTH / 2.0f,
                        SpirepassPositionSettings.REWARD_PREVIEW_Y - SpirepassPositionSettings.CHARACTER_MODEL_Y_OFFSET
                );

                // Apply scale based on entity type
                float scale = getScaleFactor(entityId);
                skeleton.getRootBone().setScale(scale, scale);

                // Update world transform after position and scale changes
                skeleton.updateWorldTransform();

                // Check if we're already inside a rendering block
                boolean batchWasDrawing = sb.isDrawing();

                // If the batch was actively drawing, we need to end it before switching renderers
                if (batchWasDrawing) {
                    sb.end();
                }

                // Configure and start our polygon batch
                polyBatch.setProjectionMatrix(sb.getProjectionMatrix());
                polyBatch.setTransformMatrix(sb.getTransformMatrix());
                polyBatch.begin();

                // Draw with our mesh renderer
                skeletonMeshRenderer.draw(polyBatch, skeleton);

                // End our polygon batch
                polyBatch.end();

                // If the original batch was drawing, restart it
                if (batchWasDrawing) {
                    sb.begin();
                }

            } catch (Exception e) {
                BaseMod.logger.error("Error rendering animation for " + entityId + "/" + variant + ": " + e.getMessage());
                e.printStackTrace();

                // Make sure we restart the main batch if there was an error
                if (!sb.isDrawing()) {
                    sb.begin();
                }

                renderFallbackText(sb, "Error Rendering Animation", Color.RED);
            }
        } else {
            renderFallbackText(sb, entityId + " " + variant + " Preview", Color.WHITE);
        }
    }

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

            // Initialize entity preview maps
            initializeAnimationMaps();

            // Load background textures
            loadBackgroundTextures();

            // Initialize reward data and textures
            initializeRewardData();
        } catch (Exception e) {
            // Fallback in case images can't be loaded
            System.err.println("Failed to load SpirePass textures: " + e.getMessage());
            e.printStackTrace();
        }
        this.polyBatch = new PolygonSpriteBatch();
        skeletonMeshRenderer = new SkeletonMeshRenderer();
        skeletonMeshRenderer.setPremultipliedAlpha(true);

    }


    private void loadBackgroundTextures() {
        // Load the background textures for different rarities (unchanged)
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
        // Level 1: Ironclad skin
        rewardData.put(1, new SpirepassRewardData(
                1,
                "MS Paint Ironclad",
                "A beautiful hand-drawn rendition of the Ironclad",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_IRONCLAD,
                "IRONCLAD"
        ));

        // Level 2: Weaponized 115 Ironclad skin
        rewardData.put(2, new SpirepassRewardData(
                2,
                "Weaponized 115 from the hit game Call of Duty®: Black Ops II",
                "Ironclad with a radioactive glow and questionable side effects",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_IRONCLAD,
                "IRONCLAD_WEAPONIZED115"
        ));

        // Level 3: Invisible Man Ironclad skin
        rewardData.put(3, new SpirepassRewardData(
                3,
                "Invisible Man",
                "You can't see him, but the enemies still can",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_IRONCLAD,
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

        // Add Watcher skin
        rewardData.put(5, new SpirepassRewardData(
                5,
                "Sickly Watcher",
                "A Watcher that's seen better days",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_WATCHER,
                "WATCHER_SICKLY"
        ));

        // Add Jaw Worm skin
        rewardData.put(6, new SpirepassRewardData(
                6,
                "Bloodied Jaw Worm",
                "A Jaw Worm that's taken a few hits",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_JAW_WORM,
                "JAW_WORM_BLOODIED"
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
                String entityId = reward.getEntityId();

                // Get the variant from the model ID
                String variant = getVariantFromModelId(entityId, modelId);

                // Render the appropriate entity preview
                renderAnimationPreview(sb, entityId, variant);
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
            // Try to guess what kind of reward this might be based on the level
            if (level == 1) {
                // Default to Ironclad preview for level 1
                renderAnimationPreview(sb, Spirepass.ENTITY_IRONCLAD, "default");
            } else if (level == 5) {
                // Default to Watcher preview for level 5
                renderAnimationPreview(sb, Spirepass.ENTITY_WATCHER, "default");
            } else if (level == 6) {
                // Default to Jaw Worm preview for level 6
                renderAnimationPreview(sb, Spirepass.ENTITY_JAW_WORM, "default");
            } else if (getRewardTexture(level) != null) {
                // Otherwise show the texture if available
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
                            SpirepassPositionSettings.REWARD_PREVIEW_Y - previewHeight / 2.0f,
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


    private String getVariantFromModelId(String entityId, String modelId) {
        if (entityId.equals(Spirepass.ENTITY_IRONCLAD)) {
            if (modelId.equals("IRONCLAD")) {
                return "default";
            } else if (modelId.startsWith("IRONCLAD_")) {
                return modelId.substring("IRONCLAD_".length()).toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_WATCHER)) {
            if (modelId.equals("WATCHER")) {
                return "default";
            } else if (modelId.startsWith("WATCHER_")) {
                return modelId.substring("WATCHER_".length()).toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_JAW_WORM)) {
            if (modelId.equals("JAW_WORM")) {
                return "default";
            } else if (modelId.startsWith("JAW_WORM_")) {
                return modelId.substring("JAW_WORM_".length()).toLowerCase();
            }
        }

        return "default"; // fallback
    }

    // Get the scale factor for different entity types
    private float getScaleFactor(String entityId) {
        // Different entities might need different scaling
        if (entityId.equals(Spirepass.ENTITY_IRONCLAD) ||
                entityId.equals(Spirepass.ENTITY_WATCHER)) {
            return SpirepassPositionSettings.CHARACTER_MODEL_SCALE;
        } else if (entityId.equals(Spirepass.ENTITY_JAW_WORM)) {
            return SpirepassPositionSettings.MONSTER_MODEL_SCALE;
        }
        return SpirepassPositionSettings.CHARACTER_MODEL_SCALE; // Default
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

    public void dispose() {
        if (polyBatch != null) {
            polyBatch.dispose();
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