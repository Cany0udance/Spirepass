package spirepass.patches;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.*;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.characters.*;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.beyond.*;
import com.megacrit.cardcrawl.monsters.city.*;
import com.megacrit.cardcrawl.monsters.exordium.*;
import com.megacrit.cardcrawl.relics.PreservedInsect;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import spirepass.spirepassutil.SkinManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

@SpirePatch(
        clz = AbstractCreature.class,
        method = "loadAnimation"
)
public class EntitySkinPatch {
    // ==================== TEXTURE CACHING ====================

    private static HashMap<String, Texture> textureCache = new HashMap<>();

    // ==================== PATCH METHODS ====================

    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractCreature __instance,
                                           String atlasUrl,
                                           String skeletonUrl,
                                           float scale) {
        boolean inDungeon = CardCrawlGame.dungeon != null;
        boolean isLoadingSave = CardCrawlGame.loadingSave;
        boolean isPreview = __instance.name != null && __instance.name.contains("Preview");
        boolean isPlayerCharacter = __instance instanceof AbstractPlayer;
        boolean isInGameMode = CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT ||
                CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY;

        if ((inDungeon || isLoadingSave || (isPlayerCharacter && isInGameMode)) && !isPreview) {
            String entityId = getEntityId(__instance);

            if (entityId != null) {
                String skinId = SkinManager.getInstance().getAppliedSkin(entityId);

                if (skinId != null && !skinId.isEmpty()) {
                    try {
                        String basePath = getSkinPath(entityId, skinId);
                        if (basePath == null) {
                            return SpireReturn.Continue();
                        }

                        String customAtlasUrl = basePath + "skeleton.atlas";
                        String customSkeletonUrl = basePath + "skeleton.json";

                        FileHandle atlasFile = Gdx.files.internal(customAtlasUrl);
                        FileHandle skeletonFile = Gdx.files.internal(customSkeletonUrl);

                        if (!atlasFile.exists() || !skeletonFile.exists()) {
                            return SpireReturn.Continue();
                        }

                        TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(
                                atlasFile,
                                Gdx.files.internal(basePath),
                                false
                        );

                        for (TextureAtlas.TextureAtlasData.Page page : data.getPages()) {
                            String texturePath = basePath + page.textureFile.name();
                            getOrLoadTexture(texturePath);
                        }

                        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(customAtlasUrl));
                        ReflectionHacks.setPrivate(__instance, AbstractCreature.class, "atlas", atlas);

                        SkeletonJson json = new SkeletonJson(atlas);
                        float finalScale = scale;
                        if (CardCrawlGame.dungeon != null && AbstractDungeon.player != null) {
                            AbstractRoom currentRoom = null;
                            try {
                                currentRoom = AbstractDungeon.getCurrRoom();
                            } catch (Exception e) {

                            }

                            if (currentRoom != null && AbstractDungeon.player.hasRelic(PreservedInsect.ID) && !__instance.isPlayer && currentRoom.eliteTrigger) {
                                finalScale += 0.3F;
                            }
                        }

                        json.setScale(Settings.renderScale / finalScale);
                        SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal(customSkeletonUrl));

                        Skeleton skeleton = new Skeleton(skeletonData);
                        skeleton.setColor(Color.WHITE);
                        ReflectionHacks.setPrivate(__instance, AbstractCreature.class, "skeleton", skeleton);

                        AnimationStateData stateData = new AnimationStateData(skeletonData);
                        ReflectionHacks.setPrivate(__instance, AbstractCreature.class, "stateData", stateData);
                        __instance.state = new AnimationState(stateData);

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

    // ==================== HELPER METHODS ====================

    public static String getEntityId(AbstractCreature creature) {
        if (creature instanceof Ironclad) {
            return SkinManager.ENTITY_IRONCLAD;
        } else if (creature instanceof TheSilent) {
            return SkinManager.ENTITY_SILENT;
        } else if (creature instanceof Defect) {
            return SkinManager.ENTITY_DEFECT;
        } else if (creature instanceof Watcher) {
            return SkinManager.ENTITY_WATCHER;
        } else if (creature instanceof JawWorm) {
            return SkinManager.ENTITY_JAW_WORM;
        } else if (creature instanceof Cultist) {
            return SkinManager.ENTITY_CULTIST;
        } else if (creature instanceof FungiBeast) {
            return SkinManager.ENTITY_FUNGI_BEAST;
        } else if (creature instanceof SlaverBlue) {
            return SkinManager.ENTITY_BLUE_SLAVER;
        } else if (creature instanceof SlaverRed) {
            return SkinManager.ENTITY_RED_SLAVER;
        } else if (creature instanceof GremlinWarrior) {
            return SkinManager.ENTITY_MAD_GREMLIN;
        } else if (creature instanceof GremlinThief) {
            return SkinManager.ENTITY_SNEAKY_GREMLIN;
        } else if (creature instanceof Sentry) {
            return SkinManager.ENTITY_SENTRY;
        } else if (creature instanceof GremlinNob) {
            return SkinManager.ENTITY_GREMLIN_NOB;
        } else if (creature instanceof TheGuardian) {
            return SkinManager.ENTITY_GUARDIAN;
        } else if (creature instanceof SlimeBoss) {
            return SkinManager.ENTITY_SLIME_BOSS;
        } else if (creature instanceof ShelledParasite) {
            return SkinManager.ENTITY_SHELLED_PARASITE;
        } else if (creature instanceof BanditLeader) {
            return SkinManager.ENTITY_ROMEO;
        } else if (creature instanceof BanditBear) {
            return SkinManager.ENTITY_BEAR;
        } else if (creature instanceof Centurion) {
            return SkinManager.ENTITY_CENTURION;
        } else if (creature instanceof Snecko) {
            return SkinManager.ENTITY_SNECKO;
        } else if (creature instanceof OrbWalker) {
            return SkinManager.ENTITY_ORB_WALKER;
        } else if (creature instanceof SpireGrowth) {
            return SkinManager.ENTITY_SPIRE_GROWTH;
        } else if (creature instanceof WrithingMass) {
            return SkinManager.ENTITY_WRITHING_MASS;
        } else if (creature instanceof GiantHead) {
            return SkinManager.ENTITY_GIANT_HEAD;
        } else if (creature instanceof Donu) {
            return SkinManager.ENTITY_DONU;
        } else if (creature instanceof Deca) {
            return SkinManager.ENTITY_DECA;
        } else if (creature instanceof AwakenedOne) {
            return SkinManager.ENTITY_AWAKENED_ONE;
        }
        return null;
    }

    private static String getSkinPath(String entityId, String skinId) {
        if (entityId.equals(SkinManager.ENTITY_IRONCLAD)) {
            if (skinId.equals("IRONCLAD")) {
                return null; // Default Ironclad
            } else if (skinId.startsWith("IRONCLAD_")) {
                String variant = skinId.substring("IRONCLAD_".length()).toLowerCase();
                return "spirepass/images/skins/ironclad/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_SILENT)) {
            if (skinId.equals("SILENT")) {
                return null; // Default Silent
            } else if (skinId.startsWith("SILENT_")) {
                String variant = skinId.substring("SILENT_".length()).toLowerCase();
                return "spirepass/images/skins/silent/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_DEFECT)) {
            if (skinId.equals("DEFECT")) {
                return null;
            } else if (skinId.startsWith("DEFECT_")) {
                String variant = skinId.substring("DEFECT_".length()).toLowerCase();
                return "spirepass/images/skins/defect/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_WATCHER)) {
            if (skinId.equals("WATCHER")) {
                return null; // Default Watcher
            } else if (skinId.startsWith("WATCHER_")) {
                String variant = skinId.substring("WATCHER_".length()).toLowerCase();
                return "spirepass/images/skins/watcher/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_JAW_WORM)) {
            if (skinId.equals("JAW_WORM")) {
                return null; // Default Jaw Worm
            } else if (skinId.startsWith("JAW_WORM_")) {
                String variant = skinId.substring("JAW_WORM_".length()).toLowerCase();
                if (variant.equals("pretty")) {
                    int randomVariantNum = MathUtils.random(1, 6);
                    return "spirepass/images/skins/jaw_worm/pretty" + randomVariantNum + "/";
                } else {
                    return "spirepass/images/skins/jaw_worm/" + variant + "/";
                }
            }
        } else if (entityId.equals(SkinManager.ENTITY_CULTIST)) {
            if (skinId.equals("CULTIST")) {
                return null;
            } else if (skinId.startsWith("CULTIST_")) {
                String variant = skinId.substring("CULTIST_".length()).toLowerCase();
                return "spirepass/images/skins/cultist/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_FUNGI_BEAST)) {
            if (skinId.equals("FUNGI_BEAST")) {
                return null;
            } else if (skinId.startsWith("FUNGI_BEAST_")) {
                String variant = skinId.substring("FUNGI_BEAST_".length()).toLowerCase();
                return "spirepass/images/skins/fungi_beast/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_MAD_GREMLIN)) {
            if (skinId.equals("MAD_GREMLIN")) {
                return null;
            } else if (skinId.startsWith("MAD_GREMLIN_")) {
                String variant = skinId.substring("MAD_GREMLIN_".length()).toLowerCase();
                return "spirepass/images/skins/mad_gremlin/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_SNEAKY_GREMLIN)) {
            if (skinId.equals("SNEAKY_GREMLIN")) {
                return null;
            } else if (skinId.startsWith("SNEAKY_GREMLIN_")) {
                String variant = skinId.substring("SNEAKY_GREMLIN_".length()).toLowerCase();
                return "spirepass/images/skins/sneaky_gremlin/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_SENTRY)) {
            if (skinId.equals("SENTRY")) {
                return null;
            } else if (skinId.startsWith("SENTRY_")) {
                String variant = skinId.substring("SENTRY_".length()).toLowerCase();
                return "spirepass/images/skins/sentry/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_GREMLIN_NOB)) {
            if (skinId.equals("GREMLIN_NOB")) {
                return null;
            } else if (skinId.startsWith("GREMLIN_NOB_")) {
                String variant = skinId.substring("GREMLIN_NOB_".length()).toLowerCase();
                return "spirepass/images/skins/gremlin_nob/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_GUARDIAN)) {
            if (skinId.equals("GUARDIAN")) {
                return null;
            } else if (skinId.startsWith("GUARDIAN_")) {
                String variant = skinId.substring("GUARDIAN_".length()).toLowerCase();
                return "spirepass/images/skins/guardian/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_SLIME_BOSS)) {
            if (skinId.equals("SLIME_BOSS")) {
                return null;
            } else if (skinId.startsWith("SLIME_BOSS_")) {
                String variant = skinId.substring("SLIME_BOSS_".length()).toLowerCase();
                return "spirepass/images/skins/slime_boss/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_SHELLED_PARASITE)) {
            if (skinId.equals("ENTITY_SHELLED_PARASITE")) {
                return null;
            } else if (skinId.startsWith("SHELLED_PARASITE_")) {
                String variant = skinId.substring("SHELLED_PARASITE_".length()).toLowerCase();
                return "spirepass/images/skins/shelled_parasite/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_ROMEO)) {
            if (skinId.equals("ROMEO")) {
                return null;
            } else if (skinId.startsWith("ROMEO_")) {
                String variant = skinId.substring("ROMEO_".length()).toLowerCase();
                return "spirepass/images/skins/romeo/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_BEAR)) {
            if (skinId.equals("BEAR")) {
                return null;
            } else if (skinId.startsWith("BEAR_")) {
                String variant = skinId.substring("BEAR_".length()).toLowerCase();
                return "spirepass/images/skins/bear/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_CENTURION)) {
            if (skinId.equals("CENTURION")) {
                return null;
            } else if (skinId.startsWith("CENTURION_")) {
                String variant = skinId.substring("CENTURION_".length()).toLowerCase();
                return "spirepass/images/skins/centurion/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_SNECKO)) {
            if (skinId.equals("SNECKO")) {
                return null;
            } else if (skinId.startsWith("SNECKO_")) {
                String variant = skinId.substring("SNECKO_".length()).toLowerCase();
                return "spirepass/images/skins/snecko/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_ORB_WALKER)) {
            if (skinId.equals("ORB_WALKER")) {
                return null;
            } else if (skinId.startsWith("ORB_WALKER_")) {
                String variant = skinId.substring("ORB_WALKER_".length()).toLowerCase();
                return "spirepass/images/skins/orb_walker/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_SPIRE_GROWTH)) {
            if (skinId.equals("SPIRE_GROWTH")) {
                return null;
            } else if (skinId.startsWith("SPIRE_GROWTH_")) {
                String variant = skinId.substring("SPIRE_GROWTH_".length()).toLowerCase();
                return "spirepass/images/skins/spire_growth/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_WRITHING_MASS)) {
            if (skinId.equals("WRITHING_MASS")) {
                return null;
            } else if (skinId.startsWith("WRITHING_MASS_")) {
                String variant = skinId.substring("WRITHING_MASS_".length()).toLowerCase();
                return "spirepass/images/skins/writhing_mass/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_GIANT_HEAD)) {
            if (skinId.equals("GIANT_HEAD")) {
                return null;
            } else if (skinId.startsWith("GIANT_HEAD_")) {
                String variant = skinId.substring("GIANT_HEAD_".length()).toLowerCase();
                return "spirepass/images/skins/giant_head/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_BLUE_SLAVER)) {
            if (skinId.equals("BLUE_SLAVER")) {
                return null;
            } else if (skinId.startsWith("BLUE_SLAVER_")) {
                String variant = skinId.substring("BLUE_SLAVER_".length()).toLowerCase();
                return "spirepass/images/skins/blueSlaver/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_RED_SLAVER)) {
            if (skinId.equals("RED_SLAVER")) {
                return null;
            } else if (skinId.startsWith("RED_SLAVER_")) {
                String variant = skinId.substring("RED_SLAVER_".length()).toLowerCase();
                return "spirepass/images/skins/redSlaver/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_DONU)) {
            if (skinId.equals("DONU")) {
                return null;
            } else if (skinId.startsWith("DONU_")) {
                String variant = skinId.substring("DONU_".length()).toLowerCase();
                return "spirepass/images/skins/donu/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_DECA)) {
            if (skinId.equals("DECA")) {
                return null;
            } else if (skinId.startsWith("DECA_")) {
                String variant = skinId.substring("DECA_".length()).toLowerCase();
                return "spirepass/images/skins/deca/" + variant + "/";
            }
        } else if (entityId.equals(SkinManager.ENTITY_AWAKENED_ONE)) {
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
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
    }
}