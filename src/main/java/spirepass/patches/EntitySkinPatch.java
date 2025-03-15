package spirepass.patches;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.*;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.characters.Defect;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.characters.Watcher;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.monsters.beyond.AwakenedOne;
import com.megacrit.cardcrawl.monsters.exordium.JawWorm;
import com.megacrit.cardcrawl.monsters.exordium.SlaverBlue;
import com.megacrit.cardcrawl.monsters.exordium.SlaverRed;
import spirepass.Spirepass;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

@SpirePatch(
        clz = AbstractCreature.class,
        method = "loadAnimation"
)
public class EntitySkinPatch {
    private static HashMap<String, Texture> textureCache = new HashMap<>();

    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractCreature __instance,
                                           String atlasUrl,
                                           String skeletonUrl,
                                           float scale) {
        // Log every character that tries to load animation
        BaseMod.logger.info("loadAnimation called for: " + __instance.getClass().getSimpleName()
                + ", atlas: " + atlasUrl
                + ", skeleton: " + skeletonUrl);

        // Only apply if:
        // 1. We're in a dungeon OR loading a save
        // 2. This is not a preview character
        boolean inDungeon = CardCrawlGame.dungeon != null;
        boolean isLoadingSave = CardCrawlGame.loadingSave;
        boolean isPreview = __instance.name != null && __instance.name.contains("Preview");

        if ((inDungeon || isLoadingSave) && !isPreview) {
            // Determine which entity type this is
            String entityId = null;

            if (__instance instanceof Ironclad) {
                entityId = Spirepass.ENTITY_IRONCLAD;
            } else if (__instance instanceof Defect) {
                entityId = Spirepass.ENTITY_DEFECT;
            } else if (__instance instanceof Watcher) {
                entityId = Spirepass.ENTITY_WATCHER;
            } else if (__instance instanceof JawWorm) {
                entityId = Spirepass.ENTITY_JAW_WORM;
            } else if (__instance instanceof SlaverBlue) {
                entityId = Spirepass.ENTITY_BLUE_SLAVER;
            } else if (__instance instanceof SlaverRed) {
                entityId = Spirepass.ENTITY_RED_SLAVER;
            } else if (__instance instanceof AwakenedOne) {
                entityId = Spirepass.ENTITY_AWAKENED_ONE;
            }

            // If we recognized the entity and have a skin for it
            if (entityId != null) {
                String skinId = Spirepass.getAppliedSkin(entityId);

                if (skinId != null && !skinId.isEmpty()) {
                    BaseMod.logger.info("Applying skin " + skinId + " to " + entityId);

                    try {
                        // Get the path for this entity's skin
                        String basePath = getSkinPath(entityId, skinId);
                        if (basePath == null) {
                            BaseMod.logger.info("Using default skin for " + entityId);
                            return SpireReturn.Continue();
                        }

                        // Set paths for the custom skin
                        String customAtlasUrl = basePath + "skeleton.atlas";
                        String customSkeletonUrl = basePath + "skeleton.json";

                        // Log the skin application
                        Spirepass.logger.info("Applying skin from " + customAtlasUrl);

                        // Check if files exist
                        FileHandle atlasFile = Gdx.files.internal(customAtlasUrl);
                        FileHandle skeletonFile = Gdx.files.internal(customSkeletonUrl);

                        BaseMod.logger.info("Atlas file exists: " + atlasFile.exists() + ", path: " + customAtlasUrl);
                        BaseMod.logger.info("Skeleton file exists: " + skeletonFile.exists() + ", path: " + customSkeletonUrl);

                        if (!atlasFile.exists() || !skeletonFile.exists()) {
                            BaseMod.logger.error("Skin files not found! Falling back to default");
                            return SpireReturn.Continue();
                        }

                        // Load the atlas data
                        BaseMod.logger.info("Loading texture atlas data from: " + customAtlasUrl);
                        TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(
                                atlasFile,
                                Gdx.files.internal(basePath),
                                false
                        );

                        // Pre-load textures
                        BaseMod.logger.info("Pre-loading textures for skin...");
                        for (TextureAtlas.TextureAtlasData.Page page : data.getPages()) {
                            String texturePath = basePath + page.textureFile.name();
                            BaseMod.logger.info("Loading texture: " + texturePath);
                            getOrLoadTexture(texturePath);
                        }
                        BaseMod.logger.info("Finished pre-loading textures");

                        // Create the texture atlas
                        BaseMod.logger.info("Creating texture atlas");
                        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(customAtlasUrl));
                        ReflectionHacks.setPrivate(__instance, AbstractCreature.class, "atlas", atlas);

                        // Load the skeleton data
                        BaseMod.logger.info("Loading skeleton data");
                        SkeletonJson json = new SkeletonJson(atlas);
                        json.setScale(Settings.renderScale / scale);
                        SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal(customSkeletonUrl));

                        // Check available animations
                        BaseMod.logger.info("Available animations:");
                        for (Animation anim : skeletonData.getAnimations()) {
                            BaseMod.logger.info(" - " + anim.getName() + " (duration: " + anim.getDuration() + ")");
                        }

                        // Create and configure the skeleton
                        BaseMod.logger.info("Creating skeleton");
                        Skeleton skeleton = new Skeleton(skeletonData);
                        skeleton.setColor(Color.WHITE);
                        ReflectionHacks.setPrivate(__instance, AbstractCreature.class, "skeleton", skeleton);

                        // Set up the animation state
                        BaseMod.logger.info("Setting up animation state");
                        AnimationStateData stateData = new AnimationStateData(skeletonData);
                        ReflectionHacks.setPrivate(__instance, AbstractCreature.class, "stateData", stateData);
                        __instance.state = new AnimationState(stateData);

                        BaseMod.logger.info("Skin " + skinId + " successfully applied to " + entityId);
                        return SpireReturn.Return();
                    } catch (Exception e) {
                        logError(e, entityId, skinId);
                        return SpireReturn.Continue();
                    }
                }
            }
        }

        return SpireReturn.Continue();
    }

    private static String getSkinPath(String entityId, String skinId) {
        if (entityId.equals(Spirepass.ENTITY_IRONCLAD)) {
            if (skinId.equals("IRONCLAD")) {
                return null; // Default Ironclad
            } else if (skinId.startsWith("IRONCLAD_")) {
                String variant = skinId.substring("IRONCLAD_".length()).toLowerCase();
                return "spirepass/images/skins/ironclad/" + variant + "/";
            }
        } else if (entityId.equals(Spirepass.ENTITY_DEFECT)) {
            if (skinId.equals("DEFECT")) {
                return null; // Default Watcher
            } else if (skinId.startsWith("DEFECT_")) {
                String variant = skinId.substring("DEFECT_".length()).toLowerCase();
                return "spirepass/images/skins/defect/" + variant + "/";
            }
        } else if (entityId.equals(Spirepass.ENTITY_WATCHER)) {
            if (skinId.equals("WATCHER")) {
                return null; // Default Watcher
            } else if (skinId.startsWith("WATCHER_")) {
                String variant = skinId.substring("WATCHER_".length()).toLowerCase();
                return "spirepass/images/skins/watcher/" + variant + "/";
            }
        } else if (entityId.equals(Spirepass.ENTITY_JAW_WORM)) {
            if (skinId.equals("JAW_WORM")) {
                return null; // Default Jaw Worm
            } else if (skinId.startsWith("JAW_WORM_")) {
                String variant = skinId.substring("JAW_WORM_".length()).toLowerCase();
                return "spirepass/images/skins/jaw_worm/" + variant + "/";
            }
        } else if (entityId.equals(Spirepass.ENTITY_BLUE_SLAVER)) {
            if (skinId.equals("BLUE_SLAVER")) {
                return null; // Default Jaw Worm
            } else if (skinId.startsWith("BLUE_SLAVER_")) {
                String variant = skinId.substring("BLUE_SLAVER_".length()).toLowerCase();
                return "spirepass/images/skins/blueSlaver/" + variant + "/";
            }
        } else if (entityId.equals(Spirepass.ENTITY_RED_SLAVER)) {
            if (skinId.equals("RED_SLAVER")) {
                return null; // Default Jaw Worm
            } else if (skinId.startsWith("RED_SLAVER_")) {
                String variant = skinId.substring("RED_SLAVER_".length()).toLowerCase();
                return "spirepass/images/skins/redSlaver/" + variant + "/";
            }
        } else if (entityId.equals(Spirepass.ENTITY_AWAKENED_ONE)) {
            if (skinId.equals("AWAKENED_ONE")) {
                return null; // Default Awakened One
            } else if (skinId.startsWith("AWAKENED_ONE_")) {
                String variant = skinId.substring("AWAKENED_ONE_".length()).toLowerCase();
                return "spirepass/images/skins/awakened_one/" + variant + "/";
            }
        }


        return null; // Unknown format or default skin
    }

    private static Texture getOrLoadTexture(String path) {
        if (!textureCache.containsKey(path)) {
            FileHandle fileHandle = Gdx.files.internal(path);
            textureCache.put(path, new Texture(fileHandle));
        }
        return textureCache.get(path);
    }

    private static void logError(Exception e, String entityId, String skinId) {
        BaseMod.logger.error("ERROR APPLYING SKIN " + skinId + " TO " + entityId + ": " + e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        BaseMod.logger.error(sw.toString());
    }
}