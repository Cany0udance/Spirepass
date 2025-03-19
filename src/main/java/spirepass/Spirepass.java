package spirepass;

import basemod.BaseMod;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import spirepass.challengeutil.Challenge;
import spirepass.challengeutil.ChallengeDefinitions;
import spirepass.challengeutil.ChallengeManager;
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

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SpireInitializer
public class Spirepass implements
        EditStringsSubscriber,
        PostInitializeSubscriber,
        OnCardUseSubscriber,
        OnStartBattleSubscriber {
    public static ModInfo info;
    public static String modID;
    static { loadModInfo(); }
    private static final String resourcesFolder = checkResourcesPath();
    public static final Logger logger = LogManager.getLogger(modID);
    private float saveTimer = 0f;
    private static final float SAVE_INTERVAL = 10f; // Save every 60 seconds
    // Replace single skin string with a map of entity IDs to skin IDs
    public static Map<String, String> appliedSkins = new HashMap<>();
    public static Map<String, String> appliedCardbacks = new HashMap<>();
    // Define constants for entity IDs
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
    public static final String CARDBACK_COLORLESS = "colorless";
    public static final String CARDBACK_CURSE = "curse";

    // Challenge system related properties
    private static final int NUM_DAILY_CHALLENGES = 3;
    private static final int NUM_WEEKLY_CHALLENGES = 3;
    private static final String LAST_DAILY_REFRESH = "lastDailyRefresh";
    private static final String LAST_WEEKLY_REFRESH = "lastWeeklyRefresh";
    private static final int REFRESH_HOUR_EST = 12; // 12 PM EST

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
            // Add cardback defaults
            defaults.setProperty(CARDBACK_COLORLESS, "");
            defaults.setProperty(CARDBACK_CURSE, "");
            // Challenge system default timestamps
            defaults.setProperty(LAST_DAILY_REFRESH, String.valueOf(0));
            defaults.setProperty(LAST_WEEKLY_REFRESH, String.valueOf(0));

            config = new SpireConfig(modID, "config", defaults);
        } catch (Exception e) {
            logger.error("Failed to load config: " + e.getMessage());
        }

        // Load the saved preferences
        try {
            if (config != null) {
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

                // Initialize challenge manager and load challenge data
                ChallengeManager challengeManager = ChallengeManager.getInstance();

                // Load challenge data first
                challengeManager.loadData(config);

                // Then load refresh times
                String dailyRefresh = config.getString(LAST_DAILY_REFRESH);
                String weeklyRefresh = config.getString(LAST_WEEKLY_REFRESH);
                if (dailyRefresh != null && !dailyRefresh.isEmpty()) {
                    challengeManager.setLastDailyRefreshTime(Long.parseLong(dailyRefresh));
                }
                if (weeklyRefresh != null && !weeklyRefresh.isEmpty()) {
                    challengeManager.setLastWeeklyRefreshTime(Long.parseLong(weeklyRefresh));
                }

                // Check if we need to generate/refresh challenges
                checkAndRefreshChallenges();

                logger.info("Loaded " + challengeManager.getDailyChallenges().size() + " daily challenges");
                logger.info("Loaded " + challengeManager.getWeeklyChallenges().size() + " weekly challenges");
            }
        } catch (Exception e) {
            logger.error("Failed to load preferences or challenges: " + e.getMessage());
            e.printStackTrace();
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

            // Save challenge data and timestamps
            ChallengeManager manager = ChallengeManager.getInstance();
            manager.saveData(config);
            config.setString(LAST_DAILY_REFRESH, String.valueOf(manager.getLastDailyRefreshTime()));
            config.setString(LAST_WEEKLY_REFRESH, String.valueOf(manager.getLastWeeklyRefreshTime()));
            logger.info("Saved challenge data");

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

        // No need to initialize challenge manager here - it's already done in initialize()
    }

    /**
     * Check if challenge lists need to be generated or refreshed based on time
     */
    private static void checkAndRefreshChallenges() {
        ChallengeManager manager = ChallengeManager.getInstance();

        // If no challenges exist, generate initial set
        if (manager.getDailyChallenges().isEmpty() && manager.getWeeklyChallenges().isEmpty()) {
            generateInitialChallenges();
            return;
        }

        // Get current time in EST timezone
        ZoneId estZone = ZoneId.of("America/New_York");
        ZonedDateTime now = ZonedDateTime.now(estZone);

        // Check if we need to refresh daily challenges (after 12 PM EST on a new day)
        long lastDailyRefresh = manager.getLastDailyRefreshTime();
        ZonedDateTime lastDailyRefreshDate = Instant.ofEpochMilli(lastDailyRefresh)
                .atZone(estZone);

        if (lastDailyRefresh == 0 ||
                (now.getHour() >= REFRESH_HOUR_EST &&
                        (now.toLocalDate().isAfter(lastDailyRefreshDate.toLocalDate())))) {
            generateDailyChallenges();
        }

        // Check if we need to refresh weekly challenges (after 12 PM EST on Monday)
        long lastWeeklyRefresh = manager.getLastWeeklyRefreshTime();
        ZonedDateTime lastWeeklyRefreshDate = Instant.ofEpochMilli(lastWeeklyRefresh)
                .atZone(estZone);

        if (lastWeeklyRefresh == 0 ||
                (now.getHour() >= REFRESH_HOUR_EST &&
                        now.getDayOfWeek() == DayOfWeek.MONDAY &&
                        (now.toLocalDate().isAfter(lastWeeklyRefreshDate.toLocalDate()) ||
                                lastWeeklyRefreshDate.getDayOfWeek() != DayOfWeek.MONDAY))) {
            generateWeeklyChallenges();
        }

        // Save the updated timestamps
        saveConfig();
    }

    /**
     * Generate initial set of challenges
     */
    private static void generateInitialChallenges() {
        generateDailyChallenges();
        generateWeeklyChallenges();
    }

    public static void generateDailyChallenges() {
        ChallengeManager manager = ChallengeManager.getInstance();

        // Save IDs of existing challenges to clear their completion status
        Set<String> oldChallengeIds = manager.getDailyChallenges().stream()
                .map(Challenge::getId)
                .collect(Collectors.toSet());

        // Clear existing daily challenges
        manager.getDailyChallenges().clear();

        // Get all available daily challenges
        List<Challenge> allDailyChallenges = ChallengeDefinitions.getAllDailyChallenges();

        // Randomly select NUM_DAILY_CHALLENGES from the list
        List<Challenge> selectedChallenges = selectRandomChallenges(allDailyChallenges, NUM_DAILY_CHALLENGES);

        // Add the selected challenges to the manager
        for (Challenge challenge : selectedChallenges) {
            manager.addDailyChallenge(challenge);

            // Clear completion status for this challenge if it was previously completed
            manager.clearCompletionStatus(challenge.getId());
        }

        // Update the last refresh time
        manager.setLastDailyRefreshTime(System.currentTimeMillis());

        // Save data after generating new challenges
        manager.saveData(config);
        try {
            config.save();
            logger.info("Saved config after generating daily challenges");
        } catch (Exception e) {
            logger.error("Error saving config after generating daily challenges: " + e.getMessage());
        }

        logger.info("Generated " + selectedChallenges.size() + " new daily challenges");
    }

    public static void generateWeeklyChallenges() {
        ChallengeManager manager = ChallengeManager.getInstance();

        // Clear existing weekly challenges
        manager.getWeeklyChallenges().clear();

        // Get all available weekly challenges
        List<Challenge> allWeeklyChallenges = ChallengeDefinitions.getAllWeeklyChallenges();

        // Randomly select NUM_WEEKLY_CHALLENGES from the list
        List<Challenge> selectedChallenges = selectRandomChallenges(allWeeklyChallenges, NUM_WEEKLY_CHALLENGES);

        // Add the selected challenges to the manager
        for (Challenge challenge : selectedChallenges) {
            manager.addWeeklyChallenge(challenge);

            manager.clearCompletionStatus(challenge.getId());
        }

        // Update the last refresh time
        manager.setLastWeeklyRefreshTime(System.currentTimeMillis());

        // Save data after generating new challenges
        manager.saveData(config);
        try {
            config.save();
            logger.info("Saved config after generating weekly challenges");
        } catch (Exception e) {
            logger.error("Error saving config after generating weekly challenges: " + e.getMessage());
        }

        logger.info("Generated " + selectedChallenges.size() + " new weekly challenges");
    }

    /**
     * Helper method to select random challenges from a list
     */
    private static List<Challenge> selectRandomChallenges(List<Challenge> allChallenges, int count) {
        List<Challenge> result = new ArrayList<>();

        // Create a copy of the list to avoid modifying the original
        List<Challenge> availableChallenges = new ArrayList<>(allChallenges);

        // Ensure we don't try to select more challenges than available
        count = Math.min(count, availableChallenges.size());

        // Randomly select challenges
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(availableChallenges.size());
            result.add(availableChallenges.remove(index));
        }

        return result;
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

    @Override
    public void receiveCardUsed(AbstractCard abstractCard) {

    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {

    }

}
