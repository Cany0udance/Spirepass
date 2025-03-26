package spirepass.spirepassutil;

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
        CARDBACK,
        TEXT
    }

    private int level;
    private String name;
    private String description;
    private RewardRarity rarity;
    private RewardType type;
    private String imagePath; // For IMAGE type
    private String modelId;   // For CHARACTER_MODEL type
    private String entityId;  // Added field to identify which entity this skin applies to
    private String cardbackType; // For CARDBACK type (colorless or curse)
    private String cardbackId;   // For CARDBACK type (identifier for specific cardback)

    // Updated constructor for CHARACTER_MODEL type with entity ID (unchanged)
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
            this.cardbackType = null;
            this.cardbackId = null;
        } else {
            throw new IllegalArgumentException("This constructor should only be used for CHARACTER_MODEL type");
        }
    }

    // New constructor for TEXT type
    public SpirepassRewardData(int level, String name, String description,
                               RewardRarity rarity, RewardType type) {
        this.level = level;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.type = type;
        if (type == RewardType.TEXT) {
            // For text-only rewards, we don't need paths or IDs
            this.imagePath = null;
            this.modelId = null;
            this.entityId = null;
            this.cardbackType = null;
            this.cardbackId = null;
        } else {
            throw new IllegalArgumentException("This constructor should only be used for TEXT type");
        }
    }

    // Constructor for IMAGE type (unchanged)
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
            this.cardbackType = null;
            this.cardbackId = null;
        } else {
            throw new IllegalArgumentException("This constructor should only be used for IMAGE type");
        }
    }

    // New constructor for CARDBACK type
    public SpirepassRewardData(int level, String name, String description,
                               RewardRarity rarity, RewardType type,
                               String cardbackType, String cardbackId, String imagePath) {
        this.level = level;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.type = type;
        if (type == RewardType.CARDBACK) {
            this.cardbackType = cardbackType;
            this.cardbackId = cardbackId;
            this.imagePath = imagePath; // For preview image
            this.modelId = null;
            this.entityId = null;
        } else {
            throw new IllegalArgumentException("This constructor should only be used for CARDBACK type");
        }
    }

    // Getters (add new getters for cardback fields)
    public int getLevel() { return level; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public RewardRarity getRarity() { return rarity; }
    public RewardType getType() { return type; }
    public String getImagePath() { return imagePath; }
    public String getModelId() { return modelId; }
    public String getEntityId() { return entityId; }
    public String getCardbackType() { return cardbackType; }
    public String getCardbackId() { return cardbackId; }

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