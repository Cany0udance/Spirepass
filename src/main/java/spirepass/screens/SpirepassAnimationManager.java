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
    // ==================== DATA STRUCTURES ====================

    private HashMap<String, HashMap<String, AnimationState>> previewAnimations = new HashMap<>();
    private HashMap<String, HashMap<String, Skeleton>> previewSkeletons = new HashMap<>();
    private HashMap<String, HashMap<String, Boolean>> animationInitialized = new HashMap<>();
    private static SkeletonMeshRenderer skeletonMeshRenderer;
    private PolygonSpriteBatch polyBatch;

    // ==================== INITIALIZATION ====================

    public SpirepassAnimationManager() {
        initializeAnimationMaps();
        this.polyBatch = new PolygonSpriteBatch();
        skeletonMeshRenderer = new SkeletonMeshRenderer();
        skeletonMeshRenderer.setPremultipliedAlpha(true);
    }

    private void initializeAnimationMaps() {
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

    // ==================== ANIMATION MANAGEMENT ====================

    private void initializeAnimationPreview(String entityId, String variant) {
        if (animationInitialized.get(entityId).getOrDefault(variant, false)) {
            return;
        }
        try {
            String atlasUrl, skeletonUrl;

            String basePath = getBasePath(entityId, variant);
            if (basePath.isEmpty()) {
                throw new Exception("Unknown entity type: " + entityId);
            }

            atlasUrl = basePath + "skeleton.atlas";
            skeletonUrl = basePath + "skeleton.json";

            TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(atlasUrl));
            SkeletonJson json = new SkeletonJson(atlas);
            SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal(skeletonUrl));

            Skeleton skeleton = new Skeleton(skeletonData);
            AnimationStateData stateData = new AnimationStateData(skeletonData);
            AnimationState state = new AnimationState(stateData);

            String idleAnimation = null;
            String idle1Animation = null;
            for (Animation anim : skeletonData.getAnimations()) {
                if (anim.getName().equalsIgnoreCase("idle")) {
                    idleAnimation = anim.getName();
                } else if (anim.getName().equalsIgnoreCase("idle_1")) {
                    idle1Animation = anim.getName();
                }
            }

            if (idleAnimation != null) {
                state.setAnimation(0, idleAnimation, true);
            } else if (idle1Animation != null) {
                state.setAnimation(0, idle1Animation, true);
            } else {
                if (skeletonData.getAnimations().size > 0) {
                    String firstAnim = skeletonData.getAnimations().get(0).getName();
                    state.setAnimation(0, firstAnim, true);
                } else {
                    throw new Exception("No animations found in skeleton data");
                }
            }

            state.getCurrent(0).setTimeScale(0.85f);

            previewAnimations.get(entityId).put(variant, state);
            previewSkeletons.get(entityId).put(variant, skeleton);
            animationInitialized.get(entityId).put(variant, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renderAnimationPreview(SpriteBatch sb, String entityId, String variant) {
        if (!animationInitialized.get(entityId).getOrDefault(variant, false)) {
            initializeAnimationPreview(entityId, variant);
        }

        AnimationState state = previewAnimations.get(entityId).get(variant);
        Skeleton skeleton = previewSkeletons.get(entityId).get(variant);

        if (state != null && skeleton != null) {
            try {
                state.update(Gdx.graphics.getDeltaTime());
                state.apply(skeleton);

                skeleton.setPosition(
                        Settings.WIDTH / 2.0f,
                        SpirepassPositionSettings.REWARD_PREVIEW_Y - SpirepassPositionSettings.CHARACTER_MODEL_Y_OFFSET
                );

                float scale = getScaleFactor(entityId);
                skeleton.getRootBone().setScale(scale, scale);

                skeleton.updateWorldTransform();

                boolean batchWasDrawing = sb.isDrawing();

                if (batchWasDrawing) {
                    sb.end();
                }

                polyBatch.setProjectionMatrix(sb.getProjectionMatrix());
                polyBatch.setTransformMatrix(sb.getTransformMatrix());
                polyBatch.begin();

                skeletonMeshRenderer.draw(polyBatch, skeleton);

                polyBatch.end();

                if (batchWasDrawing) {
                    sb.begin();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!sb.isDrawing()) {
                    sb.begin();
                }
                renderFallbackText(sb, "Error Rendering Animation", Color.RED);
            }
        } else {
            renderFallbackText(sb, entityId + " " + variant + " Preview", Color.RED);
        }
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

    private float getScaleFactor(String entityId) {
        float baseScale;
        if (entityId.equals(SkinManager.ENTITY_IRONCLAD) ||
                entityId.equals(SkinManager.ENTITY_SILENT) ||
                entityId.equals(SkinManager.ENTITY_DEFECT)) {
            baseScale = SpirepassPositionSettings.CHARACTER_MODEL_SCALE;
        } else {
            baseScale = SpirepassPositionSettings.MONSTER_MODEL_SCALE;
        }

        switch (entityId) {
            case SkinManager.ENTITY_DONU:
                return baseScale * 0.75f;
            case SkinManager.ENTITY_CENTURION:
                return baseScale * 0.85f;
            case SkinManager.ENTITY_GREMLIN_NOB:
                return baseScale * 0.8f;
            case SkinManager.ENTITY_SNECKO:
                return baseScale * 0.9f;
            case SkinManager.ENTITY_DECA:
                return baseScale * 0.75f;
            case SkinManager.ENTITY_WATCHER:
                return SpirepassPositionSettings.CHARACTER_MODEL_SCALE * 0.95f;
            case SkinManager.ENTITY_SENTRY:
                return baseScale * 0.85f;
            case SkinManager.ENTITY_WRITHING_MASS:
                return baseScale * 0.9f;
            default:
                return baseScale;
        }
    }

    // ==================== PATH MANAGEMENT ====================

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

        return modelId.toLowerCase();
    }

    // ==================== CLEANUP ====================

    public void dispose() {
        if (polyBatch != null) {
            polyBatch.dispose();
        }
    }
}