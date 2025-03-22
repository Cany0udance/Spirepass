package spirepass.screens;

import basemod.BaseMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import spirepass.spirepassutil.SkinManager;
import spirepass.spirepassutil.SpirepassPositionSettings;

import java.util.HashMap;

public class SpirepassAnimationManager {
    // Maps for entity previews - generalized for any entity type
    private HashMap<String, HashMap<String, AnimationState>> previewAnimations = new HashMap<>();
    private HashMap<String, HashMap<String, Skeleton>> previewSkeletons = new HashMap<>();
    private HashMap<String, HashMap<String, Boolean>> animationInitialized = new HashMap<>();
    private static SkeletonMeshRenderer skeletonMeshRenderer;
    private PolygonSpriteBatch polyBatch;

    public SpirepassAnimationManager() {
        // Initialize entity preview maps
        initializeAnimationMaps();

        this.polyBatch = new PolygonSpriteBatch();
        skeletonMeshRenderer = new SkeletonMeshRenderer();
        skeletonMeshRenderer.setPremultipliedAlpha(true);
    }

    private void initializeAnimationMaps() {
        // Create maps for all entity types
        String[] entityTypes = {
                SkinManager.ENTITY_IRONCLAD,
                SkinManager.ENTITY_SILENT,
                SkinManager.ENTITY_DEFECT,
                SkinManager.ENTITY_WATCHER,
                SkinManager.ENTITY_JAW_WORM,
                SkinManager.ENTITY_CULTIST,
                SkinManager.ENTITY_BLUE_SLAVER,
                SkinManager.ENTITY_RED_SLAVER,
                SkinManager.ENTITY_SENTRY,
                SkinManager.ENTITY_GREMLIN_NOB,
                SkinManager.ENTITY_ROMEO,
                SkinManager.ENTITY_BEAR,
                SkinManager.ENTITY_CENTURION,
                SkinManager.ENTITY_SNECKO,
                SkinManager.ENTITY_WRITHING_MASS,
                SkinManager.ENTITY_GIANT_HEAD,
                SkinManager.ENTITY_DONU,
                SkinManager.ENTITY_DECA,
                SkinManager.ENTITY_AWAKENED_ONE
        };

        for (String entityType : entityTypes) {
            previewAnimations.put(entityType, new HashMap<>());
            previewSkeletons.put(entityType, new HashMap<>());
            animationInitialized.put(entityType, new HashMap<>());
        }
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
            String basePath = getBasePath(entityId, variant);
            if (basePath.isEmpty()) {
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

    private String getBasePath(String entityId, String variant) {
        String basePath = "";

        if (entityId.equals(SkinManager.ENTITY_IRONCLAD)) {
            basePath = "spirepass/images/skins/ironclad/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_SILENT)) {
            basePath = "spirepass/images/skins/silent/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_DEFECT)) {
            basePath = "spirepass/images/skins/defect/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_WATCHER)) {
            basePath = "spirepass/images/skins/watcher/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_JAW_WORM)) {
            basePath = "spirepass/images/skins/jaw_worm/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_CULTIST)) {
            basePath = "spirepass/images/skins/cultist/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_SENTRY)) {
            basePath = "spirepass/images/skins/sentry/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_GREMLIN_NOB)) {
            basePath = "spirepass/images/skins/gremlin_nob/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_ROMEO)) {
            basePath = "spirepass/images/skins/romeo/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_BEAR)) {
            basePath = "spirepass/images/skins/bear/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_CENTURION)) {
            basePath = "spirepass/images/skins/centurion/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_SNECKO)) {
            basePath = "spirepass/images/skins/snecko/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_WRITHING_MASS)) {
            basePath = "spirepass/images/skins/writhing_mass/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_GIANT_HEAD)) {
            basePath = "spirepass/images/skins/giant_head/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_DONU)) {
            basePath = "spirepass/images/skins/donu/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_DECA)) {
            basePath = "spirepass/images/skins/deca/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_AWAKENED_ONE)) {
            basePath = "spirepass/images/skins/awakened_one/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_BLUE_SLAVER)) {
            basePath = "spirepass/images/skins/blueSlaver/" + variant + "/";
        } else if (entityId.equals(SkinManager.ENTITY_RED_SLAVER)) {
            basePath = "spirepass/images/skins/redSlaver/" + variant + "/";
        }

        return basePath;
    }

    public void renderAnimationPreview(SpriteBatch sb, String entityId, String variant) {
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

    // Get the scale factor for different entity types
    private float getScaleFactor(String entityId) {
        // Different entities might need different scaling
        if (entityId.equals(SkinManager.ENTITY_IRONCLAD) ||
                entityId.equals(SkinManager.ENTITY_SILENT) ||
                entityId.equals(SkinManager.ENTITY_WATCHER) ||
                entityId.equals(SkinManager.ENTITY_DEFECT)) {
            return SpirepassPositionSettings.CHARACTER_MODEL_SCALE;
        } else if (entityId.equals(SkinManager.ENTITY_JAW_WORM) ||
                entityId.equals(SkinManager.ENTITY_CULTIST) ||
                entityId.equals(SkinManager.ENTITY_AWAKENED_ONE) ||
                entityId.equals(SkinManager.ENTITY_SENTRY) ||
                entityId.equals(SkinManager.ENTITY_GREMLIN_NOB) ||
                entityId.equals(SkinManager.ENTITY_BEAR) ||
                entityId.equals(SkinManager.ENTITY_ROMEO) ||
                entityId.equals(SkinManager.ENTITY_CENTURION) ||
                entityId.equals(SkinManager.ENTITY_SNECKO) ||
                entityId.equals(SkinManager.ENTITY_GIANT_HEAD) ||
                entityId.equals(SkinManager.ENTITY_WRITHING_MASS) ||
                entityId.equals(SkinManager.ENTITY_DONU) ||
                entityId.equals(SkinManager.ENTITY_DECA) ||
                entityId.equals(SkinManager.ENTITY_BLUE_SLAVER) ||
                entityId.equals(SkinManager.ENTITY_RED_SLAVER)) {
            return SpirepassPositionSettings.MONSTER_MODEL_SCALE;
        }
        return SpirepassPositionSettings.CHARACTER_MODEL_SCALE; // Default
    }

    public static String getVariantFromModelId(String entityId, String modelId) {
        String prefix = "";

        if (entityId.equals(SkinManager.ENTITY_IRONCLAD)) {
            prefix = "IRONCLAD_";
        } else if (entityId.equals(SkinManager.ENTITY_SILENT)) {
            prefix = "SILENT_";
        } else if (entityId.equals(SkinManager.ENTITY_DEFECT)) {
            prefix = "DEFECT_";
        } else if (entityId.equals(SkinManager.ENTITY_WATCHER)) {
            prefix = "WATCHER_";
        } else if (entityId.equals(SkinManager.ENTITY_JAW_WORM)) {
            prefix = "JAW_WORM_";
        } else if (entityId.equals(SkinManager.ENTITY_CULTIST)) {
            prefix = "CULTIST_";
        } else if (entityId.equals(SkinManager.ENTITY_SENTRY)) {
            prefix = "SENTRY_";
        } else if (entityId.equals(SkinManager.ENTITY_GREMLIN_NOB)) {
            prefix = "GREMLIN_NOB_";
        } else if (entityId.equals(SkinManager.ENTITY_ROMEO)) {
            prefix = "ROMEO_";
        } else if (entityId.equals(SkinManager.ENTITY_BEAR)) {
            prefix = "BEAR_";
        } else if (entityId.equals(SkinManager.ENTITY_CENTURION)) {
            prefix = "CENTURION_";
        } else if (entityId.equals(SkinManager.ENTITY_SNECKO)) {
            prefix = "SNECKO_";
        } else if (entityId.equals(SkinManager.ENTITY_WRITHING_MASS)) {
            prefix = "WRITHING_MASS_";
        } else if (entityId.equals(SkinManager.ENTITY_GIANT_HEAD)) {
            prefix = "GIANT_HEAD_";
        } else if (entityId.equals(SkinManager.ENTITY_DONU)) {
            prefix = "DONU_";
        } else if (entityId.equals(SkinManager.ENTITY_DECA)) {
            prefix = "DECA_";
        } else if (entityId.equals(SkinManager.ENTITY_AWAKENED_ONE)) {
            prefix = "AWAKENED_ONE_";
        } else if (entityId.equals(SkinManager.ENTITY_BLUE_SLAVER)) {
            prefix = "BLUE_SLAVER_";
        } else if (entityId.equals(SkinManager.ENTITY_RED_SLAVER)) {
            prefix = "RED_SLAVER_";
        }

        if (!prefix.isEmpty() && modelId.startsWith(prefix)) {
            return modelId.substring(prefix.length()).toLowerCase();
        }

        return modelId.toLowerCase(); // Use the modelId directly if no prefix match
    }

    public void dispose() {
        if (polyBatch != null) {
            polyBatch.dispose();
        }
    }
}