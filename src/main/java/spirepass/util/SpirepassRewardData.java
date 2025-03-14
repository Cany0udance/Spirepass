package spirepass.util;

public class SpirepassRewardData {
    public enum RewardRarity {
        NONE,
        COMMON,
        UNCOMMON,
        RARE
    }

    public enum RewardType {
        IMAGE,
        CHARACTER_MODEL,
    }

    private int level;
    private String name;
    private String description;
    private RewardRarity rarity;
    private RewardType type;
    private String imagePath; // For IMAGE type
    private String modelId;   // For CHARACTER_MODEL type
    private String entityId;  // Added field to identify which entity this skin applies to

    // Updated constructor for CHARACTER_MODEL type with entity ID
    public SpirepassRewardData(int level, String name, String description,
                               RewardRarity rarity, RewardType type,
                               String entityId, String modelId) {
        this.level = level;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.type = type;

        if (type == RewardType.CHARACTER_MODEL) {
            this.modelId = modelId;
            this.entityId = entityId;
            this.imagePath = null;
        } else {
            throw new IllegalArgumentException("This constructor should only be used for CHARACTER_MODEL type");
        }
    }

    // Constructor for IMAGE type (unchanged functionality but adjusted parameter name)
    public SpirepassRewardData(int level, String name, String description,
                               RewardRarity rarity, RewardType type, String imagePath) {
        this.level = level;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.type = type;

        if (type == RewardType.IMAGE) {
            this.imagePath = imagePath;
            this.modelId = null;
            this.entityId = null;
        } else {
            throw new IllegalArgumentException("This constructor should only be used for IMAGE type");
        }
    }

    // Getters (add new getter for entityId)
    public int getLevel() { return level; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public RewardRarity getRarity() { return rarity; }
    public RewardType getType() { return type; }
    public String getImagePath() { return imagePath; }
    public String getModelId() { return modelId; }
    public String getEntityId() { return entityId; }

    // Helper method to get the appropriate background texture based on rarity (unchanged)
    public String getBackgroundTexturePath() {
        switch (rarity) {
            case NONE:
                return null;
            case COMMON:
                return "spirepass/images/screen/CommonRewardBackground.png";
            case UNCOMMON:
                return "spirepass/images/screen/UncommonRewardBackground.png";
            case RARE:
                return "spirepass/images/screen/RareRewardBackground.png";
            default:
                return null;
        }
    }
}