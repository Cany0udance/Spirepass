package spirepass.spirepassutil;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;

import java.util.HashMap;
import java.util.Map;

public class SkinManager {
    private static SkinManager instance;
//     private static final Logger logger = LogManager.getLogger(Spirepass.modID);

    // Maps to store skin and cardback preferences
    private Map<String, String> appliedSkins = new HashMap<>();
    private Map<String, String> appliedCardbacks = new HashMap<>();

    // Constants for entity IDs
    public static final String ENTITY_IRONCLAD = "ironclad";
    public static final String ENTITY_SILENT = "silent";
    public static final String ENTITY_DEFECT = "defect";
    public static final String ENTITY_WATCHER = "watcher";
    public static final String ENTITY_JAW_WORM = "jaw_worm";
    public static final String ENTITY_CULTIST = "cultist";
    public static final String ENTITY_FUNGI_BEAST = "fungi_beast";
    public static final String ENTITY_BLUE_SLAVER = "blue_slaver";
    public static final String ENTITY_RED_SLAVER = "red_slaver";
    public static final String ENTITY_MAD_GREMLIN = "mad_gremlin";
    public static final String ENTITY_SNEAKY_GREMLIN = "sneaky_gremlin";
    public static final String ENTITY_SENTRY = "sentry";
    public static final String ENTITY_GREMLIN_NOB = "gremlin_nob";
    public static final String ENTITY_GUARDIAN = "guardian";
    public static final String ENTITY_SLIME_BOSS = "slime_boss";
    public static final String ENTITY_SHELLED_PARASITE = "shelled_parasite";
    public static final String ENTITY_ROMEO = "romeo";
    public static final String ENTITY_BEAR = "bear";
    public static final String ENTITY_CENTURION = "centurion";
    public static final String ENTITY_SNECKO = "snecko";
    public static final String ENTITY_ORB_WALKER = "orb_walker";
    public static final String ENTITY_SPIRE_GROWTH = "spire_growth";
    public static final String ENTITY_WRITHING_MASS = "writhing_mass";
    public static final String ENTITY_GIANT_HEAD = "giant_head";
    public static final String ENTITY_DONU = "donu";
    public static final String ENTITY_DECA = "deca";
    public static final String ENTITY_AWAKENED_ONE = "awakened_one";

    // Constants for cardback types
    public static final String CARDBACK_COLORLESS = "colorless";
    public static final String CARDBACK_CURSE = "curse";

    public static final String BACKGROUND_SCREEN = "screen_background";

    // Private constructor for singleton pattern
    private SkinManager() {
        // Initialize empty maps
    }

    /**
     * Get the singleton instance of SkinManager
     */
    public static SkinManager getInstance() {
        if (instance == null) {
            instance = new SkinManager();
        }
        return instance;
    }

    /**
     * Helper method to get the applied skin for an entity
     */
    public String getAppliedSkin(String entityId) {
        return appliedSkins.getOrDefault(entityId, "");
    }

    /**
     * Helper method to set an applied skin for an entity
     */
    public void setAppliedSkin(String entityId, String skinId) {
        appliedSkins.put(entityId, skinId);
    }

    /**
     * Helper method to get the applied cardback for a type
     */
    public String getAppliedCardback(String cardbackType) {
        return appliedCardbacks.getOrDefault(cardbackType, "");
    }

    /**
     * Helper method to set an applied cardback
     */
    public void setAppliedCardback(String cardbackType, String cardbackId) {
        appliedCardbacks.put(cardbackType, cardbackId);
    }

    /**
     * Load skin data from config
     */
    public void loadData(SpireConfig config) {
        try {
            // Add each entity type to the map with proper null checking
            // Character skins
            appliedSkins.put(ENTITY_IRONCLAD, config.has(ENTITY_IRONCLAD) ? config.getString(ENTITY_IRONCLAD) : "");
            appliedSkins.put(ENTITY_SILENT, config.has(ENTITY_SILENT) ? config.getString(ENTITY_SILENT) : "");
            appliedSkins.put(ENTITY_DEFECT, config.has(ENTITY_DEFECT) ? config.getString(ENTITY_DEFECT) : "");
            appliedSkins.put(ENTITY_WATCHER, config.has(ENTITY_WATCHER) ? config.getString(ENTITY_WATCHER) : "");

            // Monster skins
            appliedSkins.put(ENTITY_JAW_WORM, config.has(ENTITY_JAW_WORM) ? config.getString(ENTITY_JAW_WORM) : "");
            appliedSkins.put(ENTITY_CULTIST, config.has(ENTITY_CULTIST) ? config.getString(ENTITY_CULTIST) : "");
            appliedSkins.put(ENTITY_FUNGI_BEAST, config.has(ENTITY_FUNGI_BEAST) ? config.getString(ENTITY_FUNGI_BEAST) : "");
            appliedSkins.put(ENTITY_BLUE_SLAVER, config.has(ENTITY_BLUE_SLAVER) ? config.getString(ENTITY_BLUE_SLAVER) : "");
            appliedSkins.put(ENTITY_RED_SLAVER, config.has(ENTITY_RED_SLAVER) ? config.getString(ENTITY_RED_SLAVER) : "");
            appliedSkins.put(ENTITY_MAD_GREMLIN, config.has(ENTITY_MAD_GREMLIN) ? config.getString(ENTITY_MAD_GREMLIN) : "");
            appliedSkins.put(ENTITY_SNEAKY_GREMLIN, config.has(ENTITY_SNEAKY_GREMLIN) ? config.getString(ENTITY_SNEAKY_GREMLIN) : "");
            appliedSkins.put(ENTITY_SENTRY, config.has(ENTITY_SENTRY) ? config.getString(ENTITY_SENTRY) : "");
            appliedSkins.put(ENTITY_GREMLIN_NOB, config.has(ENTITY_GREMLIN_NOB) ? config.getString(ENTITY_GREMLIN_NOB) : "");
            appliedSkins.put(ENTITY_GUARDIAN, config.has(ENTITY_GUARDIAN) ? config.getString(ENTITY_GUARDIAN) : "");
            appliedSkins.put(ENTITY_SLIME_BOSS, config.has(ENTITY_SLIME_BOSS) ? config.getString(ENTITY_SLIME_BOSS) : "");
            appliedSkins.put(ENTITY_SHELLED_PARASITE, config.has(ENTITY_SHELLED_PARASITE) ? config.getString(ENTITY_SHELLED_PARASITE) : "");
            appliedSkins.put(ENTITY_ROMEO, config.has(ENTITY_ROMEO) ? config.getString(ENTITY_ROMEO) : "");
            appliedSkins.put(ENTITY_BEAR, config.has(ENTITY_BEAR) ? config.getString(ENTITY_BEAR) : "");
            appliedSkins.put(ENTITY_CENTURION, config.has(ENTITY_CENTURION) ? config.getString(ENTITY_CENTURION) : "");
            appliedSkins.put(ENTITY_SNECKO, config.has(ENTITY_SNECKO) ? config.getString(ENTITY_SNECKO) : "");
            appliedSkins.put(ENTITY_ORB_WALKER, config.has(ENTITY_ORB_WALKER) ? config.getString(ENTITY_ORB_WALKER) : "");
            appliedSkins.put(ENTITY_SPIRE_GROWTH, config.has(ENTITY_SPIRE_GROWTH) ? config.getString(ENTITY_SPIRE_GROWTH) : "");
            appliedSkins.put(ENTITY_WRITHING_MASS, config.has(ENTITY_WRITHING_MASS) ? config.getString(ENTITY_WRITHING_MASS) : "");
            appliedSkins.put(ENTITY_GIANT_HEAD, config.has(ENTITY_GIANT_HEAD) ? config.getString(ENTITY_GIANT_HEAD) : "");
            appliedSkins.put(ENTITY_DONU, config.has(ENTITY_DONU) ? config.getString(ENTITY_DONU) : "");
            appliedSkins.put(ENTITY_DECA, config.has(ENTITY_DECA) ? config.getString(ENTITY_DECA) : "");
            appliedSkins.put(ENTITY_AWAKENED_ONE, config.has(ENTITY_AWAKENED_ONE) ? config.getString(ENTITY_AWAKENED_ONE) : "");

            appliedSkins.put(BACKGROUND_SCREEN, config.has(BACKGROUND_SCREEN) ? config.getString(BACKGROUND_SCREEN) : "");

            // Load cardback preferences - FIXED with proper null checking
            appliedCardbacks.put(CARDBACK_COLORLESS, config.has(CARDBACK_COLORLESS) ? config.getString(CARDBACK_COLORLESS) : "");
            appliedCardbacks.put(CARDBACK_CURSE, config.has(CARDBACK_CURSE) ? config.getString(CARDBACK_CURSE) : "");

//             logger.info("Loaded skin preferences: " + appliedSkins);
//             logger.info("Loaded cardback preferences: " + appliedCardbacks);
        } catch (Exception e) {
//             logger.error("Failed to load skin preferences: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save skin data to config
     */
    public void saveData(SpireConfig config) {
        try {
            for (Map.Entry<String, String> entry : appliedSkins.entrySet()) {
                config.setString(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : appliedCardbacks.entrySet()) {
                config.setString(entry.getKey(), entry.getValue());
            }

//             logger.info("Saved skin preferences: " + appliedSkins);
//             logger.info("Saved cardback preferences: " + appliedCardbacks);
        } catch (Exception e) {
//             logger.error("Failed to save skin preferences: " + e.getMessage());
        }
    }

    /**
     * Add default properties to config
     */
    public void addDefaultProperties(java.util.Properties defaults) {
        // Entity defaults
        defaults.setProperty(ENTITY_IRONCLAD, "");
        defaults.setProperty(ENTITY_SILENT, "");
        defaults.setProperty(ENTITY_DEFECT, "");
        defaults.setProperty(ENTITY_WATCHER, "");
        defaults.setProperty(ENTITY_JAW_WORM, "");
        defaults.setProperty(ENTITY_FUNGI_BEAST, "");
        defaults.setProperty(ENTITY_CULTIST, "");
        defaults.setProperty(ENTITY_BLUE_SLAVER, "");
        defaults.setProperty(ENTITY_RED_SLAVER, "");
        defaults.setProperty(ENTITY_MAD_GREMLIN, "");
        defaults.setProperty(ENTITY_SNEAKY_GREMLIN, "");
        defaults.setProperty(ENTITY_SENTRY, "");
        defaults.setProperty(ENTITY_GREMLIN_NOB, "");
        defaults.setProperty(ENTITY_GUARDIAN, "");
        defaults.setProperty(ENTITY_SLIME_BOSS, "");
        defaults.setProperty(ENTITY_SHELLED_PARASITE, "");
        defaults.setProperty(ENTITY_ROMEO, "");
        defaults.setProperty(ENTITY_BEAR, "");
        defaults.setProperty(ENTITY_CENTURION, "");
        defaults.setProperty(ENTITY_SNECKO, "");
        defaults.setProperty(ENTITY_ORB_WALKER, "");
        defaults.setProperty(ENTITY_SPIRE_GROWTH, "");
        defaults.setProperty(ENTITY_WRITHING_MASS, "");
        defaults.setProperty(ENTITY_GIANT_HEAD, "");
        defaults.setProperty(ENTITY_DONU, "");
        defaults.setProperty(ENTITY_DECA, "");
        defaults.setProperty(ENTITY_AWAKENED_ONE, "");
        defaults.setProperty(BACKGROUND_SCREEN, "");

        // Cardback defaults
        defaults.setProperty(CARDBACK_COLORLESS, "");
        defaults.setProperty(CARDBACK_CURSE, "");
    }
}