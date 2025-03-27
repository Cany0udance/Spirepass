package spirepass;

import basemod.BaseMod;
import basemod.IUIElement;
import basemod.ModLabeledButton;
import basemod.ModPanel;
import basemod.interfaces.*;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;
import spirepass.challengeutil.*;
import spirepass.spirepassutil.SkinManager;
import spirepass.util.GeneralUtils;
import spirepass.util.KeywordInfo;
import spirepass.util.TextureLoader;

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
        OnStartBattleSubscriber,
        OnPlayerTurnStartSubscriber,
        PostUpdateSubscriber {
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
    public static final int REFRESH_HOUR_LOCAL = 12;
    public static final int REFRESH_MINUTE_LOCAL = 00;
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
//                 logger.info("Loaded battle pass XP: " + totalXP);
            }

            // Load skin data AFTER config is fully initialized
            skinManager.loadData(config);

            // Initialize challenge manager and load challenge data
            ChallengeManager challengeManager = ChallengeManager.getInstance();
            // Load challenge data
            challengeManager.loadData(config);
            // Check if we need to generate/refresh challenges
            checkAndRefreshChallenges();

            // Important: Save the config after everything is loaded
            // This ensures any missing properties get saved with defaults
            config.save();

        } catch (Exception e) {
//             logger.error("Failed to load config: " + e.getMessage());
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
//             logger.info("Saved all config data");
        } catch (Exception e) {
//             logger.error("Failed to save config: " + e.getMessage());
        }
    }

    public Spirepass() {
        BaseMod.subscribe(this);
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
                Settings.GOLD_COLOR, // Normal color - gold
                Settings.GOLD_COLOR, // Hover color - SAME as normal color
                FontHelper.buttonLabelFont,
                settingsPanel,
                (button) -> {
                    // When clicked, add 10,000 XP
                    addXP(10000);
//                     logger.info("Added 10,000 XP via secret button");
                    // Keep the button visible (don't move off-screen)
                }
        );

// Create the Reset XP button - initially positioned off-screen
        ModLabeledButton resetXpButton = new ModLabeledButton(
                "Reset XP to 0",
                9999.0f, // Off-screen X position
                9999.0f, // Off-screen Y position
                Settings.RED_TEXT_COLOR, // Normal color - red
                Settings.RED_TEXT_COLOR, // Hover color - SAME as normal color
                FontHelper.buttonLabelFont,
                settingsPanel,
                (button) -> {
                    // Reset XP to 0
                    totalXP = 0;
                    config.setInt(TOTAL_XP_KEY, 0);
                    saveConfig();
//                     logger.info("Reset XP to 0 via secret button");
                    // Keep the button visible (don't move off-screen)
                }
        );

        // Add both buttons to the panel
        settingsPanel.addUIElement(xpButton);
        settingsPanel.addUIElement(resetXpButton);

        // Create a secret area detector that exactly matches button hitboxes
        class SecretAreaElement implements IUIElement {
            // Position values for our buttons - easy to adjust
            private final float BUTTON_X = Settings.WIDTH / 2.0f / Settings.scale - 75.0f;
            private final float BUTTON_Y_TOP = Settings.HEIGHT / 2.0f / Settings.scale - 10.0f;
            private final float BUTTON_Y_SPACING = 70.0f; // Increased spacing between buttons
            private final float BUTTON_Y_BOTTOM = BUTTON_Y_TOP - BUTTON_Y_SPACING;

            // Track when buttons become visible
            private boolean areButtonsVisible = false;

            // Holds hitbox references that match exactly where the buttons would be
            private Hitbox topButtonHitbox;
            private Hitbox bottomButtonHitbox;

            public SecretAreaElement() {
                // Create invisible hitboxes to match exactly where buttons would appear
                // We use the same hitbox size logic as ModLabeledButton

                // For Add XP button
                float topTextWidth = FontHelper.getSmartWidth(
                        FontHelper.buttonLabelFont,
                        "Add 10,000 XP",
                        9999.0f,
                        0.0f
                );
                float topMiddleWidth = Math.max(0.0f, topTextWidth - 18.0f * Settings.scale);
                // Use the same textures as the ModLabeledButton class
                Texture textureLeft = ImageMaster.loadImage("img/ButtonLeft.png");
                Texture textureRight = ImageMaster.loadImage("img/ButtonRight.png");

                float topButtonWidth = (textureLeft.getWidth() + textureRight.getWidth()) * Settings.scale + topMiddleWidth;
                float buttonHeight = textureLeft.getHeight() * Settings.scale;

                // Hitbox for top button - use exact same offsets as ModLabeledButton
                topButtonHitbox = new Hitbox(
                        BUTTON_X * Settings.scale + 1.0f * Settings.scale,
                        BUTTON_Y_TOP * Settings.scale + 1.0f * Settings.scale,
                        topButtonWidth - 2.0f * Settings.scale,
                        buttonHeight - 2.0f * Settings.scale
                );

                // For Reset XP button
                float bottomTextWidth = FontHelper.getSmartWidth(
                        FontHelper.buttonLabelFont,
                        "Reset XP to 0",
                        9999.0f,
                        0.0f
                );
                float bottomMiddleWidth = Math.max(0.0f, bottomTextWidth - 18.0f * Settings.scale);
                float bottomButtonWidth = (textureLeft.getWidth() + textureRight.getWidth()) * Settings.scale + bottomMiddleWidth;

                // Hitbox for bottom button
                bottomButtonHitbox = new Hitbox(
                        BUTTON_X * Settings.scale + 1.0f * Settings.scale,
                        BUTTON_Y_BOTTOM * Settings.scale + 1.0f * Settings.scale,
                        bottomButtonWidth - 2.0f * Settings.scale,
                        buttonHeight - 2.0f * Settings.scale
                );
            }

            @Override
            public void render(SpriteBatch sb) {
                // Optional debugging - uncomment to see hitboxes
                // topButtonHitbox.render(sb);
                // bottomButtonHitbox.render(sb);
            }

            @Override
            public void update() {
                // Update our invisible hitboxes
                topButtonHitbox.update();
                bottomButtonHitbox.update();

                // Check if mouse is hovering over either hitbox
                boolean isHoveringOverButtonArea = topButtonHitbox.hovered || bottomButtonHitbox.hovered;

                // Only take action if the state changes
                if (isHoveringOverButtonArea != areButtonsVisible) {
                    areButtonsVisible = isHoveringOverButtonArea;

                    if (isHoveringOverButtonArea) {
                        // Show buttons in their proper positions
                        xpButton.set(BUTTON_X, BUTTON_Y_TOP);
                        resetXpButton.set(BUTTON_X, BUTTON_Y_BOTTOM);
                    } else {
                        // Hide buttons
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
    // Replace the existing checkAndRefreshChallenges method with this one
    private static void checkAndRefreshChallenges() {
        ChallengeManager manager = ChallengeManager.getInstance();

        // Check if any challenges exist - if not, generate initial set
        boolean needInitialChallenges = manager.getDailyChallenges().isEmpty() ||
                manager.getWeeklyChallenges().isEmpty();

        if (needInitialChallenges) {
//             logger.info("No challenges found, generating initial set");
            generateInitialChallenges();
            return;
        }

        // Use local Calendar instance
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        // Check if current time has passed the refresh time
        boolean passedRefreshTime = (currentHour > REFRESH_HOUR_LOCAL ||
                (currentHour == REFRESH_HOUR_LOCAL && currentMinute >= REFRESH_MINUTE_LOCAL));

        // Get the last refresh timestamps
        long lastDailyRefresh = manager.getLastDailyRefreshTime();
        long lastWeeklyRefresh = manager.getLastWeeklyRefreshTime();

        // Convert timestamps to Calendar objects
        Calendar lastDailyRefreshDate = Calendar.getInstance();
        lastDailyRefreshDate.setTimeInMillis(lastDailyRefresh);

        Calendar lastWeeklyRefreshDate = Calendar.getInstance();
        lastWeeklyRefreshDate.setTimeInMillis(lastWeeklyRefresh);

        // Check if we need to refresh daily challenges
        // This happens if we've passed the refresh time today AND
        // the last refresh was before today or before today's refresh time
        boolean needsDailyRefresh = passedRefreshTime &&
                (isSameDay(now, lastDailyRefreshDate) ?
                        (lastDailyRefreshDate.get(Calendar.HOUR_OF_DAY) < REFRESH_HOUR_LOCAL ||
                                (lastDailyRefreshDate.get(Calendar.HOUR_OF_DAY) == REFRESH_HOUR_LOCAL &&
                                        lastDailyRefreshDate.get(Calendar.MINUTE) < REFRESH_MINUTE_LOCAL)) :
                        true);

        if (needsDailyRefresh) {
//             logger.info("Daily challenges need refresh");
            generateDailyChallenges();
        }

        // Check if we need to refresh weekly challenges
        // This happens if it's Monday, we've passed the refresh time,
        // and the last refresh was earlier this Monday or before this Monday
        boolean isMonday = currentDayOfWeek == Calendar.MONDAY;
        boolean needsWeeklyRefresh = isMonday && passedRefreshTime &&
                (isSameDay(now, lastWeeklyRefreshDate) ?
                        (lastWeeklyRefreshDate.get(Calendar.HOUR_OF_DAY) < REFRESH_HOUR_LOCAL ||
                                (lastWeeklyRefreshDate.get(Calendar.HOUR_OF_DAY) == REFRESH_HOUR_LOCAL &&
                                        lastWeeklyRefreshDate.get(Calendar.MINUTE) < REFRESH_MINUTE_LOCAL)) :
                        !isMonday(lastWeeklyRefreshDate) || !isSameWeek(now, lastWeeklyRefreshDate));

        if (needsWeeklyRefresh) {
//             logger.info("Weekly challenges need refresh");
            generateWeeklyChallenges();
        }

        // Save the updated timestamps
        saveConfig();
    }

    // Helper methods for date comparison
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isMonday(Calendar cal) {
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    private static boolean isSameWeek(Calendar cal1, Calendar cal2) {
        // Check if same year and week
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Generate initial set of challenges
     */
    private static void generateInitialChallenges() {
        generateDailyChallenges();
        generateWeeklyChallenges();
    }

    // Fix the generateDailyChallenges method in Spirepass
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

        // Clear completion status for old challenges that are no longer active
        for (String oldId : oldChallengeIds) {
            manager.clearCompletionStatus(oldId);
        }

        // Add the selected challenges to the manager
        for (Challenge challenge : selectedChallenges) {
            manager.addDailyChallenge(challenge);
        }

        // Update the last refresh time
        manager.setLastDailyRefreshTime(System.currentTimeMillis());

        // Save data after generating new challenges
        saveConfig();

//         logger.info("Generated " + selectedChallenges.size() + " new daily challenges");
    }

    // Similarly fix the generateWeeklyChallenges method in Spirepass
    public static void generateWeeklyChallenges() {
        ChallengeManager manager = ChallengeManager.getInstance();

        // Save IDs of existing challenges to clear their completion status
        Set<String> oldChallengeIds = manager.getWeeklyChallenges().stream()
                .map(Challenge::getId)
                .collect(Collectors.toSet());

        // Clear existing weekly challenges
        manager.getWeeklyChallenges().clear();

        // Get all available weekly challenges
        List<Challenge> allWeeklyChallenges = ChallengeDefinitions.getAllWeeklyChallenges();

        // Randomly select NUM_WEEKLY_CHALLENGES from the list
        List<Challenge> selectedChallenges = selectRandomChallenges(allWeeklyChallenges, NUM_WEEKLY_CHALLENGES);

        // Clear completion status for old challenges that are no longer active
        for (String oldId : oldChallengeIds) {
            manager.clearCompletionStatus(oldId);
        }

        // Add the selected challenges to the manager
        for (Challenge challenge : selectedChallenges) {
            manager.addWeeklyChallenge(challenge);
        }

        // Update the last refresh time
        manager.setLastWeeklyRefreshTime(System.currentTimeMillis());

        // Save data after generating new challenges
        saveConfig();

//         logger.info("Generated " + selectedChallenges.size() + " new weekly challenges");
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
//             logger.error("Cannot save XP: config is null");
        }

        // Log level up events
        int newLevel = getCurrentLevel();
        if (newLevel > oldLevel) {
//             logger.info("Level up! " + oldLevel + " -> " + newLevel);
        }

//         logger.info("Added " + amount + " XP. Total: " + totalXP + ", Level: " + getCurrentLevel());
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
        // First check if the challenge is active and incomplete
        if (!ChallengeHelper.isActiveChallengeIncomplete("daily_setup")) {
            return; // Skip all processing if challenge is inactive or already completed
        }

        // Check if the card is a Power card
        if (abstractCard.type == AbstractCard.CardType.POWER) {
            // Check if we're in turn 1
            if (AbstractDungeon.actionManager.turn == 1) {
                // Increment the counter for power cards played on turn 1
                ChallengeVariables.dailySetupPowersPlayed++;

                // If we've played 2 or more power cards
                if (ChallengeVariables.dailySetupPowersPlayed >= 2) {
                    // Complete the challenge
                    ChallengeHelper.completeChallenge("daily_setup");

                    // Optional: Log the completion
//                     logger.info("Daily Setup challenge completed! Played 2 power cards on turn 1.");
                }
            }
        }
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {

    }

    @Override
    public void receiveOnPlayerTurnStart() {
        // Reset the variables at the start of each turn
        ChallengeVariables.resetVariablesEveryTurn();
    }

    @Override
    public void receivePostUpdate() {
        // Update timer
        this.saveTimer += Gdx.graphics.getDeltaTime();

        // Check if it's time to check for challenge refreshes
        if (this.saveTimer >= SAVE_INTERVAL) {
            this.saveTimer = 0f;

            // Check if challenges need to be refreshed
            checkAndRefreshChallenges();

            // No need to call saveConfig() here as it's already called in checkAndRefreshChallenges()
        }
    }
}
