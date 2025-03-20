package spirepass;

import basemod.BaseMod;
import basemod.IUIElement;
import basemod.ModLabeledButton;
import basemod.ModPanel;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import spirepass.challengeutil.Challenge;
import spirepass.challengeutil.ChallengeDefinitions;
import spirepass.challengeutil.ChallengeManager;
import spirepass.spirepassutil.SkinManager;
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

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static spirepass.challengeutil.ChallengeManager.LAST_DAILY_REFRESH;
import static spirepass.challengeutil.ChallengeManager.LAST_WEEKLY_REFRESH;

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
    private static final float SAVE_INTERVAL = 10f; // Save every 10 seconds

    // Challenge system related constants
    private static final int NUM_DAILY_CHALLENGES = 3;
    private static final int NUM_WEEKLY_CHALLENGES = 3;
    private static final int REFRESH_HOUR_EST = 12; // 12 PM EST

    // XP and level system constants
    private static final String TOTAL_XP_KEY = "totalXP";
    public static final int XP_PER_LEVEL = 50;
    public static final int DAILY_CHALLENGE_XP = 25;
    public static final int WEEKLY_CHALLENGE_XP = 75;
    public static final int MAX_LEVEL = 30;
    private static int totalXP = 0;

    public static SpireConfig config;

    public static String makeID(String id) {
        return modID + ":" + id;
    }

    public static void initialize() {
        try {
            // Simple config to store skin and cardback preferences
            Properties defaults = new Properties();

            // Get manager instances
            SkinManager skinManager = SkinManager.getInstance();

            // Add default properties for skins
            skinManager.addDefaultProperties(defaults);

            // Challenge system default timestamps
            defaults.setProperty(LAST_DAILY_REFRESH, String.valueOf(0));
            defaults.setProperty(LAST_WEEKLY_REFRESH, String.valueOf(0));

            // Add XP default
            defaults.setProperty(TOTAL_XP_KEY, "0");

            // Initialize config with defaults
            config = new SpireConfig(modID, "config", defaults);

            // Load XP data after config is initialized
            if (config.has(TOTAL_XP_KEY)) {
                totalXP = config.getInt(TOTAL_XP_KEY);
                logger.info("Loaded battle pass XP: " + totalXP);
            }

            // Load skin data
            skinManager.loadData(config);

            // Initialize challenge manager and load challenge data
            ChallengeManager challengeManager = ChallengeManager.getInstance();

            // Load challenge data
            challengeManager.loadData(config);

            // Check if we need to generate/refresh challenges
            checkAndRefreshChallenges();

        } catch (Exception e) {
            logger.error("Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }

        new Spirepass();
    }

    // Update saveConfig method to use SkinManager
    public static void saveConfig() {
        try {
            // Save skin data
            SkinManager.getInstance().saveData(config);

            // Save challenge data and timestamps
            ChallengeManager manager = ChallengeManager.getInstance();
            manager.saveData(config);

            // Save XP data
            config.setInt(TOTAL_XP_KEY, totalXP);

            config.save();
            logger.info("Saved all config data");
        } catch (Exception e) {
            logger.error("Failed to save config: " + e.getMessage());
        }
    }

    public Spirepass() {
        BaseMod.subscribe(this); //This will make BaseMod trigger all the subscribers at their appropriate times.
        logger.info(modID + " subscribed to BaseMod.");
    }

    @Override
    public void receivePostInitialize() {
        // Create the settings panel
        ModPanel settingsPanel = new ModPanel();

        // Create the XP button - initially positioned off-screen
        ModLabeledButton xpButton = new ModLabeledButton(
                "Add 10,000 XP",
                9999.0f, // Off-screen X position
                9999.0f, // Off-screen Y position
                Settings.GOLD_COLOR,
                Settings.CREAM_COLOR,
                FontHelper.buttonLabelFont,
                settingsPanel,
                (button) -> {
                    // When clicked, add 10,000 XP
                    addXP(10000);
                    logger.info("Added 10,000 XP via secret button");

                    // Keep the button visible (don't move off-screen)
                    // This allows for multiple clicks
                }
        );

        // Create the Reset XP button - initially positioned off-screen
        ModLabeledButton resetXpButton = new ModLabeledButton(
                "Reset XP to 0",
                9999.0f, // Off-screen X position
                9999.0f, // Off-screen Y position
                Settings.RED_TEXT_COLOR,
                Settings.CREAM_COLOR,
                FontHelper.buttonLabelFont,
                settingsPanel,
                (button) -> {
                    // Reset XP to 0
                    totalXP = 0;
                    config.setInt(TOTAL_XP_KEY, 0);
                    saveConfig();
                    logger.info("Reset XP to 0 via secret button");

                    // Keep the button visible (don't move off-screen)
                    // This allows for multiple clicks
                }
        );

        // Add both buttons to the panel
        settingsPanel.addUIElement(xpButton);
        settingsPanel.addUIElement(resetXpButton);

        // Create a simple secret area detector
        class SecretAreaElement implements IUIElement {
            // Define the secret area in the exact center of the screen
            private final float AREA_WIDTH = 200.0f;
            private final float AREA_HEIGHT = 150.0f;
            private final float AREA_X = Settings.WIDTH / 2.0f - AREA_WIDTH / 2.0f;
            private final float AREA_Y = Settings.HEIGHT / 2.0f - AREA_HEIGHT / 2.0f;

            // For centering the buttons
            private final float BUTTON_X = Settings.WIDTH / 2.0f / Settings.scale - 75.0f;
            private final float BUTTON_Y_TOP = Settings.HEIGHT / 2.0f / Settings.scale - 10.0f;
            private final float BUTTON_Y_BOTTOM = BUTTON_Y_TOP - 150.0f; // 150 units below the top button

            private boolean wasInArea = false;

            @Override
            public void render(SpriteBatch sb) {
                // No rendering needed
            }

            @Override
            public void update() {
                // Check if the mouse is in the secret area (center of screen)
                boolean isInArea = (InputHelper.mX >= AREA_X &&
                        InputHelper.mX <= AREA_X + AREA_WIDTH &&
                        InputHelper.mY >= AREA_Y &&
                        InputHelper.mY <= AREA_Y + AREA_HEIGHT);

                // Only take action when the hover state changes
                if (isInArea != wasInArea) {
                    wasInArea = isInArea;

                    if (isInArea) {
                        // Position buttons
                        xpButton.set(BUTTON_X, BUTTON_Y_TOP);
                        resetXpButton.set(BUTTON_X, BUTTON_Y_BOTTOM);
                    } else {
                        // Move buttons off-screen
                        xpButton.set(9999.0f, 9999.0f);
                        resetXpButton.set(9999.0f, 9999.0f);
                    }
                }
            }

            // Implementing required methods from IUIElement
            @Override
            public int renderLayer() {
                return 0;
            }

            @Override
            public int updateOrder() {
                return 0;
            }
        }

        // Add the secret area detector
        settingsPanel.addUIElement(new SecretAreaElement());

        // Register the mod badge with the settings panel
        Texture badgeTexture = TextureLoader.getTexture(imagePath("badge.png"));
        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors),
                info.Description, settingsPanel);
    }

    /**
     * Check if challenge lists need to be generated or refreshed based on time
     */
    private static void checkAndRefreshChallenges() {
        ChallengeManager manager = ChallengeManager.getInstance();

        // Get current time in EST timezone
        ZoneId estZone = ZoneId.of("America/New_York");
        ZonedDateTime now = ZonedDateTime.now(estZone);

        // Check if any challenges exist
        boolean needInitialChallenges = manager.getDailyChallenges().isEmpty() ||
                manager.getWeeklyChallenges().isEmpty();

        if (needInitialChallenges) {
            logger.info("No challenges found, generating initial set");
            generateInitialChallenges();
            return;
        }

        // Check daily challenge refresh
        long lastDailyRefresh = manager.getLastDailyRefreshTime();
        if (lastDailyRefresh > 0) {  // Only check if we have a valid timestamp
            ZonedDateTime lastDailyRefreshDate = Instant.ofEpochMilli(lastDailyRefresh)
                    .atZone(estZone);

            boolean needsDailyRefresh = now.getHour() >= REFRESH_HOUR_EST &&
                    now.toLocalDate().isAfter(lastDailyRefreshDate.toLocalDate());

            if (needsDailyRefresh) {
                logger.info("Daily challenges need refresh");
                generateDailyChallenges();
            }
        }

        // Check weekly challenge refresh
        long lastWeeklyRefresh = manager.getLastWeeklyRefreshTime();
        if (lastWeeklyRefresh > 0) {  // Only check if we have a valid timestamp
            ZonedDateTime lastWeeklyRefreshDate = Instant.ofEpochMilli(lastWeeklyRefresh)
                    .atZone(estZone);

            boolean needsWeeklyRefresh = now.getHour() >= REFRESH_HOUR_EST &&
                    now.getDayOfWeek() == DayOfWeek.MONDAY &&
                    (now.toLocalDate().isAfter(lastWeeklyRefreshDate.toLocalDate()) ||
                            lastWeeklyRefreshDate.getDayOfWeek() != DayOfWeek.MONDAY);

            if (needsWeeklyRefresh) {
                logger.info("Weekly challenges need refresh");
                generateWeeklyChallenges();
            }
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
        saveConfig();

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
        saveConfig();

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

    // Get current level based on XP
    public static int getCurrentLevel() {
        int level = totalXP / XP_PER_LEVEL;
        return Math.min(level, MAX_LEVEL);
    }

    // Get XP needed for next level
    public static int getXPForNextLevel() {
        if (getCurrentLevel() >= MAX_LEVEL) {
            return 0; // At max level
        }
        return ((getCurrentLevel() + 1) * XP_PER_LEVEL) - totalXP;
    }

    // Add XP and save
    public static void addXP(int amount) {
        int oldLevel = getCurrentLevel();
        totalXP += amount;

        // Add null check to prevent NPE
        if (config != null) {
            config.setInt(TOTAL_XP_KEY, totalXP);
            saveConfig();
        } else {
            logger.error("Cannot save XP: config is null");
        }

        // Log level up events
        int newLevel = getCurrentLevel();
        if (newLevel > oldLevel) {
            logger.info("Level up! " + oldLevel + " -> " + newLevel);
        }

        logger.info("Added " + amount + " XP. Total: " + totalXP + ", Level: " + getCurrentLevel());
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
