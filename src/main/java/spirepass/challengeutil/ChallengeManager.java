package spirepass.challengeutil;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static basemod.BaseMod.logger;

public class ChallengeManager {
    private static ChallengeManager instance = null;

    private List<Challenge> dailyChallenges;
    private List<Challenge> weeklyChallenges;

    // Track challenge completion status
    private HashMap<String, Boolean> completedChallenges;

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
        }
    }

    /**
     * Clear all completion statuses (useful for dev testing)
     */
    public void clearAllCompletionStatus() {
        completedChallenges.clear();
    }

    // Method called when a challenge is completed
    public void onComplete(Challenge challenge) {
        // Mark the challenge as completed
        completedChallenges.put(challenge.getId(), true);

        // TODO: Implementation for rewards
        // For now, just log completion
        System.out.println("Challenge completed: " + challenge.getName());
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
            logger.info("Saving challenge data to config...");
            logger.info("Daily challenges to save: " + dailyChallenges.size());
            logger.info("Weekly challenges to save: " + weeklyChallenges.size());

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

            logger.info("Saved " + dailyChallenges.size() + " daily challenges and " +
                    weeklyChallenges.size() + " weekly challenges");
            logger.info("Saved " + completedCount + " completed challenges");

            // Force saving the config file immediately
            try {
                config.save();
                logger.info("Config file saved successfully");
            } catch (Exception e) {
                logger.error("Error saving config file: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Failed to save challenge data: " + e.getMessage());
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

            // Load challenge completion statuses
            for (Challenge challenge : dailyChallenges) {
                String challengeId = challenge.getId();
                if (config.has("completed_" + challengeId)) {
                    boolean completed = config.getBool("completed_" + challengeId);
                    completedChallenges.put(challengeId, completed);
                }
            }

            for (Challenge challenge : weeklyChallenges) {
                String challengeId = challenge.getId();
                if (config.has("completed_" + challengeId)) {
                    boolean completed = config.getBool("completed_" + challengeId);
                    completedChallenges.put(challengeId, completed);
                }
            }

            logger.info("Loaded " + dailyChallenges.size() + " daily challenges and " +
                    weeklyChallenges.size() + " weekly challenges");
        } catch (Exception e) {
            logger.error("Failed to load challenge data: " + e.getMessage());
            e.printStackTrace();
        }
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

            // Create the challenge with all properties
            Challenge challenge = new Challenge(id, name, description, type, maxProgress);
            challenge.setProgress(currentProgress);

            return challenge;
        } catch (Exception e) {
            logger.error("Failed to load challenge: " + e.getMessage());
            return null;
        }
    }
}