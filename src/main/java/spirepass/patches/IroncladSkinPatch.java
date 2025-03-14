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
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import spirepass.Spirepass;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

@SpirePatch(
        clz = AbstractCreature.class,
        method = "loadAnimation"
)
public class IroncladSkinPatch {
    private static HashMap<String, Texture> textureCache = new HashMap<>();

    // Remove the SkinCache class - caching might be causing our issues

    private static Texture getOrLoadTexture(String path) {
        if (!textureCache.containsKey(path)) {
            FileHandle fileHandle = Gdx.files.internal(path);
            textureCache.put(path, new Texture(fileHandle));
        }
        return textureCache.get(path);
    }

    private static String getVariantPath(String skinId) {
        if (skinId.equals("IRONCLAD")) {
            return null; // Default Ironclad uses game's built-in assets
        } else if (skinId.startsWith("IRONCLAD_")) {
            String variant = skinId.substring("IRONCLAD_".length()).toLowerCase();
            return "spirepass/images/skins/ironclad/" + variant + "/";
        }
        return null; // Unknown format
    }

    private static void logError(Exception e, String skinId) {
        BaseMod.logger.error("ERROR APPLYING IRONCLAD SKIN " + skinId + ": " + e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        BaseMod.logger.error(sw.toString());
    }

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
        // 2. This is an Ironclad character
        // 3. We have a selected skin
        boolean inDungeon = CardCrawlGame.dungeon != null;
        boolean isLoadingSave = CardCrawlGame.loadingSave; // Correct field to check if game is loading
        boolean isIronclad = __instance instanceof Ironclad;
        boolean hasSkin = Spirepass.currentIroncladSkin != null && !Spirepass.currentIroncladSkin.isEmpty();

        // Special case for preview Ironclads in the Spirepass screen
        boolean isPreviewIronclad = __instance.name != null && __instance.name.contains("PreviewIronclad");

        BaseMod.logger.info("Spirepass skin conditions: inDungeon=" + inDungeon
                + ", isLoadingSave=" + isLoadingSave
                + ", isIronclad=" + isIronclad
                + ", isPreviewIronclad=" + isPreviewIronclad
                + ", hasSkin=" + hasSkin
                + ", currentSkin='" + Spirepass.currentIroncladSkin + "'");

        // Apply skin if it's the actual player character (not a preview) and there's a skin selected
        if ((inDungeon || isLoadingSave) && isIronclad && !isPreviewIronclad && hasSkin) {
            try {
                String skinId = Spirepass.currentIroncladSkin;

                // Get the path for this variant
                String basePath = getVariantPath(skinId);
                if (basePath == null) {
                    BaseMod.logger.info("Using default or unknown skin ID: " + skinId);
                    return SpireReturn.Continue();
                }

                // Set paths for the custom skin
                String customAtlasUrl = basePath + "skeleton.atlas";
                String customSkeletonUrl = basePath + "skeleton.json";

                // Log the skin application
                Spirepass.logger.info("Applying Ironclad skin: " + skinId + " from " + customAtlasUrl);

                // Check if files exist
                FileHandle atlasFile = Gdx.files.internal(customAtlasUrl);
                FileHandle skeletonFile = Gdx.files.internal(customSkeletonUrl);

                BaseMod.logger.info("Atlas file exists: " + atlasFile.exists() + ", path: " + customAtlasUrl);
                BaseMod.logger.info("Skeleton file exists: " + skeletonFile.exists() + ", path: " + customSkeletonUrl);

                if (!atlasFile.exists() || !skeletonFile.exists()) {
                    BaseMod.logger.error("Skin files not found! Falling back to default Ironclad");
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

                // Create the texture atlas fresh each time
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

                // Set up animations
                BaseMod.logger.info("Configuring animations");
                stateData.setMix("Hit", "Idle", 0.1F);
                AnimationState.TrackEntry e = __instance.state.setAnimation(0, "Idle", true);
                e.setTimeScale(0.6F);

                BaseMod.logger.info("Ironclad skin " + skinId + " successfully applied!");
                return SpireReturn.Return();
            } catch (Exception e) {
                logError(e, Spirepass.currentIroncladSkin);
                return SpireReturn.Continue();
            }
        }

        return SpireReturn.Continue();
    }
}