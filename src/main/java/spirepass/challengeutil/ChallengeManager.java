package spirepass.challengeutil;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import spirepass.Spirepass;

import java.util.*;

import static basemod.BaseMod.logger;

// import static basemod.BaseMod.logger;

public class ChallengeManager {
    private static ChallengeManager instance = null;

    private List<Challenge> dailyChallenges;
    private List<Challenge> weeklyChallenges;
    public static final String LAST_DAILY_REFRESH = "lastDailyRefresh";
    public static final String LAST_WEEKLY_REFRESH = "lastWeeklyRefresh";

    // Track challenge completion status
    public HashMap<String, Boolean> completedChallenges;

    // Track when challenges were last refreshed
    private long lastDailyRefreshTime;
    private long lastWeeklyRefreshTime;

    // Constructor is private for singleton pattern
    private ChallengeManager() {
        dailyChallenges = new ArrayList<>();
        weeklyChallenges = new ArrayList<>();
        completedChallenges = new HashMap<>();
        lastDailyRefreshTime = 0;
        lastWeeklyRefreshTime = 0;
    }

    // Define a set of challenge IDs that should NOT play the completion sound
    private static final Set<String> META_CHALLENGE_IDS = new HashSet<>(Arrays.asList(
            "daily_tagalong",
            "weekly_freeloader",
            "weekly_dailymaster"
    ));

    // Singleton pattern getter
    public static ChallengeManager getInstance() {
        if (instance == null) {
            instance = new ChallengeManager();
        }
        return instance;
    }

    /**
     * Clear the completion status for a specific challenge ID
     * @param challengeId The ID of the challenge to clear completion status
     */
    public void clearCompletionStatus(String challengeId) {
        if (completedChallenges.containsKey(challengeId)) {
            completedChallenges.remove(challengeId);

            // Also remove from the config file
            try {
                if (Spirepass.config.has("completed_" + challengeId)) {
                    Spirepass.config.remove("completed_" + challengeId);
                }
            } catch (Exception e) {
//                 logger.error("Failed to remove completion status from config: " + e.getMessage());
            }
        }
    }

    /**
     * Clear all completion statuses (useful for dev testing)
     */
    public void clearAllCompletionStatus() {
        int beforeCount = completedChallenges.size();

        // Remove each completion status from the config file
        for (String challengeId : completedChallenges.keySet()) {
            try {
                Spirepass.config.remove("completed_" + challengeId);
            } catch (Exception e) {
//                 logger.error("Failed to remove completion status from config: " + e.getMessage());
            }
        }

        // Clear the map in memory
        completedChallenges.clear();

        // Save the config after removing keys
        try {
            Spirepass.config.save();
        } catch (Exception e) {
//             logger.error("Failed to save config after clearing completion statuses: " + e.getMessage());
        }

//         logger.info("clearAllCompletionStatus: Cleared " + beforeCount + " completion entries, map now has " + completedChallenges.size() + " entries");
    }


    public void onComplete(Challenge challenge) {
        if (completedChallenges.containsKey(challenge.getId()) && completedChallenges.get(challenge.getId())) {
            return;
        }
        completedChallenges.put(challenge.getId(), true);

        if (Spirepass.enableMainMenuElements &&
                Spirepass.playChallengeCompleteSound &&
                !META_CHALLENGE_IDS.contains(challenge.getId())) {
            CardCrawlGame.sound.play("UNLOCK_PING");
        }

        if (challenge.getType() == Challenge.ChallengeType.DAILY) {
            Spirepass.addXP(Spirepass.DAILY_CHALLENGE_XP);
        } else if (challenge.getType() == Challenge.ChallengeType.WEEKLY) {
            Spirepass.addXP(Spirepass.WEEKLY_CHALLENGE_XP);
        }

        // Check if this completion should trigger the Tagalong challenge
        if (!challenge.getId().equals("daily_tagalong") && challenge.getType() == Challenge.ChallengeType.DAILY) {
            // Count how many daily challenges are completed
            int completedDailies = 0;
            for (Challenge dailyChallenge : dailyChallenges) {
                if (isCompleted(dailyChallenge.getId()) && !dailyChallenge.getId().equals("daily_tagalong")) {
                    completedDailies++;
                }
            }
            // If 2 daily challenges are completed (excluding Tagalong itself), complete the Tagalong challenge
            if (completedDailies >= 2) {
                // Find and complete the Tagalong challenge if it exists and isn't already completed
                for (Challenge dailyChallenge : dailyChallenges) {
                    if (dailyChallenge.getId().equals("daily_tagalong") && !isCompleted("daily_tagalong")) {
//                         logger.info("Completing Tagalong challenge as other daily challenges are complete");
                        dailyChallenge.complete();
                        break;
                    }
                }
            }
        }

        // Check if this completion should trigger the Freeloader weekly challenge
        if (challenge.getType() == Challenge.ChallengeType.WEEKLY && !challenge.getId().equals("weekly_freeloader")) {
            // Count how many weekly challenges are completed (excluding Freeloader itself)
            int completedWeeklies = 0;
            for (Challenge weeklyChallenge : weeklyChallenges) {
                if (isCompleted(weeklyChallenge.getId()) &&
                        !weeklyChallenge.getId().equals("weekly_freeloader")) {
                    completedWeeklies++;
                }
            }

            // If at least 2 weekly challenges are completed, complete the Freeloader challenge
            if (completedWeeklies >= 2) {
                // Find and complete the Freeloader challenge if it exists and isn't already completed
                for (Challenge weeklyChallenge : weeklyChallenges) {
                    if (weeklyChallenge.getId().equals("weekly_freeloader") && !isCompleted("weekly_freeloader")) {
//                         logger.info("Completing Freeloader challenge as 2+ other weekly challenges are complete");
                        weeklyChallenge.complete();
                        break;
                    }
                }
            }
        }

        // Check if this completion should trigger the Daily Master weekly challenge
        if (challenge.getType() == Challenge.ChallengeType.DAILY) {
            // Count how many daily challenges are completed (including all daily challenges)
            int completedDailies = 0;
            for (Challenge dailyChallenge : dailyChallenges) {
                if (isCompleted(dailyChallenge.getId())) {
                    completedDailies++;
                }
            }

            // If all daily challenges are completed, complete the Daily Master challenge
            if (completedDailies >= dailyChallenges.size()) {
                // Find and complete the Daily Master challenge if it exists and isn't already completed
                for (Challenge weeklyChallenge : weeklyChallenges) {
                    if (weeklyChallenge.getId().equals("weekly_dailymaster") && !isCompleted("weekly_dailymaster")) {
//                         logger.info("Completing Daily Master challenge as all daily challenges are complete");
                        weeklyChallenge.complete();
                        break;
                    }
                }
            }
        }
    }

    // Check if a challenge is completed
    public boolean isCompleted(String challengeId) {
        return completedChallenges.getOrDefault(challengeId, false);
    }

    // Get all daily challenges
    public List<Challenge> getDailyChallenges() {
        return dailyChallenges;
    }

    // Get all weekly challenges
    public List<Challenge> getWeeklyChallenges() {
        return weeklyChallenges;
    }

    // Manually add a daily challenge
    public void addDailyChallenge(Challenge challenge) {
        dailyChallenges.add(challenge);
    }

    // Manually add a weekly challenge
    public void addWeeklyChallenge(Challenge challenge) {
        weeklyChallenges.add(challenge);
    }

    // Getters and setters for refresh timestamps
    public long getLastDailyRefreshTime() {
        return lastDailyRefreshTime;
    }

    public void setLastDailyRefreshTime(long time) {
        this.lastDailyRefreshTime = time;
    }

    public long getLastWeeklyRefreshTime() {
        return lastWeeklyRefreshTime;
    }

    public void setLastWeeklyRefreshTime(long time) {
        this.lastWeeklyRefreshTime = time;

    }
    // Save all challenge data to config
    public void saveData(SpireConfig config) {
        try {

            // Save the list of daily challenges
            saveChallengeList(config, dailyChallenges, "daily");

            // Save the list of weekly challenges
            saveChallengeList(config, weeklyChallenges, "weekly");

            // Save completion status for all challenges
            int completedCount = 0;
            for (String challengeId : completedChallenges.keySet()) {
                if (completedChallenges.get(challengeId)) {
                    completedCount++;
                }
                config.setBool("completed_" + challengeId, completedChallenges.get(challengeId));
            }

            config.setString(LAST_DAILY_REFRESH, String.valueOf(lastDailyRefreshTime));
            config.setString(LAST_WEEKLY_REFRESH, String.valueOf(lastWeeklyRefreshTime));
            try {
                config.save();
            } catch (Exception e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to save a list of challenges
    private void saveChallengeList(SpireConfig config, List<Challenge> challenges, String prefix) {
        // Save the number of challenges
        config.setInt(prefix + "_count", challenges.size());

        // Save each challenge's data
        for (int i = 0; i < challenges.size(); i++) {
            Challenge challenge = challenges.get(i);
            String baseKey = prefix + "_" + i + "_";

            config.setString(baseKey + "id", challenge.getId());
            config.setString(baseKey + "name", challenge.getName());
            config.setString(baseKey + "description", challenge.getDescription());
            config.setString(baseKey + "type", challenge.getType().toString());
            config.setInt(baseKey + "currentProgress", challenge.getCurrentProgress());
            config.setInt(baseKey + "maxProgress", challenge.getMaxProgress());
        }
    }

    // Load all challenge data from config
    public void loadData(SpireConfig config) {
        try {
            // Clear existing challenges
            dailyChallenges.clear();
            weeklyChallenges.clear();
            completedChallenges.clear();

            // We need to approach this differently since SpireConfig doesn't have getKeys()
            // First load the challenges and then check completions during the challenge loading
            // We'll check completions in the loadChallenge method

            // Now load challenges with completion status already populated
            // Rest of your existing loading code...

            // Check if daily_count key exists before trying to load
            if (config.has("daily_count")) {
                // Load daily challenges
                int dailyCount = config.getInt("daily_count");
                for (int i = 0; i < dailyCount; i++) {
                    Challenge challenge = loadChallenge(config, "daily_" + i + "_");
                    if (challenge != null) {
                        dailyChallenges.add(challenge);
                    }
                }
            }

            // Check if weekly_count key exists before trying to load
            if (config.has("weekly_count")) {
                // Load weekly challenges
                int weeklyCount = config.getInt("weekly_count");
                for (int i = 0; i < weeklyCount; i++) {
                    Challenge challenge = loadChallenge(config, "weekly_" + i + "_");
                    if (challenge != null) {
                        weeklyChallenges.add(challenge);
                    }
                }
            }

            // Load refresh timestamps
            if (config.has(LAST_DAILY_REFRESH)) {
                String dailyRefresh = config.getString(LAST_DAILY_REFRESH);
                if (dailyRefresh != null && !dailyRefresh.isEmpty()) {
                    lastDailyRefreshTime = Long.parseLong(dailyRefresh);
                }
            }

            if (config.has(LAST_WEEKLY_REFRESH)) {
                String weeklyRefresh = config.getString(LAST_WEEKLY_REFRESH);
                if (weeklyRefresh != null && !weeklyRefresh.isEmpty()) {
                    lastWeeklyRefreshTime = Long.parseLong(weeklyRefresh);
                }
            }

//             logger.info("Loaded " + dailyChallenges.size() + " daily challenges and " +
//                    weeklyChallenges.size() + " weekly challenges");
        //     logger.info("Last daily refresh: " + new java.util.Date(lastDailyRefreshTime));
//             logger.info("Last weekly refresh: " + new java.util.Date(lastWeeklyRefreshTime));
        } catch (Exception e) {
//             logger.error("Failed to load challenge data: " + e.getMessage());
            e.printStackTrace();
        }
//         logger.info("loadData: Loaded " + completedChallenges.size() + " challenge completion statuses");
    }

    // Helper method to load a single challenge
    private Challenge loadChallenge(SpireConfig config, String baseKey) {
        try {
            if (!config.has(baseKey + "id")) {
                return null;
            }

            String id = config.getString(baseKey + "id");
            String name = config.getString(baseKey + "name");
            String description = config.getString(baseKey + "description");
            Challenge.ChallengeType type = Challenge.ChallengeType.valueOf(
                    config.getString(baseKey + "type"));
            int currentProgress = config.getInt(baseKey + "currentProgress");
            int maxProgress = config.getInt(baseKey + "maxProgress");

            // First check if this challenge is already tracked as completed
            boolean isAlreadyCompleted = false;
            String completionKey = "completed_" + id;
            if (config.has(completionKey)) {
                isAlreadyCompleted = config.getBool(completionKey);
                completedChallenges.put(id, isAlreadyCompleted);
//                 logger.info("Challenge " + id + " loaded with completion status: " + isAlreadyCompleted);
            }

            // Create the challenge with all properties
            Challenge challenge = new Challenge(id, name, description, type, maxProgress);

            // Safely set progress without triggering onComplete if already completed
            if (isAlreadyCompleted) {
                challenge.setProgressSilently(currentProgress);
            } else {
                challenge.setProgress(currentProgress);
            }

            return challenge;
        } catch (Exception e) {
//             logger.error("Failed to load challenge: " + e.getMessage());
            return null;
        }
    }

}