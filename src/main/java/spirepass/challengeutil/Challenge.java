package spirepass.challengeutil;

public class Challenge {
    // Basic challenge properties
    private String id;
    private String name;
    private String description;
    private ChallengeType type;

    // Progress tracking
    private int currentProgress;
    private int maxProgress;

    // Challenge type enum to distinguish daily vs weekly
    public enum ChallengeType {
        DAILY,
        WEEKLY
    }

    /**
     * Constructor for simple challenges
     */
    public Challenge(String id, String name, String description, ChallengeType type) {
        this(id, name, description, type, 1);
    }

    /**
     * Constructor for challenges that require multiple steps
     */
    public Challenge(String id, String name, String description, ChallengeType type, int maxProgress) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.maxProgress = maxProgress;
        this.currentProgress = 0;
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ChallengeType getType() {
        return type;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    // Progress methods

    /**
     * Increment progress by 1
     */
    public void incrementProgress() {
        incrementProgress(1);

        if (isCompleted()) {
            ChallengeManager.getInstance().onComplete(this);
        }

    }

    /**
     * Increment progress by specified amount
     */
    public void incrementProgress(int amount) {
        currentProgress += amount;
        if (currentProgress > maxProgress) {
            currentProgress = maxProgress;
        }

        // Auto-check completion
        if (isCompleted()) {
            ChallengeManager.getInstance().onComplete(this);
        }
    }

    /**
     * Set progress to a specific value
     */
    public void setProgress(int progress) {
        this.currentProgress = progress;
        if (currentProgress > maxProgress) {
            currentProgress = maxProgress;
        }

        // Auto-check completion - but only if not already tracked as completed
        if (isCompleted() && !ChallengeManager.getInstance().isCompleted(this.getId())) {
            ChallengeManager.getInstance().onComplete(this);
        }
    }

    /**
     * Set progress without triggering completion check
     * Used when loading already-completed challenges
     */
    public void setProgressSilently(int progress) {
        this.currentProgress = progress;
        if (currentProgress > maxProgress) {
            currentProgress = maxProgress;
        }
        // No completion check here
    }

    /**
     * Reset progress to 0
     */
    public void resetProgress() {
        this.currentProgress = 0;
    }

    /**
     * Check if challenge is completed
     */
    public boolean isCompleted() {
        return currentProgress >= maxProgress;
    }

    /**
     * Get completion percentage (0-100)
     */
    public int getCompletionPercentage() {
        return (int)((float)currentProgress / maxProgress * 100);
    }

    /**
     * Complete this challenge immediately
     */
    public void complete() {
        setProgress(maxProgress);
    }
}