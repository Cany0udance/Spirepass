package spirepass.screens;

import basemod.BaseMod;
import basemod.ModLabeledButton;
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
import spirepass.spirepassutil.SpirepassPositionSettings;
import spirepass.spirepassutil.SpirepassRewardData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class SpirepassScreenRenderer {
    // ==================== TEXTURES ====================
    private Texture backgroundTexture;
    private Texture levelBoxTexture;
    private Texture currentLevelBoxTexture;
    private Texture lockedLevelBoxTexture;

    // ==================== DATA STRUCTURES ====================
    // Maps to hold our reward data and textures
    private HashMap<Integer, SpirepassRewardData> rewardData;
    private HashMap<Integer, Texture> rewardTextures;
    private HashMap<String, Texture> backgroundTextures;

    // ==================== ANIMATION RELATED ====================
    // Maps for entity previews - generalized for any entity type
    private HashMap<String, HashMap<String, AnimationState>> previewAnimations = new HashMap<>();
    private HashMap<String, HashMap<String, Skeleton>> previewSkeletons = new HashMap<>();
    private HashMap<String, HashMap<String, Boolean>> animationInitialized = new HashMap<>();
    private static SkeletonMeshRenderer skeletonMeshRenderer;
    private PolygonSpriteBatch polyBatch;

    // ==================== UI ELEMENTS ====================
    private ModLabeledButton equipButton;

    // ==================== INITIALIZATION ====================

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
        // Level 1: Defect skin
        rewardData.put(1, new SpirepassRewardData(
                1,
                "avwejkanaklwjv e",
                "yeah",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_DEFECT,
                "DEFECT_GARBLE"
        ));

        // Level 2: Weaponized 115 Ironclad skin
        rewardData.put(2, new SpirepassRewardData(
                2,
                "Weaponized 115 from the hit game Call of Duty®: Black Ops II",
                "The Weaponized 115 Personalization Pack brings out the power of Zombies to any Multiplayer match. Pack your weapons with an all-new Call of Duty: Black Ops II Origins-inspired camo, set of reticles, and undead animated calling card.",
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

        // Level 4: Sponsored colorless cardback
        rewardData.put(4, new SpirepassRewardData(
                4,
                "Sponsored",
                "Legal note: this cardback does not represent any official affiliation with a brand.",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                Spirepass.CARDBACK_COLORLESS,
                "COLORLESS_SPONSORED",
                "spirepass/images/rewards/cardbacks/colorless/sponsored/RAIDSkillLarge.png"
        ));

        // Add Watcher skin
        rewardData.put(5, new SpirepassRewardData(
                5,
                "kris deltarune",
                "\"W...Watcher? Are you OK? You're yelling...\"",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_WATCHER,
                "WATCHER_DREEMURR"
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

        // Add Harold Curse cardback at level 7
        rewardData.put(7, new SpirepassRewardData(
                7,
                "Harold Curse Cardback",
                ":)",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                Spirepass.CARDBACK_CURSE,
                "CURSE_HAROLD",
                "spirepass/images/rewards/cardbacks/curse/HaroldLarge.png"
        ));

        rewardData.put(8, new SpirepassRewardData(
                8,
                "Big Bird",
                "he big",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_AWAKENED_ONE,
                "AWAKENED_ONE_BIGBIRD"
        ));

        rewardData.put(9, new SpirepassRewardData(
                9,
                "Blue",
                "he's blue, da ba dee da ba di",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_RED_SLAVER,
                "RED_SLAVER_BLUE"
        ));

        rewardData.put(10, new SpirepassRewardData(
                10,
                "Red",
                "We have red slaver at home",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_BLUE_SLAVER,
                "BLUE_SLAVER_RED"
        ));

        rewardData.put(11, new SpirepassRewardData(
                11,
                "Disarmed",
                "It's like that one beta art",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_CULTIST,
                "CULTIST_DISARMED"
        ));

        rewardData.put(12, new SpirepassRewardData(
                12,
                "Law Abiding Citizen",
                "Deal 3 damage. Increase the damage of ALL Claw cards by 2 this combat.",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_DEFECT,
                "DEFECT_LAWABIDINGCITIZEN"
        ));

        rewardData.put(13, new SpirepassRewardData(
                13,
                "Bear",
                "Somehow, the rest of the Red Mask Gang still haven't noticed.",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_BEAR,
                "BEAR_ACTUALBEAR"
        ));

        rewardData.put(14, new SpirepassRewardData(
                14,
                "Spaghetti & Meatballs",
                "Comes with a side of Parasites.",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_WRITHING_MASS,
                "WRITHING_MASS_SPAGHETTI"
        ));

        rewardData.put(15, new SpirepassRewardData(
                15,
                "Giantama",
                "\"No\"",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_GIANT_HEAD,
                "GIANT_HEAD_GIANTAMA"
        ));

        rewardData.put(16, new SpirepassRewardData(
                16,
                "Blurry",
                "where are my glasses??",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_SILENT,
                "SILENT_BLURRY"
        ));

        rewardData.put(17, new SpirepassRewardData(
                17,
                "Space Blanket",
                "Are you telling me that my RNG just happens to be so unlucky? No! He orchestrated it! Casey!!!",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_SILENT,
                "SILENT_SPACEBLANKET"
        ));

        rewardData.put(18, new SpirepassRewardData(
                18,
                "Pajama Sam",
                "The Darkness orbs are really starting to get to me.",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_DEFECT,
                "DEFECT_PAJAMASAM"
        ));

        rewardData.put(19, new SpirepassRewardData(
                19,
                "Urban Legend",
                "Pages 3/8",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                Spirepass.ENTITY_GREMLIN_NOB,
                "GREMLIN_NOB_URBANLEGEND"
        ));

        rewardData.put(20, new SpirepassRewardData(
                20,
                "Favorite Customer",
                "Do you like this cardback? It's not for sale",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                Spirepass.CARDBACK_COLORLESS,
                "COLORLESS_FAVORITECUSTOMER",
                "spirepass/images/rewards/cardbacks/colorless/favoritecustomer/FavoriteCustomerSkillLarge.png"
        ));

        // Default reward for all other levels (badge image)
        Texture badgeTexture = ImageMaster.loadImage("spirepass/images/badge.png");

        // Load the image textures for any image-type or cardback-type rewards
        for (Integer level : rewardData.keySet()) {
            SpirepassRewardData data = rewardData.get(level);
            if (data.getImagePath() != null) {
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

    // ==================== ANIMATION MANAGEMENT ====================

    private void initializeAnimationMaps() {
        previewAnimations.put(Spirepass.ENTITY_IRONCLAD, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_SILENT, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_DEFECT, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_WATCHER, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_JAW_WORM, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_CULTIST, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_BLUE_SLAVER, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_RED_SLAVER, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_GREMLIN_NOB, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_BEAR, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_WRITHING_MASS, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_GIANT_HEAD, new HashMap<>());
        previewAnimations.put(Spirepass.ENTITY_AWAKENED_ONE, new HashMap<>());

        previewSkeletons.put(Spirepass.ENTITY_IRONCLAD, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_SILENT, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_DEFECT, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_WATCHER, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_JAW_WORM, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_CULTIST, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_BLUE_SLAVER, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_RED_SLAVER, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_GREMLIN_NOB, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_BEAR, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_WRITHING_MASS, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_GIANT_HEAD, new HashMap<>());
        previewSkeletons.put(Spirepass.ENTITY_AWAKENED_ONE, new HashMap<>());

        animationInitialized.put(Spirepass.ENTITY_IRONCLAD, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_SILENT, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_DEFECT, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_WATCHER, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_JAW_WORM, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_CULTIST, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_BLUE_SLAVER, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_RED_SLAVER, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_GREMLIN_NOB, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_BEAR, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_WRITHING_MASS, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_GIANT_HEAD, new HashMap<>());
        animationInitialized.put(Spirepass.ENTITY_AWAKENED_ONE, new HashMap<>());
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

            // Always use skin asset paths, never default
            String basePath = "";
            if (entityId.equals(Spirepass.ENTITY_IRONCLAD)) {
                basePath = "spirepass/images/skins/ironclad/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_SILENT)) {
                basePath = "spirepass/images/skins/silent/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_DEFECT)) {
                basePath = "spirepass/images/skins/defect/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_WATCHER)) {
                basePath = "spirepass/images/skins/watcher/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_JAW_WORM)) {
                basePath = "spirepass/images/skins/jaw_worm/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_CULTIST)) {
                basePath = "spirepass/images/skins/cultist/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_GREMLIN_NOB)) {
                basePath = "spirepass/images/skins/gremlin_nob/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_BEAR)) {
                basePath = "spirepass/images/skins/bear/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_WRITHING_MASS)) {
                basePath = "spirepass/images/skins/writhing_mass/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_GIANT_HEAD)) {
                basePath = "spirepass/images/skins/giant_head/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_AWAKENED_ONE)) {
                basePath = "spirepass/images/skins/awakened_one/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_BLUE_SLAVER)) {
                basePath = "spirepass/images/skins/blueSlaver/" + variant + "/";
            } else if (entityId.equals(Spirepass.ENTITY_RED_SLAVER)) {
                basePath = "spirepass/images/skins/redSlaver/" + variant + "/";
            } else {
                throw new Exception("Unknown entity type: " + entityId);
            }

            atlasUrl = basePath + "skeleton.atlas";
            skeletonUrl = basePath + "skeleton.json";

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
            String idle1Animation = null;
            BaseMod.logger.info("Available animations for " + entityId + ":");
            for (Animation anim : skeletonData.getAnimations()) {
                BaseMod.logger.info(" - " + anim.getName() + " (duration: " + anim.getDuration() + ")");
                // Look for idle animation with case-insensitive comparison
                if (anim.getName().equalsIgnoreCase("idle")) {
                    idleAnimation = anim.getName();
                } else if (anim.getName().equalsIgnoreCase("idle_1")) {
                    idle1Animation = anim.getName();
                }
            }

            // Set appropriate idle animation if found
            if (idleAnimation != null) {
                state.setAnimation(0, idleAnimation, true);
                BaseMod.logger.info(entityId + " using 'idle' animation: " + idleAnimation);
            } else if (idle1Animation != null) {
                state.setAnimation(0, idle1Animation, true);
                BaseMod.logger.info(entityId + " using 'idle_1' animation: " + idle1Animation);
            } else {
                // If no idle animation was found, try to use the first available animation
                if (skeletonData.getAnimations().size > 0) {
                    String firstAnim = skeletonData.getAnimations().get(0).getName();
                    state.setAnimation(0, firstAnim, true);
                    BaseMod.logger.info(entityId + " animation preview for " + variant + " initialized with fallback animation: " + firstAnim);
                } else {
                    throw new Exception("No animations found in skeleton data");
                }
            }

            // Set animation speed
            float scale = getScaleFactor(entityId);
            state.getCurrent(0).setTimeScale(0.6f * scale);

            // Store animation and skeleton
            previewAnimations.get(entityId).put(variant, state);
            previewSkeletons.get(entityId).put(variant, skeleton);
            animationInitialized.get(entityId).put(variant, true);
            BaseMod.logger.info(entityId + " animation preview for " + variant + " initialized successfully");
        } catch (Exception e) {
            BaseMod.logger.error("Error initializing animation for " + entityId + "/" + variant + ": " + e.getMessage());
            e.printStackTrace();
            // Don't store anything for failed animations - let it fail properly
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
            renderFallbackText(sb, entityId + " " + variant + " Preview", Color.RED);
        }
    }

    // Get the scale factor for different entity types
    private float getScaleFactor(String entityId) {
        // Different entities might need different scaling
        if (entityId.equals(Spirepass.ENTITY_IRONCLAD) ||
                entityId.equals(Spirepass.ENTITY_SILENT) ||
                entityId.equals(Spirepass.ENTITY_WATCHER) ||
                entityId.equals(Spirepass.ENTITY_DEFECT)) {
            return SpirepassPositionSettings.CHARACTER_MODEL_SCALE;
        } else if (entityId.equals(Spirepass.ENTITY_JAW_WORM) ||
                entityId.equals(Spirepass.ENTITY_CULTIST) ||
                entityId.equals(Spirepass.ENTITY_AWAKENED_ONE) ||
                entityId.equals(Spirepass.ENTITY_GREMLIN_NOB) ||
                entityId.equals(Spirepass.ENTITY_BEAR) ||
                entityId.equals(Spirepass.ENTITY_GIANT_HEAD) ||
                entityId.equals(Spirepass.ENTITY_WRITHING_MASS) ||
                entityId.equals(Spirepass.ENTITY_BLUE_SLAVER) ||
                entityId.equals(Spirepass.ENTITY_RED_SLAVER)) {
            return SpirepassPositionSettings.MONSTER_MODEL_SCALE;
        }
        return SpirepassPositionSettings.CHARACTER_MODEL_SCALE; // Default
    }

    private String getVariantFromModelId(String entityId, String modelId) {
        if (entityId.equals(Spirepass.ENTITY_IRONCLAD)) {
            if (modelId.startsWith("IRONCLAD_")) {
                return modelId.substring("IRONCLAD_".length()).toLowerCase();
            } else {
                // Just use the modelId directly (or a portion of it) for non-prefixed names
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_SILENT)) {
            if (modelId.startsWith("SILENT_")) {
                return modelId.substring("SILENT_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_DEFECT)) {
            if (modelId.startsWith("DEFECT_")) {
                return modelId.substring("DEFECT_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_WATCHER)) {
            if (modelId.startsWith("WATCHER_")) {
                return modelId.substring("WATCHER_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_JAW_WORM)) {
            if (modelId.startsWith("JAW_WORM_")) {
                return modelId.substring("JAW_WORM_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_CULTIST)) {
            if (modelId.startsWith("CULTIST_")) {
                return modelId.substring("CULTIST_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_GREMLIN_NOB)) {
            if (modelId.startsWith("GREMLIN_NOB_")) {
                return modelId.substring("GREMLIN_NOB_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_BEAR)) {
            if (modelId.startsWith("BEAR_")) {
                return modelId.substring("BEAR_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_WRITHING_MASS)) {
            if (modelId.startsWith("WRITHING_MASS_")) {
                return modelId.substring("WRITHING_MASS_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_GIANT_HEAD)) {
            if (modelId.startsWith("GIANT_HEAD_")) {
                return modelId.substring("GIANT_HEAD_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_AWAKENED_ONE)) {
            if (modelId.startsWith("AWAKENED_ONE_")) {
                return modelId.substring("AWAKENED_ONE_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_BLUE_SLAVER)) {
            if (modelId.startsWith("BLUE_SLAVER_")) {
                return modelId.substring("BLUE_SLAVER_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        } else if (entityId.equals(Spirepass.ENTITY_RED_SLAVER)) {
            if (modelId.startsWith("RED_SLAVER_")) {
                return modelId.substring("RED_SLAVER_".length()).toLowerCase();
            } else {
                return modelId.toLowerCase();
            }
        }

        return modelId.toLowerCase(); // Use the modelId directly
    }

    // ==================== UI BUTTON MANAGEMENT ====================

    private void updateEquipButton(SpirepassLevelBox selectedBox) {
        boolean isUnlocked = selectedBox.isUnlocked();
        SpirepassRewardData reward = getRewardData(selectedBox.getLevel());

        // Determine if equipped
        boolean isEquipped = false;
        String entityId = null;
        String modelId = null;
        String cardbackType = null;
        String cardbackId = null;

        if (reward != null) {
            if (reward.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL) {
                modelId = reward.getModelId();
                entityId = reward.getEntityId();
                String currentSkin = Spirepass.getAppliedSkin(entityId);
                isEquipped = modelId.equals(currentSkin);
            } else if (reward.getType() == SpirepassRewardData.RewardType.CARDBACK) {
                cardbackType = reward.getCardbackType();
                cardbackId = reward.getCardbackId();
                String currentCardback = Spirepass.getAppliedCardback(cardbackType);
                isEquipped = cardbackId.equals(currentCardback);
            }
        }

        // Create button with appropriate text and colors
        String buttonText = isUnlocked ? (isEquipped ? "UNEQUIP" : "EQUIP") : "LOCKED";
        Color buttonColor = isUnlocked ? (isEquipped ? Color.ORANGE : Color.WHITE) : Color.GRAY;
        Color hoverColor = isUnlocked ? (isEquipped ? Color.YELLOW : Color.GREEN) : Color.DARK_GRAY;
        float buttonX = Settings.WIDTH / 2.0f - 80.0f;
        float buttonY = SpirepassPositionSettings.REWARD_BUTTON_Y - 25.0f;

        // Store final values for lambda
        final String finalEntityId = entityId;
        final String finalModelId = modelId;
        final String finalCardbackType = cardbackType;
        final String finalCardbackId = cardbackId;
        final boolean finalIsUnlocked = isUnlocked;
        final SpirepassRewardData finalReward = reward;

        // Create a consumer for the button click
        Consumer<ModLabeledButton> clickConsumer = (button) -> {
            handleButtonClick(finalIsUnlocked, finalReward, finalEntityId, finalModelId,
                    finalCardbackType, finalCardbackId);
        };

        if (equipButton == null) {
            // Create a new button
            equipButton = new ModLabeledButton(buttonText, buttonX / Settings.scale, buttonY / Settings.scale,
                    buttonColor, hoverColor, null, clickConsumer);
        } else {
            // Update existing button
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
                BaseMod.logger.error("Failed to update button click handler: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleButtonClick(boolean isUnlocked, SpirepassRewardData reward,
                                   String entityId, String modelId,
                                   String cardbackType, String cardbackId) {
        System.out.println("Button clicked, unlocked: " + isUnlocked);
        if (!isUnlocked || reward == null) {
            return;
        }

        if (reward.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL && entityId != null && modelId != null) {
            System.out.println("Handling character model: " + entityId + ", " + modelId);
            // Toggle the skin directly
            String currentSkin = Spirepass.getAppliedSkin(entityId);
            boolean shouldUnequip = modelId.equals(currentSkin);
            Spirepass.setAppliedSkin(entityId, shouldUnequip ? "" : modelId);
            System.out.println((shouldUnequip ? "Unequipped " : "Equipped ") +
                    entityId + " skin: " + modelId);
        } else if (reward.getType() == SpirepassRewardData.RewardType.CARDBACK && cardbackType != null && cardbackId != null) {
            System.out.println("Handling cardback: " + cardbackType + ", " + cardbackId);
            // Toggle the cardback directly
            String currentCardback = Spirepass.getAppliedCardback(cardbackType);
            boolean shouldUnequip = cardbackId.equals(currentCardback);
            Spirepass.setAppliedCardback(cardbackType, shouldUnequip ? "" : cardbackId);
            System.out.println((shouldUnequip ? "Unequipped " : "Equipped ") +
                    cardbackType + " cardback: " + cardbackId);
        }
    }

    // ==================== RENDERING ====================

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
    }

    private void renderLevelBoxes(SpriteBatch sb, SpirepassScreen screen, float scrollX, float edgePadding, ArrayList<SpirepassLevelBox> levelBoxes) {
        // Calculate which level boxes are currently visible
        int firstVisibleLevel = Math.max(0, (int) ((scrollX - edgePadding) / screen.getLevelBoxSpacing()) - 1);
        int lastVisibleLevel = Math.min(screen.getMaxLevel(), (int) ((scrollX + Settings.WIDTH - edgePadding) / screen.getLevelBoxSpacing()) + 1);

        // Render each visible level box
        for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
            if (i < levelBoxes.size()) {
                levelBoxes.get(i).render(sb);
            }
        }
    }

    public void renderSelectedLevelReward(SpriteBatch sb, SpirepassLevelBox selectedBox) {
        int level = selectedBox.getLevel();
        boolean isUnlocked = selectedBox.isUnlocked();
        SpirepassRewardData reward = getRewardData(level);

        // If we have specific reward data, use it
        if (reward != null) {
            renderRewardWithData(sb, level, isUnlocked, reward);
        } else {
            renderDefaultReward(sb, level);
        }

        // Update and render the equip button
        updateEquipButton(selectedBox);
        if (equipButton != null) {
            equipButton.render(sb);
        }
    }

    private void renderRewardWithData(SpriteBatch sb, int level, boolean isUnlocked, SpirepassRewardData reward) {
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
        } else if (reward.getType() == SpirepassRewardData.RewardType.IMAGE ||
                reward.getType() == SpirepassRewardData.RewardType.CARDBACK) {
            // For both IMAGE and CARDBACK types, display the image preview
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

        // For cardbacks, show the current status
        if (reward.getType() == SpirepassRewardData.RewardType.CARDBACK) {
            String cardbackType = reward.getCardbackType();
            String cardbackId = reward.getCardbackId();
            String currentCardback = Spirepass.getAppliedCardback(cardbackType);
            boolean isEquipped = cardbackId.equals(currentCardback);

            // Show equipped status beneath the description
            FontHelper.renderFontCentered(
                    sb,
                    FontHelper.tipBodyFont,
                    isEquipped ? "Currently Equipped" : "Not Equipped",
                    Settings.WIDTH / 2.0f,
                    SpirepassPositionSettings.REWARD_DESCRIPTION_Y - 30.0f * Settings.scale,
                    isEquipped ? Color.GREEN : Color.GRAY
            );
        }
    }

    private void renderDefaultReward(SpriteBatch sb, int level) {
        // No specific reward data, just show level number and a generic message
        Texture rewardTexture = getRewardTexture(level);
        if (rewardTexture != null) {
            // Set desired height for all reward images
            float previewHeight = 200.0f * Settings.scale;
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
        } else {
            // Only show text if no texture is available
            renderFallbackText(sb, "No preview available for Level " + level, Color.ORANGE);
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

// ==================== UTILITY METHODS ====================

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

    public void dispose() {
        if (polyBatch != null) {
            polyBatch.dispose();
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

    public SpirepassRewardData getRewardData(int level) {
        return rewardData.getOrDefault(level, null);
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
}