package spirepass;

import basemod.*;
import basemod.interfaces.*;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;
import spirepass.challengeutil.*;
import spirepass.spirepassutil.SkinManager;
import spirepass.util.GeneralUtils;
import spirepass.util.TextureLoader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static spirepass.challengeutil.ChallengeManager.LAST_DAILY_REFRESH;
import static spirepass.challengeutil.ChallengeManager.LAST_WEEKLY_REFRESH;

@SpireInitializer
public class Spirepass implements
        EditStringsSubscriber,
        PostInitializeSubscriber,
        OnCardUseSubscriber,
        OnPlayerTurnStartSubscriber,
        AddAudioSubscriber,
        PostUpdateSubscriber {
    // ==================== METADATA & STATIC CONSTANTS ====================

    public static ModInfo info;
    public static String modID;
    static { loadModInfo(); }
    private static final String resourcesFolder = checkResourcesPath();
    public static final Logger logger = LogManager.getLogger(modID);

    // ==================== CONFIG CONSTANTS ====================

    private static final String JUMP_TO_LEVEL_KEY = "jumpToCurrentLevel";
    private static final String ENABLE_MENU_ELEMENTS_KEY = "enableMainMenuElements";
    private static final String PLAY_CHALLENGE_SOUND_KEY = "playChallengeCompleteSound";
    private static final String HIDE_SPIREPASS_PREMIUM = "hideSpirepassPremium";
    private static final String MARCUS = "marcus";
    private static final String TOTAL_XP_KEY = "totalXP";
    // ==================== AUDIO ====================

    public static final String DRUM_KEY = "Drum";
    public static final String DRUM_OGG = "spirepass/audio/Drum.ogg";

    // ==================== LEVEL & CHALLENGE CONSTANTS ====================

    public static final int XP_PER_LEVEL = 50;
    public static final int DAILY_CHALLENGE_XP = 25;
    public static final int WEEKLY_CHALLENGE_XP = 75;
    public static final int MAX_LEVEL = 35;
    public static final int REFRESH_HOUR_LOCAL = 12;
    public static final int REFRESH_MINUTE_LOCAL = 00;

    private static final int NUM_DAILY_CHALLENGES = 3;
    private static final int NUM_WEEKLY_CHALLENGES = 3;
    private static final int REFRESH_HOUR_EST = 12; // 12 PM EST

    // ==================== STATE VARIABLES ====================

    public static boolean jumpToCurrentLevel;
    public static boolean hideSpirepassPremium;
    public static boolean enableMainMenuElements;
    public static boolean playChallengeCompleteSound;
    public static boolean marcus;
    private static int totalXP = 0;
    public static SpireConfig config;

    private float saveTimer = 0f;
    private static final float SAVE_INTERVAL = 10f; // Save every 10 seconds

    // ==================== INITIALIZATION ====================

    public static String makeID(String id) {
        return modID + ":" + id;
    }

    public static void initialize() {
        try {
            Properties defaults = new Properties();

            // Get manager instances
            SkinManager skinManager = SkinManager.getInstance();
            skinManager.addDefaultProperties(defaults);

            // Challenge system default timestamps
            defaults.setProperty(LAST_DAILY_REFRESH, String.valueOf(0));
            defaults.setProperty(LAST_WEEKLY_REFRESH, String.valueOf(0));

            // Add XP default
            defaults.setProperty(TOTAL_XP_KEY, "0");

            // Config options defaults
            defaults.setProperty(JUMP_TO_LEVEL_KEY, "true");
            defaults.setProperty(HIDE_SPIREPASS_PREMIUM, "false");
            defaults.setProperty(ENABLE_MENU_ELEMENTS_KEY, "true");
            defaults.setProperty(PLAY_CHALLENGE_SOUND_KEY, "false");
            defaults.setProperty(MARCUS, "false");

            // Initialize config with defaults
            config = new SpireConfig(modID, "config", defaults);

            // Load config values
            jumpToCurrentLevel = config.getBool(JUMP_TO_LEVEL_KEY);
            hideSpirepassPremium = config.getBool(HIDE_SPIREPASS_PREMIUM);
            enableMainMenuElements = config.getBool(ENABLE_MENU_ELEMENTS_KEY);
            playChallengeCompleteSound = config.getBool(PLAY_CHALLENGE_SOUND_KEY);
            marcus = config.getBool(MARCUS);

            // Load XP data after config is initialized
            if (config.has(TOTAL_XP_KEY)) {
                totalXP = config.getInt(TOTAL_XP_KEY);
            }

            // Load skin data AFTER config is fully initialized
            skinManager.loadData(config);

            // Initialize challenge manager and load challenge data
            ChallengeManager challengeManager = ChallengeManager.getInstance();
            challengeManager.loadData(config);

            // Save the config after everything is loaded
            config.save();

        } catch (Exception e) {
            e.printStackTrace();
        }
        new Spirepass();
    }

    public Spirepass() {
        BaseMod.subscribe(this);
    }

    // ==================== CONFIG MANAGEMENT ====================

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
        } catch (Exception e) {
            // Error handling
        }
    }

    // ==================== UI SETUP ====================

    @Override
    public void receivePostInitialize() {
        checkAndRefreshChallenges();
        ModPanel settingsPanel = new ModPanel();
        UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("ConfigMenuText"));
        String[] TEXT = uiStrings.TEXT;
        float toggleX = 350.0f;
        float toggleStartY = 750.0f;
        float toggleSpacing = 50.0f;
        final ModLabeledToggleButton[] menuToggleRef = new ModLabeledToggleButton[1];
        final ModLabeledToggleButton[] soundToggleRef = new ModLabeledToggleButton[1];
        ModLabeledToggleButton jumpToggle = new ModLabeledToggleButton(TEXT[0],
                toggleX, toggleStartY, Settings.CREAM_COLOR, FontHelper.charDescFont,
                jumpToCurrentLevel,
                settingsPanel, (label) -> {}, (button) -> {
            jumpToCurrentLevel = button.enabled;
            try {
                config.setBool(JUMP_TO_LEVEL_KEY, jumpToCurrentLevel);
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        settingsPanel.addUIElement(jumpToggle);
        ModLabeledToggleButton hideSpirepassPremiumToggle = new ModLabeledToggleButton(TEXT[3],
                toggleX, toggleStartY - toggleSpacing, Settings.CREAM_COLOR, FontHelper.charDescFont,
                hideSpirepassPremium,
                settingsPanel, (label) -> {}, (button) -> {
            hideSpirepassPremium = button.enabled;
            try {
                config.setBool(HIDE_SPIREPASS_PREMIUM, hideSpirepassPremium);
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        settingsPanel.addUIElement(hideSpirepassPremiumToggle);
        ModLabeledToggleButton menuElementsToggle = new ModLabeledToggleButton(TEXT[1],
                toggleX, toggleStartY - (toggleSpacing * 2), Settings.CREAM_COLOR, FontHelper.charDescFont,
                enableMainMenuElements,
                settingsPanel, (label) -> {}, (button) -> {
            boolean nowEnabled = button.enabled;
            enableMainMenuElements = nowEnabled;
            try {
                config.setBool(ENABLE_MENU_ELEMENTS_KEY, nowEnabled);
                if (!nowEnabled) {
                    ModLabeledToggleButton soundToggle = soundToggleRef[0];
                    if (soundToggle != null && soundToggle.toggle.enabled) {
                        soundToggle.toggle.enabled = false;
                        playChallengeCompleteSound = false;
                        config.setBool(PLAY_CHALLENGE_SOUND_KEY, false);
                    }
                }
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        settingsPanel.addUIElement(menuElementsToggle);
        menuToggleRef[0] = menuElementsToggle;
        ModLabeledToggleButton playSoundToggle = new ModLabeledToggleButton(TEXT[2],
                toggleX, toggleStartY - (toggleSpacing * 3), Settings.CREAM_COLOR, FontHelper.charDescFont,
                enableMainMenuElements && playChallengeCompleteSound,
                settingsPanel, (label) -> {}, (button) -> {
            boolean nowEnabled = button.enabled;
            playChallengeCompleteSound = nowEnabled;
            try {
                config.setBool(PLAY_CHALLENGE_SOUND_KEY, nowEnabled);
                if (nowEnabled) {
                    ModLabeledToggleButton menuToggle = menuToggleRef[0];
                    if (menuToggle != null && !menuToggle.toggle.enabled) {
                        menuToggle.toggle.enabled = true;
                        enableMainMenuElements = true;
                        config.setBool(ENABLE_MENU_ELEMENTS_KEY, true);
                    }
                }
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        settingsPanel.addUIElement(playSoundToggle);
        soundToggleRef[0] = playSoundToggle;
        ModLabeledToggleButton marcusToggle = new ModLabeledToggleButton(TEXT[4],
                toggleX, toggleStartY - (toggleSpacing * 4), Settings.CREAM_COLOR, FontHelper.charDescFont,
                marcus,
                settingsPanel, (label) -> {}, (button) -> {
            marcus = button.enabled;
            try {
                config.setBool(MARCUS, marcus);
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        settingsPanel.addUIElement(marcusToggle);
        if (!menuElementsToggle.toggle.enabled && playSoundToggle.toggle.enabled) {
            playSoundToggle.toggle.enabled = false;
        }
        ModLabeledButton xpButton = new ModLabeledButton(
                TEXT[5], // "Add 10,000 XP"
                9999.0f,
                9999.0f,
                Settings.GOLD_COLOR,
                Settings.GOLD_COLOR,
                FontHelper.buttonLabelFont,
                settingsPanel,
                (button) -> {
                    addXP(10000);
                }
        );
    /* Dev feature - commented out
    ModLabeledButton resetXpButton = new ModLabeledButton(
            TEXT[6], // "Reset XP to 0"
            9999.0f,
            9999.0f,
            Settings.RED_TEXT_COLOR,
            Settings.RED_TEXT_COLOR,
            FontHelper.buttonLabelFont,
            settingsPanel,
            (button) -> {
                totalXP = 0;
                if (config != null) {
                    config.setInt(TOTAL_XP_KEY, 0);
                    saveConfig();
                    // logger.info("Reset XP to 0 via secret button");
                } else {
                    logger.error("Config not initialized when trying to reset XP!");
                }
            }
    );
    settingsPanel.addUIElement(resetXpButton);
    */
        settingsPanel.addUIElement(xpButton);
        // Create a secret area detector that exactly matches button hitboxes
        class SecretAreaElement implements IUIElement {
            private final float BUTTON_X = Settings.WIDTH / Settings.scale - 650.0f;
            private final float BUTTON_Y_BOTTOM = 250.0f;
            private final float BUTTON_Y_SPACING = 70.0f;
            private final float BUTTON_Y_TOP = BUTTON_Y_BOTTOM + BUTTON_Y_SPACING;
            private boolean areButtonsVisible = false;
            private Hitbox topButtonHitbox;
            /* Dev feature - commented out
            private Hitbox bottomButtonHitbox;
            */
            public SecretAreaElement() {
                String topButtonText = TEXT[5]; // "Add 10,000 XP"
            /* Dev feature - commented out
            String bottomButtonText = TEXT[6]; // "Reset XP to 0"
            */
                Texture textureLeft = ImageMaster.loadImage("img/ButtonLeft.png");
                Texture textureRight = ImageMaster.loadImage("img/ButtonRight.png");
                float buttonHeight = textureLeft.getHeight() * Settings.scale;
                float topTextWidth = FontHelper.getSmartWidth(FontHelper.buttonLabelFont, topButtonText, 9999.0f, 0.0f);
                float topMiddleWidth = Math.max(0.0f, topTextWidth - 18.0f * Settings.scale);
                float topButtonWidth = (textureLeft.getWidth() + textureRight.getWidth()) * Settings.scale + topMiddleWidth;
                topButtonHitbox = new Hitbox(
                        BUTTON_X * Settings.scale + 1.0f * Settings.scale,
                        BUTTON_Y_TOP * Settings.scale + 1.0f * Settings.scale,
                        topButtonWidth - 2.0f * Settings.scale,
                        buttonHeight - 2.0f * Settings.scale
                );
            /* Dev feature - commented out
            float bottomTextWidth = FontHelper.getSmartWidth(FontHelper.buttonLabelFont, bottomButtonText, 9999.0f, 0.0f);
            float bottomMiddleWidth = Math.max(0.0f, bottomTextWidth - 18.0f * Settings.scale);
            float bottomButtonWidth = (textureLeft.getWidth() + textureRight.getWidth()) * Settings.scale + bottomMiddleWidth;
            bottomButtonHitbox = new Hitbox(
                    BUTTON_X * Settings.scale + 1.0f * Settings.scale,
                    BUTTON_Y_BOTTOM * Settings.scale + 1.0f * Settings.scale,
                    bottomButtonWidth - 2.0f * Settings.scale,
                    buttonHeight - 2.0f * Settings.scale
            );
            */
            }
            @Override
            public void render(SpriteBatch sb) {
            }
            @Override
            public void update() {
                topButtonHitbox.update();
            /* Dev feature - commented out
            bottomButtonHitbox.update();
            boolean isHoveringOverButtonArea = topButtonHitbox.hovered || bottomButtonHitbox.hovered;
            */
                boolean isHoveringOverButtonArea = topButtonHitbox.hovered;
                if (isHoveringOverButtonArea != areButtonsVisible) {
                    areButtonsVisible = isHoveringOverButtonArea;
                    if (isHoveringOverButtonArea) {
                        xpButton.set(BUTTON_X, BUTTON_Y_TOP);
                    /* Dev feature - commented out
                    resetXpButton.set(BUTTON_X, BUTTON_Y_BOTTOM);
                    */
                    } else {
                        xpButton.set(9999.0f, 9999.0f);
                    /* Dev feature - commented out
                    resetXpButton.set(9999.0f, 9999.0f);
                    */
                    }
                }
            }
            @Override public int renderLayer() { return 1; }
            @Override public int updateOrder() { return 1; }
        }
        settingsPanel.addUIElement(new SecretAreaElement());
        Texture badgeTexture = TextureLoader.getTexture(imagePath("badge.png"));
        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors),
                info.Description, settingsPanel);
    }

    // ==================== CHALLENGE MANAGEMENT ====================

    private static void checkAndRefreshChallenges() {
        ChallengeManager manager = ChallengeManager.getInstance();

        // Check if any challenges exist - if not, generate initial set
        boolean needInitialChallenges = manager.getDailyChallenges().isEmpty() ||
                manager.getWeeklyChallenges().isEmpty();
        if (needInitialChallenges) {
            generateInitialChallenges();
            return;
        }

        // Get current time and last refresh times
        Calendar now = Calendar.getInstance();

        Calendar lastDailyRefreshDate = Calendar.getInstance();
        lastDailyRefreshDate.setTimeInMillis(manager.getLastDailyRefreshTime());

        Calendar lastWeeklyRefreshDate = Calendar.getInstance();
        lastWeeklyRefreshDate.setTimeInMillis(manager.getLastWeeklyRefreshTime());

        // Check if we need to refresh daily challenges
        if (needsDailyRefresh(now, lastDailyRefreshDate)) {
            generateDailyChallenges();
        }

        // Check if we need to refresh weekly challenges
        if (needsWeeklyRefresh(now, lastWeeklyRefreshDate)) {
            generateWeeklyChallenges();
        }

        // Save the updated timestamps
        saveConfig();
    }

    private static boolean needsDailyRefresh(Calendar now, Calendar lastRefresh) {
        // If lastRefresh is effectively zero (or very old), assume refresh is needed
        // Note: Relies on initial generation setting a valid timestamp.
        if (lastRefresh.getTimeInMillis() < 1000) { // Check against a small value to avoid issues with exact zero
            // Consider if getDailyChallenges().isEmpty() is a better check for initial state
            return true;
        }

        // Calculate the *next* noon that should have occurred after the last refresh.
        Calendar nextPotentialRefreshTime = (Calendar) lastRefresh.clone();
        nextPotentialRefreshTime.set(Calendar.HOUR_OF_DAY, REFRESH_HOUR_LOCAL);
        nextPotentialRefreshTime.set(Calendar.MINUTE, REFRESH_MINUTE_LOCAL);
        nextPotentialRefreshTime.set(Calendar.SECOND, 0);
        nextPotentialRefreshTime.set(Calendar.MILLISECOND, 0);

        // If the last refresh happened *at or after* noon on its day,
        // the next refresh target is noon on the *following* day.
        if (!lastRefresh.before(nextPotentialRefreshTime)) {
            nextPotentialRefreshTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Refresh if the current time 'now' is after that next potential refresh time.
        return now.after(nextPotentialRefreshTime);
    }

    // Helper method for weekly refresh check (Unchanged - Logic appears correct)
    private static boolean needsWeeklyRefresh(Calendar now, Calendar lastRefresh) {
        // Find the date of this week's Monday at refresh time
        Calendar thisWeeksMonday = (Calendar) now.clone();
        thisWeeksMonday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        thisWeeksMonday.set(Calendar.HOUR_OF_DAY, REFRESH_HOUR_LOCAL);
        thisWeeksMonday.set(Calendar.MINUTE, REFRESH_MINUTE_LOCAL);
        thisWeeksMonday.set(Calendar.SECOND, 0);
        thisWeeksMonday.set(Calendar.MILLISECOND, 0); // Good practice to zero out ms too

        // If 'now' is before this week's Monday noon (e.g., Sunday),
        // the relevant boundary is *last* week's Monday noon.
        if (now.before(thisWeeksMonday)) {
            thisWeeksMonday.add(Calendar.DAY_OF_MONTH, -7);
        }

        // Refresh if the last refresh occurred before that calculated Monday noon boundary.
        // Also handle the initial case where lastRefresh time might be 0.
        return lastRefresh.getTimeInMillis() < 1000 || lastRefresh.before(thisWeeksMonday);
    }

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
        List<Challenge> selectedChallenges = selectRandomChallengesWithConstraints(allDailyChallenges, NUM_DAILY_CHALLENGES);

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
    }

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
        List<Challenge> selectedChallenges = selectRandomChallengesWithConstraints(allWeeklyChallenges, NUM_WEEKLY_CHALLENGES);

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
    }

    /*

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

     */

    /**
     * Select random challenges with additional constraints
     * This method ensures that daily_storytime and daily_unbeatable are never selected together
     */
    private static List<Challenge> selectRandomChallengesWithConstraints(List<Challenge> allChallenges, int count) {
        List<Challenge> result = new ArrayList<>();
        // Create a copy of the list to avoid modifying the original
        List<Challenge> availableChallenges = new ArrayList<>(allChallenges);
        // Ensure we don't try to select more challenges than available
        count = Math.min(count, availableChallenges.size());

        Random random = new Random();
        boolean hasStorytime = false;
        boolean hasUnbeatable = false;

        // First pass: Randomly select challenges
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(availableChallenges.size());
            Challenge selectedChallenge = availableChallenges.remove(index);

            // Track if we've selected either of our constrained challenges
            if (selectedChallenge.getId().equals("daily_storytime")) {
                hasStorytime = true;
            } else if (selectedChallenge.getId().equals("daily_unbeatable")) {
                hasUnbeatable = true;
            }

            result.add(selectedChallenge);
        }

        // Second pass: If we selected both constrained challenges, replace one
        if (hasStorytime && hasUnbeatable && count < allChallenges.size()) {
            // Decide which one to replace (50/50 chance)
            boolean replaceStorytime = random.nextBoolean();

            // Find the challenge to replace
            Challenge toReplace = null;
            for (Challenge challenge : result) {
                if ((replaceStorytime && challenge.getId().equals("daily_storytime")) ||
                        (!replaceStorytime && challenge.getId().equals("daily_unbeatable"))) {
                    toReplace = challenge;
                    break;
                }
            }

            if (toReplace != null) {
                // Remove the challenge
                result.remove(toReplace);

                // Find a replacement that isn't already selected or one of the constrained ones
                List<Challenge> possibleReplacements = allChallenges.stream()
                        .filter(c -> !result.contains(c))
                        .filter(c -> !(replaceStorytime ?
                                c.getId().equals("daily_storytime") :
                                c.getId().equals("daily_unbeatable")))
                        .collect(Collectors.toList());

                if (!possibleReplacements.isEmpty()) {
                    int replacementIndex = random.nextInt(possibleReplacements.size());
                    result.add(possibleReplacements.get(replacementIndex));
                } else {
                    // Fallback: re-add the challenge we removed if no replacements available
                    result.add(toReplace);
                }
            }
        }

        return result;
    }

    // ==================== DATE HELPERS ====================

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isMonday(Calendar cal) {
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    private static boolean isSameWeek(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }

    // ==================== XP MANAGEMENT ====================

    public static int getCurrentLevel() {
        int level = totalXP / XP_PER_LEVEL;
        return Math.min(level, MAX_LEVEL);
    }

    public static int getXPForNextLevel() {
        if (getCurrentLevel() >= MAX_LEVEL) {
            return 0;
        }
        return ((getCurrentLevel() + 1) * XP_PER_LEVEL) - totalXP;
    }

    public static void addXP(int amount) {
        int oldLevel = getCurrentLevel();
        totalXP += amount;

        if (config != null) {
            config.setInt(TOTAL_XP_KEY, totalXP);
            saveConfig();
        }

        int newLevel = getCurrentLevel();
        if (newLevel > oldLevel) {
            // Level up event
        }
    }

    // ==================== LOCALIZATION ====================

    private static String getLangString() {
        return Settings.language.name().toLowerCase();
    }

    private static final String defaultLanguage = "eng";

    @Override
    public void receiveEditStrings() {
        loadLocalization(defaultLanguage);
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
        BaseMod.loadCustomStringsFile(UIStrings.class,
                localizationPath(lang, "UIStrings.json"));
    }

    public static String localizationPath(String lang, String file) {
        return resourcesFolder + "/localization/" + lang + "/" + file;
    }

    public static String imagePath(String file) {
        return resourcesFolder + "/images/" + file;
    }

    // ==================== RESOURCE LOADING ====================

    private static String checkResourcesPath() {
        String name = Spirepass.class.getName();
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

    // ==================== GAME EVENT HOOKS ====================

    @Override
    public void receiveCardUsed(AbstractCard abstractCard) {
        if (ChallengeHelper.isActiveChallengeIncomplete("daily_setup")) {
            if (abstractCard.type == AbstractCard.CardType.POWER) {
                if (AbstractDungeon.actionManager.turn == 1) {
                    ChallengeVariables.dailySetupPowersPlayed++;
                    if (ChallengeVariables.dailySetupPowersPlayed >= 2) {
                        ChallengeHelper.completeChallenge("daily_setup");
                    }
                }
            }
        }

        if (ChallengeHelper.isActiveChallengeIncomplete("daily_combo")) {
            ChallengeVariables.dailyComboCardsPlayedThisTurn++;
            if (ChallengeVariables.dailyComboCardsPlayedThisTurn >= 10) {
                ChallengeHelper.completeChallenge("daily_combo");
            }
        }

        if (ChallengeHelper.isActiveChallengeIncomplete("weekly_midas")) {
            if (abstractCard.rarity == AbstractCard.CardRarity.RARE) {
                ChallengeHelper.updateChallengeProgress("weekly_midas", 1);
            }
        }
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
        }
    }

    @Override
    public void receiveAddAudio() {
        BaseMod.addAudio(DRUM_KEY, DRUM_OGG);
    }
}