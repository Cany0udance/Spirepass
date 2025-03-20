package spirepass.spirepassutil;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spirepass.Spirepass;

import java.util.HashMap;
import java.util.Map;

public class SkinManager {
    private static SkinManager instance;
    private static final Logger logger = LogManager.getLogger(Spirepass.modID);

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
    public static final String ENTITY_BLUE_SLAVER = "blue_slaver";
    public static final String ENTITY_RED_SLAVER = "red_slaver";
    public static final String ENTITY_SENTRY = "sentry";
    public static final String ENTITY_GREMLIN_NOB = "gremlin_nob";
    public static final String ENTITY_BEAR = "bear";
    public static final String ENTITY_CENTURION = "centurion";
    public static final String ENTITY_SNECKO = "snecko";
    public static final String ENTITY_WRITHING_MASS = "writhing_mass";
    public static final String ENTITY_GIANT_HEAD = "giant_head";
    public static final String ENTITY_AWAKENED_ONE = "awakened_one";

    // Constants for cardback types
    public static final String CARDBACK_COLORLESS = "colorless";
    public static final String CARDBACK_CURSE = "curse";

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
            // Add each entity type to the map
            appliedSkins.put(ENTITY_IRONCLAD, config.getString(ENTITY_IRONCLAD));
            appliedSkins.put(ENTITY_SILENT, config.getString(ENTITY_SILENT));
            appliedSkins.put(ENTITY_DEFECT, config.getString(ENTITY_DEFECT));
            appliedSkins.put(ENTITY_WATCHER, config.getString(ENTITY_WATCHER));
            appliedSkins.put(ENTITY_JAW_WORM, config.getString(ENTITY_JAW_WORM));
            appliedSkins.put(ENTITY_CULTIST, config.getString(ENTITY_CULTIST));
            appliedSkins.put(ENTITY_BLUE_SLAVER, config.getString(ENTITY_BLUE_SLAVER));
            appliedSkins.put(ENTITY_RED_SLAVER, config.getString(ENTITY_RED_SLAVER));
            appliedSkins.put(ENTITY_SENTRY, config.getString(ENTITY_SENTRY));
            appliedSkins.put(ENTITY_GREMLIN_NOB, config.getString(ENTITY_GREMLIN_NOB));
            appliedSkins.put(ENTITY_BEAR, config.getString(ENTITY_BEAR));
            appliedSkins.put(ENTITY_CENTURION, config.getString(ENTITY_CENTURION));
            appliedSkins.put(ENTITY_SNECKO, config.getString(ENTITY_SNECKO));
            appliedSkins.put(ENTITY_WRITHING_MASS, config.getString(ENTITY_WRITHING_MASS));
            appliedSkins.put(ENTITY_GIANT_HEAD, config.getString(ENTITY_GIANT_HEAD));
            appliedSkins.put(ENTITY_AWAKENED_ONE, config.getString(ENTITY_AWAKENED_ONE));

            // Load cardback preferences
            appliedCardbacks.put(CARDBACK_COLORLESS, config.getString(CARDBACK_COLORLESS));
            appliedCardbacks.put(CARDBACK_CURSE, config.getString(CARDBACK_CURSE));

            logger.info("Loaded skin preferences: " + appliedSkins);
            logger.info("Loaded cardback preferences: " + appliedCardbacks);
        } catch (Exception e) {
            logger.error("Failed to load skin preferences: " + e.getMessage());
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

            logger.info("Saved skin preferences: " + appliedSkins);
            logger.info("Saved cardback preferences: " + appliedCardbacks);
        } catch (Exception e) {
            logger.error("Failed to save skin preferences: " + e.getMessage());
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
        defaults.setProperty(ENTITY_CULTIST, "");
        defaults.setProperty(ENTITY_BLUE_SLAVER, "");
        defaults.setProperty(ENTITY_RED_SLAVER, "");
        defaults.setProperty(ENTITY_SENTRY, "");
        defaults.setProperty(ENTITY_GREMLIN_NOB, "");
        defaults.setProperty(ENTITY_BEAR, "");
        defaults.setProperty(ENTITY_CENTURION, "");
        defaults.setProperty(ENTITY_SNECKO, "");
        defaults.setProperty(ENTITY_WRITHING_MASS, "");
        defaults.setProperty(ENTITY_GIANT_HEAD, "");
        defaults.setProperty(ENTITY_AWAKENED_ONE, "");

        // Cardback defaults
        defaults.setProperty(CARDBACK_COLORLESS, "");
        defaults.setProperty(CARDBACK_CURSE, "");
    }
}