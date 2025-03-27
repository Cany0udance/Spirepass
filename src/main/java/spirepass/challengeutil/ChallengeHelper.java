package spirepass.challengeutil;

import spirepass.Spirepass;

public class ChallengeHelper {
    /**
     * Check if a specific challenge is active and incomplete
     * @param challengeId The ID of the challenge to check
     * @return true if the challenge is active and not yet completed
     */
    public static boolean isActiveChallengeIncomplete(String challengeId) {
        ChallengeManager manager = ChallengeManager.getInstance();
        // Check if the challenge is completed first (fastest check)
        if (manager.isCompleted(challengeId)) {
            return false;
        }
        // Check daily challenges
        for (Challenge challenge : manager.getDailyChallenges()) {
            if (challenge.getId().equals(challengeId) && !challenge.isCompleted()) {
                return true;
            }
        }
        // Check weekly challenges
        for (Challenge challenge : manager.getWeeklyChallenges()) {
            if (challenge.getId().equals(challengeId) && !challenge.isCompleted()) {
                return true;
            }
        }
        // Challenge not found or not active
        return false;
    }

    /**
     * Update progress for a specific challenge if it's active and incomplete
     * @param challengeId The ID of the challenge to update
     * @param progressAmount Amount of progress to add
     * @return true if the challenge was found and updated
     */
    public static boolean updateChallengeProgress(String challengeId, int progressAmount) {
        ChallengeManager manager = ChallengeManager.getInstance();
        // Check if the challenge is already completed
        if (manager.isCompleted(challengeId)) {
            return false;
        }

        boolean updated = false;

        // Check and update daily challenges
        for (Challenge challenge : manager.getDailyChallenges()) {
            if (challenge.getId().equals(challengeId) && !challenge.isCompleted()) {
                challenge.incrementProgress(progressAmount);
                updated = true;
                break;
            }
        }

        // Check and update weekly challenges if not updated yet
        if (!updated) {
            for (Challenge challenge : manager.getWeeklyChallenges()) {
                if (challenge.getId().equals(challengeId) && !challenge.isCompleted()) {
                    challenge.incrementProgress(progressAmount);
                    updated = true;
                    break;
                }
            }
        }

        // Save data if challenge was updated
        if (updated) {
            manager.saveData(Spirepass.config);
            try {
                Spirepass.config.save();
//                 Spirepass.logger.info("Saved config after updating challenge progress: " + challengeId);
            } catch (Exception e) {
//                 Spirepass.logger.error("Error saving config after updating challenge progress: " + e.getMessage());
            }
        }

        return updated;
    }

    /**
     * Mark a specific challenge as complete if it's active and incomplete
     * @param challengeId The ID of the challenge to complete
     * @return true if the challenge was found and completed
     */
    public static boolean completeChallenge(String challengeId) {
        ChallengeManager manager = ChallengeManager.getInstance();
        // Check if the challenge is already completed
        if (manager.isCompleted(challengeId)) {
            return false;
        }

        boolean completed = false;

        // Check and complete daily challenges
        for (Challenge challenge : manager.getDailyChallenges()) {
            if (challenge.getId().equals(challengeId) && !challenge.isCompleted()) {
                challenge.complete();
                completed = true;
                break;
            }
        }

        // Check and complete weekly challenges if not completed yet
        if (!completed) {
            for (Challenge challenge : manager.getWeeklyChallenges()) {
                if (challenge.getId().equals(challengeId) && !challenge.isCompleted()) {
                    challenge.complete();
                    completed = true;
                    break;
                }
            }
        }

        // Save data if challenge was completed
        if (completed) {
            manager.saveData(Spirepass.config);
            try {
                Spirepass.config.save();
//                 Spirepass.logger.info("Saved config after completing challenge: " + challengeId);
            } catch (Exception e) {
//                 Spirepass.logger.error("Error saving config after completing challenge: " + e.getMessage());
            }
        }

        return completed;
    }

    /**
     * Get an active challenge by ID
     * @param challengeId The ID of the challenge to find
     * @return The Challenge object if found and active, or null
     */
    public static Challenge getActiveChallenge(String challengeId) {
        ChallengeManager manager = ChallengeManager.getInstance();
        // Check daily challenges
        for (Challenge challenge : manager.getDailyChallenges()) {
            if (challenge.getId().equals(challengeId)) {
                return challenge;
            }
        }
        // Check weekly challenges
        for (Challenge challenge : manager.getWeeklyChallenges()) {
            if (challenge.getId().equals(challengeId)) {
                return challenge;
            }
        }
        // Challenge not found or not active
        return null;
    }
}