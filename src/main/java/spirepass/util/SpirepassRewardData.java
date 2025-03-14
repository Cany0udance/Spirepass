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
        CREATURE_MODEL
    }

    private int level;
    private String name;
    private String description;
    private RewardRarity rarity;
    private RewardType type;
    private String imagePath; // For IMAGE type
    private String modelId;   // For CHARACTER_MODEL or CREATURE_MODEL type

    public SpirepassRewardData(int level, String name, String description,
                               RewardRarity rarity, RewardType type, String resourcePath) {
        this.level = level;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.type = type;

        if (type == RewardType.IMAGE) {
            this.imagePath = resourcePath;
            this.modelId = null;
        } else {
            this.imagePath = null;
            this.modelId = resourcePath;
        }
    }

    // Getters
    public int getLevel() { return level; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public RewardRarity getRarity() { return rarity; }
    public RewardType getType() { return type; }
    public String getImagePath() { return imagePath; }
    public String getModelId() { return modelId; }

    // Helper method to get the appropriate background texture based on rarity
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