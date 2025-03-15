package spirepass;

import basemod.BaseMod;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import spirepass.util.GeneralUtils;
import spirepass.util.KeywordInfo;
import spirepass.util.TextureLoader;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;

import java.util.*;

@SpireInitializer
public class Spirepass implements
        EditStringsSubscriber,
        PostInitializeSubscriber {
    public static ModInfo info;
    public static String modID;
    static { loadModInfo(); }
    private static final String resourcesFolder = checkResourcesPath();
    public static final Logger logger = LogManager.getLogger(modID);

    // Replace single skin string with a map of entity IDs to skin IDs
    public static Map<String, String> appliedSkins = new HashMap<>();
    public static Map<String, String> appliedCardbacks = new HashMap<>();

    // Define constants for entity IDs
    public static final String ENTITY_IRONCLAD = "ironclad";
    public static final String ENTITY_WATCHER = "watcher";
    public static final String ENTITY_JAW_WORM = "jaw_worm";
    public static final String CARDBACK_COLORLESS = "colorless";
    public static final String CARDBACK_CURSE = "curse";

    public static SpireConfig config;

    // Helper method to get the applied skin for an entity
    public static String getAppliedSkin(String entityId) {
        return appliedSkins.getOrDefault(entityId, "");
    }

    // Helper method to set an applied skin for an entity
    public static void setAppliedSkin(String entityId, String skinId) {
        appliedSkins.put(entityId, skinId);
        saveConfig();
    }

    public static String getAppliedCardback(String cardbackType) {
        return appliedCardbacks.getOrDefault(cardbackType, "");
    }

    // Helper method to set an applied cardback
    public static void setAppliedCardback(String cardbackType, String cardbackId) {
        appliedCardbacks.put(cardbackType, cardbackId);
        saveConfig();
    }

    public static String makeID(String id) {
        return modID + ":" + id;
    }

    public static void initialize() {
        try {
            // Simple config to store skin and cardback preferences
            Properties defaults = new Properties();
            // Existing entity defaults
            defaults.setProperty(ENTITY_IRONCLAD, "");
            defaults.setProperty(ENTITY_WATCHER, "");
            defaults.setProperty(ENTITY_JAW_WORM, "");
            // Add cardback defaults
            defaults.setProperty(CARDBACK_COLORLESS, "");
            defaults.setProperty(CARDBACK_CURSE, "");

            config = new SpireConfig(modID, "config", defaults);
        } catch (Exception e) {
            logger.error("Failed to load config: " + e.getMessage());
        }

        // Load the saved preferences
        try {
            if (config != null) {
                // Add each entity type to the map
                appliedSkins.put(ENTITY_IRONCLAD, config.getString(ENTITY_IRONCLAD));
                appliedSkins.put(ENTITY_WATCHER, config.getString(ENTITY_WATCHER));
                appliedSkins.put(ENTITY_JAW_WORM, config.getString(ENTITY_JAW_WORM));

                // Load cardback preferences
                appliedCardbacks.put(CARDBACK_COLORLESS, config.getString(CARDBACK_COLORLESS));
                appliedCardbacks.put(CARDBACK_CURSE, config.getString(CARDBACK_CURSE));

                // Backward compatibility for old config format
                if (config.has("ironcladSkin")) {
                    String oldSkin = config.getString("ironcladSkin");
                    if (!oldSkin.isEmpty()) {
                        appliedSkins.put(ENTITY_IRONCLAD, oldSkin);
                    }
                }

                logger.info("Loaded skin preferences: " + appliedSkins);
                logger.info("Loaded cardback preferences: " + appliedCardbacks);
            }
        } catch (Exception e) {
            logger.error("Failed to load preferences: " + e.getMessage());
        }

        new Spirepass();
    }

    // Update saveConfig method to save cardback preferences
    public static void saveConfig() {
        try {
            for (Map.Entry<String, String> entry : appliedSkins.entrySet()) {
                config.setString(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : appliedCardbacks.entrySet()) {
                config.setString(entry.getKey(), entry.getValue());
            }
            config.save();
            logger.info("Saved skin preferences: " + appliedSkins);
            logger.info("Saved cardback preferences: " + appliedCardbacks);
        } catch (Exception e) {
            logger.error("Failed to save preferences: " + e.getMessage());
        }
    }

    public Spirepass() {
        BaseMod.subscribe(this); //This will make BaseMod trigger all the subscribers at their appropriate times.
        logger.info(modID + " subscribed to BaseMod.");
    }

    @Override
    public void receivePostInitialize() {
        Texture badgeTexture = TextureLoader.getTexture(imagePath("badge.png"));
        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors), info.Description, null);
    }

    /*----------Localization----------*/

    //This is used to load the appropriate localization files based on language.
    private static String getLangString()
    {
        return Settings.language.name().toLowerCase();
    }
    private static final String defaultLanguage = "eng";

    public static final Map<String, KeywordInfo> keywords = new HashMap<>();

    @Override
    public void receiveEditStrings() {
        loadLocalization(defaultLanguage); //no exception catching for default localization; you better have at least one that works.
        if (!defaultLanguage.equals(getLangString())) {
            try {
                loadLocalization(getLangString());
            }
            catch (GdxRuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLocalization(String lang) {
        BaseMod.loadCustomStringsFile(CardStrings.class,
                localizationPath(lang, "CardStrings.json"));
        BaseMod.loadCustomStringsFile(CharacterStrings.class,
                localizationPath(lang, "CharacterStrings.json"));
        BaseMod.loadCustomStringsFile(EventStrings.class,
                localizationPath(lang, "EventStrings.json"));
        BaseMod.loadCustomStringsFile(OrbStrings.class,
                localizationPath(lang, "OrbStrings.json"));
        BaseMod.loadCustomStringsFile(PotionStrings.class,
                localizationPath(lang, "PotionStrings.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class,
                localizationPath(lang, "PowerStrings.json"));
        BaseMod.loadCustomStringsFile(RelicStrings.class,
                localizationPath(lang, "RelicStrings.json"));
        BaseMod.loadCustomStringsFile(UIStrings.class,
                localizationPath(lang, "UIStrings.json"));
    }

    public static String localizationPath(String lang, String file) {
        return resourcesFolder + "/localization/" + lang + "/" + file;
    }

    public static String imagePath(String file) {
        return resourcesFolder + "/images/" + file;
    }
    public static String characterPath(String file) {
        return resourcesFolder + "/images/character/" + file;
    }
    public static String powerPath(String file) {
        return resourcesFolder + "/images/powers/" + file;
    }
    public static String relicPath(String file) {
        return resourcesFolder + "/images/relics/" + file;
    }

    /**
     * Checks the expected resources path based on the package name.
     */
    private static String checkResourcesPath() {
        String name = Spirepass.class.getName(); //getPackage can be iffy with patching, so class name is used instead.
        int separator = name.indexOf('.');
        if (separator > 0)
            name = name.substring(0, separator);

        FileHandle resources = new LwjglFileHandle(name, Files.FileType.Internal);
        if (resources.child("images").exists() && resources.child("localization").exists()) {
            return name;
        }

        throw new RuntimeException("\n\tFailed to find resources folder; expected it to be named \"" + name + "\"." +
                " Either make sure the folder under resources has the same name as your mod's package, or change the line\n" +
                "\t\"private static final String resourcesFolder = checkResourcesPath();\"\n" +
                "\tat the top of the " + Spirepass.class.getSimpleName() + " java file.");
    }

    /**
     * This determines the mod's ID based on information stored by ModTheSpire.
     */
    private static void loadModInfo() {
        Optional<ModInfo> infos = Arrays.stream(Loader.MODINFOS).filter((modInfo)->{
            AnnotationDB annotationDB = Patcher.annotationDBMap.get(modInfo.jarURL);
            if (annotationDB == null)
                return false;
            Set<String> initializers = annotationDB.getAnnotationIndex().getOrDefault(SpireInitializer.class.getName(), Collections.emptySet());
            return initializers.contains(Spirepass.class.getName());
        }).findFirst();
        if (infos.isPresent()) {
            info = infos.get();
            modID = info.ID;
        }
        else {
            throw new RuntimeException("Failed to determine mod info/ID based on initializer.");
        }
    }
}
