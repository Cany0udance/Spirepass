package spirepass.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import spirepass.Spirepass;
import spirepass.spirepassutil.SkinManager;
import spirepass.spirepassutil.SpirepassPositionSettings;
import spirepass.spirepassutil.SpirepassRewardData;

import java.util.HashMap;

public class SpirepassRewardManager {
    // Maps to hold our reward data and textures
    private HashMap<Integer, SpirepassRewardData> rewardData;
    private HashMap<Integer, Texture> rewardTextures;
    private HashMap<String, Texture> backgroundTextures;

    // Animation manager for rendering character models
    private SpirepassAnimationManager animationManager;

    public SpirepassRewardManager() {
        this.rewardData = new HashMap<>();
        this.rewardTextures = new HashMap<>();
        this.backgroundTextures = new HashMap<>();

        // Initialize the animation manager
        this.animationManager = new SpirepassAnimationManager();

        // Load background textures
        loadBackgroundTextures();

        // Initialize reward data and textures
        initializeRewardData();
    }

    private void loadBackgroundTextures() {
        // Load the background textures for different rarities
        String[] paths = {
                "spirepass/images/screen/CommonRewardBackground.png",
                "spirepass/images/screen/UncommonRewardBackground.png",
                "spirepass/images/screen/RareRewardBackground.png",
                "spirepass/images/screen/LockedRewardBackground.png"
        };

        for (String path : paths) {
            try {
                backgroundTextures.put(path, ImageMaster.loadImage(path));
            } catch (Exception e) {
                System.err.println("Failed to load background texture: " + path);
            }
        }
    }

    private void initializeRewardData() {

        // Level 0: Welcome message with common border
        rewardData.put(0, new SpirepassRewardData(
                0,
                "", // Empty title
                "", // Empty description
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.TEXT
        ));

        // Level 1: Defect skin
        rewardData.put(1, new SpirepassRewardData(
                1,
                "avwejkanaklwjv e",
                "yeah",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_DEFECT,
                "DEFECT_GARBLE"
        ));

        // Level 2: Weaponized 115 Ironclad skin
        rewardData.put(2, new SpirepassRewardData(
                2,
                "Weaponized 115 from the hit first person shooter game Call of Duty®: Black Ops II, part of the Call of Duty® series",
                "The Weaponized 115 Personalization Pack brings out the power of Zombies to any Multiplayer match. Pack your weapons with an all-new Call of Duty: Black Ops II Origins-inspired camo, set of reticles, and undead animated calling card.",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_IRONCLAD,
                "IRONCLAD_WEAPONIZED115"
        ));

        // Level 3: Invisible Man Ironclad skin
        rewardData.put(3, new SpirepassRewardData(
                3,
                "Invisible Man",
                "You can't see him, but the enemies still can",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_IRONCLAD,
                "IRONCLAD_INVISIBLEMAN"
        ));

        // Level 4: Sponsored colorless cardback
        rewardData.put(4, new SpirepassRewardData(
                4,
                "Sponsored",
                "Legal note: this cardback does not represent any official affiliation with a brand.",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_COLORLESS,
                "COLORLESS_SPONSORED",
                "spirepass/images/rewards/cardbacks/colorless/sponsored/RAIDSkillLarge.png"
        ));

        // Add Watcher skin
        rewardData.put(5, new SpirepassRewardData(
                5,
                "kris deltarune",
                "\"W...Watcher? Are you OK? You're yelling...\"",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_WATCHER,
                "WATCHER_DREEMURR"
        ));

        // Add Jaw Worm skin
        rewardData.put(6, new SpirepassRewardData(
                6,
                "Bloodied Jaw Worm",
                "A Jaw Worm that's taken a few hits",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_JAW_WORM,
                "JAW_WORM_BLOODIED"
        ));

        // Add Harold Curse cardback at level 7
        rewardData.put(7, new SpirepassRewardData(
                7,
                "Harold Curse Cardback",
                ":)",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_CURSE,
                "CURSE_HAROLD",
                "spirepass/images/rewards/cardbacks/curse/HaroldLarge.png"
        ));

        rewardData.put(8, new SpirepassRewardData(
                8,
                "Big Bird",
                "he big",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_AWAKENED_ONE,
                "AWAKENED_ONE_BIGBIRD"
        ));

        rewardData.put(9, new SpirepassRewardData(
                9,
                "Blue",
                "he's blue, da ba dee da ba di",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_RED_SLAVER,
                "RED_SLAVER_BLUE"
        ));

        rewardData.put(10, new SpirepassRewardData(
                10,
                "Red",
                "We have red slaver at home",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_BLUE_SLAVER,
                "BLUE_SLAVER_RED"
        ));

        rewardData.put(11, new SpirepassRewardData(
                11,
                "Disarmed",
                "It's like that one beta art",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_CULTIST,
                "CULTIST_DISARMED"
        ));

        rewardData.put(12, new SpirepassRewardData(
                12,
                "Law Abiding Citizen",
                "Deal 3 damage. Increase the damage of ALL Claw cards by 2 this combat.",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_DEFECT,
                "DEFECT_LAWABIDINGCITIZEN"
        ));

        rewardData.put(13, new SpirepassRewardData(
                13,
                "Bear",
                "Somehow, the rest of the Red Mask Gang still haven't noticed.",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_BEAR,
                "BEAR_ACTUALBEAR"
        ));

        rewardData.put(14, new SpirepassRewardData(
                14,
                "Spaghetti & Meatballs",
                "Comes with a side of Parasites.",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_WRITHING_MASS,
                "WRITHING_MASS_SPAGHETTI"
        ));

        rewardData.put(15, new SpirepassRewardData(
                15,
                "Giantama",
                "\"No\"",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_GIANT_HEAD,
                "GIANT_HEAD_GIANTAMA"
        ));

        rewardData.put(16, new SpirepassRewardData(
                16,
                "Blurry",
                "where are my glasses??",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SILENT,
                "SILENT_BLURRY"
        ));

        rewardData.put(17, new SpirepassRewardData(
                17,
                "Space Blanket",
                "Are you telling me that my RNG just happens to be so unlucky? No! He orchestrated it! Casey!!!",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SILENT,
                "SILENT_SPACEBLANKET"
        ));

        rewardData.put(18, new SpirepassRewardData(
                18,
                "Pajama Sam",
                "The Darkness orbs are really starting to get to me.",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_DEFECT,
                "DEFECT_PAJAMASAM"
        ));

        rewardData.put(19, new SpirepassRewardData(
                19,
                "Slendernob",
                "Pages 3/8",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_GREMLIN_NOB,
                "GREMLIN_NOB_URBANLEGEND"
        ));

        rewardData.put(20, new SpirepassRewardData(
                20,
                "Favorite Customer",
                "Do you like this cardback? It's not for sale",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_COLORLESS,
                "COLORLESS_FAVORITECUSTOMER",
                "spirepass/images/rewards/cardbacks/colorless/favoritecustomer/FavoriteCustomerSkillLarge.png"
        ));

        rewardData.put(21, new SpirepassRewardData(
                21,
                "Prismatic",
                "sentries if they were good",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SENTRY,
                "SENTRY_PRISMATIC"
        ));

        rewardData.put(22, new SpirepassRewardData(
                22,
                "Lovestruck",
                "yes, my queen <3",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_CENTURION,
                "CENTURION_LOVESTRUCK"
        ));

        rewardData.put(23, new SpirepassRewardData(
                23,
                "Colgate",
                "The most fresh creature in the Spire.",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SNECKO,
                "SNECKO_COLGATE"
        ));

        rewardData.put(24, new SpirepassRewardData(
                24,
                "Donut",
                "If Grand Constructs weren't meant to be eaten, then why do they look so delicious???",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_DONU,
                "DONU_DONUT"
        ));

        rewardData.put(25, new SpirepassRewardData(
                25,
                "Chalkboard",
                "\"Uhh, our chalkboard just sprouted legs and walked away. Class is cancelled!\"",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_DECA,
                "DECA_CHALKBOARD"
        ));

        rewardData.put(26, new SpirepassRewardData(
                26,
                "Purple Guy",
                "The bandit behind the slaughter",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_ROMEO,
                "ROMEO_PURPLEGUY"
        ));

        rewardData.put(27, new SpirepassRewardData(
                27,
                "Blissful",
                "Though this cardback represents XP, it does not affect your XP. xP",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_COLORLESS,
                "COLORLESS_BLISSFUL",
                "spirepass/images/rewards/cardbacks/colorless/blissful/BlissSkillLarge.png"
        ));

        rewardData.put(28, new SpirepassRewardData(
                28,
                "Downtrend",
                "not stonks",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_CURSE,
                "CURSE_NOTSTONKS",
                "spirepass/images/rewards/cardbacks/curse/NotStonksLarge.png"
        ));

        rewardData.put(29, new SpirepassRewardData(
                29,
                "Jimbo Joker",
                "\"Maybe Go Fish is more our speed...\"",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_COLORLESS,
                "COLORLESS_JIMBO",
                "spirepass/images/rewards/cardbacks/colorless/jimbo/JimboSkillLarge.png"
        ));

        // Level 30: Final reward with rare border
        rewardData.put(30, new SpirepassRewardData(
                30,
                "", // Empty title
                "", // Empty description
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.TEXT
        ));

        // Default reward for all other levels (badge image)
        Texture badgeTexture = ImageMaster.loadImage("spirepass/images/badge.png");

        // Load the image textures for any image-type or cardback-type rewards
        for (Integer level : rewardData.keySet()) {
            SpirepassRewardData data = rewardData.get(level);
            if (data.getImagePath() != null) {
                try {
                    rewardTextures.put(level, ImageMaster.loadImage(data.getImagePath()));
                } catch (Exception e) {
                    System.err.println("Failed to load reward texture for level " + level);
                }
            }
        }

        // For any unspecified levels, use the badge texture as a fallback
        for (int i = 0; i <= 30; i++) {
            if (!rewardTextures.containsKey(i) && !rewardData.containsKey(i)) {
                rewardTextures.put(i, badgeTexture);
            }
        }
    }

    public void renderReward(SpriteBatch sb, int level, boolean isUnlocked) {
        // If the reward is locked, show the locked view
        if (!isUnlocked) {
            renderLockedReward(sb);
            return;
        }

        // Original code for unlocked rewards
        SpirepassRewardData reward = getRewardData(level);

        // If we have specific reward data, use it
        if (reward != null) {
            renderRewardWithData(sb, level, isUnlocked, reward);
        } else {
            renderDefaultReward(sb, level);
        }
    }

    private void renderTextReward(SpriteBatch sb, SpirepassRewardData reward) {
        // Get the appropriate background texture based on the reward's rarity
        String backgroundPath = reward.getBackgroundTexturePath();
        Texture backgroundTexture = backgroundPath != null ? backgroundTextures.get(backgroundPath) : null;

        // Render the background
        if (backgroundTexture != null) {
            float previewHeight = SpirepassPositionSettings.REWARD_PREVIEW_HEIGHT * SpirepassPositionSettings.REWARD_BACKGROUND_SCALE;
            float previewWidth = previewHeight * (backgroundTexture.getWidth() / (float) backgroundTexture.getHeight());

            sb.setColor(Color.WHITE);
            sb.draw(
                    backgroundTexture,
                    Settings.WIDTH / 2.0f - previewWidth / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y - previewHeight / 2.0f,
                    previewWidth,
                    previewHeight
            );
        }

        // Different message text and style based on level
        if (reward.getLevel() == 0) {
            // Level 0 message - single line
            String messageText = "Welcome to Spirepass! Go complete some challenges and level up!";
            FontHelper.renderFontCentered(
                    sb,
                    FontHelper.tipBodyFont,
                    messageText,
                    Settings.WIDTH / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y,
                    Color.WHITE
            );
        } else if (reward.getLevel() == 30) {
            // Level 30 message - split into two lines
            String line1 = "MAX LEVEL REACHED!";
            String line2 = "You've unlocked...A SENSE OF PRIDE AND ACCOMPLISHMENT!!!";

            // Render first line
            FontHelper.renderFontCentered(
                    sb,
                    FontHelper.tipBodyFont,
                    line1,
                    Settings.WIDTH / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y + 15.0f * Settings.scale,
                    Color.GOLD
            );

            // Render second line
            FontHelper.renderFontCentered(
                    sb,
                    FontHelper.tipBodyFont,
                    line2,
                    Settings.WIDTH / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y - 15.0f * Settings.scale,
                    Color.GOLD
            );
        } else {
            // Fallback for any other levels
            String messageText = "Level " + reward.getLevel() + " Reward";
            FontHelper.renderFontCentered(
                    sb,
                    FontHelper.tipBodyFont,
                    messageText,
                    Settings.WIDTH / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y,
                    Color.WHITE
            );
        }
    }

    private void renderLockedReward(SpriteBatch sb) {
        // Get the locked background texture
        Texture lockedBackgroundTexture = backgroundTextures.get("spirepass/images/screen/LockedRewardBackground.png");

        // Render the locked background
        if (lockedBackgroundTexture != null) {
            float previewHeight = SpirepassPositionSettings.REWARD_PREVIEW_HEIGHT * SpirepassPositionSettings.REWARD_BACKGROUND_SCALE;
            float previewWidth = previewHeight * (lockedBackgroundTexture.getWidth() / (float) lockedBackgroundTexture.getHeight());

            sb.setColor(Color.WHITE);
            sb.draw(
                    lockedBackgroundTexture,
                    Settings.WIDTH / 2.0f - previewWidth / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y - previewHeight / 2.0f,
                    previewWidth,
                    previewHeight
            );
        }

        // Render the lock image
        Texture lockTexture = ImageMaster.loadImage("spirepass/images/screen/lock.png");
        if (lockTexture != null) {
            // Adjustable size for the lock image - you can modify these values as needed
            float lockSize = 120.0f * Settings.scale; // Size of the lock image

            sb.setColor(Color.WHITE);
            sb.draw(
                    lockTexture,
                    Settings.WIDTH / 2.0f - lockSize / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y - lockSize / 4.0f, // Positioned slightly above center
                    lockSize,
                    lockSize
            );
        }

        // Render the locked message (moved down to make room for the lock)
        FontHelper.renderFontCentered(
                sb,
                FontHelper.tipBodyFont,
                "Reward locked. Go complete some challenges!",
                Settings.WIDTH / 2.0f,
                SpirepassPositionSettings.REWARD_PREVIEW_Y - 80.0f * Settings.scale, // Moved down to be below the lock
                Color.WHITE
        );
    }

    private void renderRewardWithData(SpriteBatch sb, int level, boolean isUnlocked, SpirepassRewardData reward) {
        // Get the background texture path
        String backgroundPath = reward.getBackgroundTexturePath();
        Texture backgroundTexture = backgroundPath != null ? backgroundTextures.get(backgroundPath) : null;

        if (reward.getType() == SpirepassRewardData.RewardType.TEXT) {
            renderTextReward(sb, reward);
            return;
        }

        // Render the background if available
        if (backgroundTexture != null) {
            float previewHeight = SpirepassPositionSettings.REWARD_PREVIEW_HEIGHT * SpirepassPositionSettings.REWARD_BACKGROUND_SCALE;
            float previewWidth = previewHeight * (backgroundTexture.getWidth() / (float) backgroundTexture.getHeight());

            sb.setColor(Color.WHITE);
            sb.draw(
                    backgroundTexture,
                    Settings.WIDTH / 2.0f - previewWidth / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y - previewHeight / 2.0f,
                    previewWidth,
                    previewHeight
            );
        }

        // Render reward preview based on type
        if (reward.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL) {
            String modelId = reward.getModelId();
            String entityId = reward.getEntityId();

            // Get the variant from the model ID
            String variant = SpirepassAnimationManager.getVariantFromModelId(entityId, modelId);

            // Render the appropriate entity preview
            animationManager.renderAnimationPreview(sb, entityId, variant);
        } else if (reward.getType() == SpirepassRewardData.RewardType.IMAGE ||
                reward.getType() == SpirepassRewardData.RewardType.CARDBACK) {
            // For both IMAGE and CARDBACK types, display the image preview
            Texture rewardTexture = getRewardTexture(level);

            if (rewardTexture != null) {
                float previewHeight = SpirepassPositionSettings.REWARD_IMAGE_HEIGHT * SpirepassPositionSettings.REWARD_CONTENT_SCALE;
                float aspectRatio = rewardTexture.getWidth() / (float) rewardTexture.getHeight();
                float previewWidth = previewHeight * aspectRatio;

                sb.setColor(Color.WHITE);
                sb.draw(
                        rewardTexture,
                        Settings.WIDTH / 2.0f - previewWidth / 2.0f,
                        SpirepassPositionSettings.REWARD_PREVIEW_Y - previewHeight / 2.0f,
                        previewWidth,
                        previewHeight
                );
            }
        }

        // Get the color based on rarity
        Color titleColor;
        switch (reward.getRarity()) {
            case UNCOMMON:
                titleColor = new Color(0.5058f, 0.8862f, 0.9098f, 1.0f); // #81e2e8
                break;
            case RARE:
                titleColor = new Color(0.9804f, 0.7333f, 0.2627f, 1.0f); // #fabb43
                break;
            case COMMON:
            default:
                titleColor = Color.WHITE;
                break;
        }

        // Render reward title with the appropriate color
        FontHelper.renderFontCentered(
                sb,
                FontHelper.tipBodyFont,
                reward.getName(),
                Settings.WIDTH / 2.0f,
                SpirepassPositionSettings.REWARD_NAME_Y,
                titleColor
        );

        // Render description
        FontHelper.renderFontCentered(
                sb,
                FontHelper.tipBodyFont,
                reward.getDescription(),
                Settings.WIDTH / 2.0f,
                SpirepassPositionSettings.REWARD_DESCRIPTION_Y,
                Color.LIGHT_GRAY
        );
    }

    private void renderDefaultReward(SpriteBatch sb, int level) {
        // No specific reward data, just show level number and a generic message
        Texture rewardTexture = getRewardTexture(level);
        if (rewardTexture != null) {
            // Set desired height for all reward images
            float previewHeight = 200.0f * Settings.scale;
            // Calculate width based on the texture's aspect ratio
            float aspectRatio = rewardTexture.getWidth() / (float) rewardTexture.getHeight();
            float previewWidth = previewHeight * aspectRatio;
            sb.setColor(Color.WHITE);
            sb.draw(
                    rewardTexture,
                    Settings.WIDTH / 2.0f - previewWidth / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y - previewHeight / 2.0f,
                    previewWidth,
                    previewHeight
            );
        } else {
            // Only show text if no texture is available
            renderFallbackText(sb, "No preview available for Level " + level, Color.ORANGE);
        }

        // Render generic title
        FontHelper.renderFontCentered(
                sb,
                FontHelper.tipBodyFont,
                "Level " + level + " Reward",
                Settings.WIDTH / 2.0f,
                SpirepassPositionSettings.REWARD_NAME_Y,
                Color.WHITE
        );
    }

    private void renderFallbackText(SpriteBatch sb, String text, Color color) {
        FontHelper.renderFontCentered(
                sb,
                FontHelper.tipBodyFont,
                text,
                Settings.WIDTH / 2.0f,
                SpirepassPositionSettings.REWARD_PREVIEW_Y,
                color
        );
    }

    public boolean isRewardEquipped(SpirepassRewardData reward) {
        if (reward == null) {
            return false;
        }

        if (reward.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL) {
            String entityId = reward.getEntityId();
            String modelId = reward.getModelId();
            String currentSkin = SkinManager.getInstance().getAppliedSkin(entityId);
            return modelId.equals(currentSkin);
        } else if (reward.getType() == SpirepassRewardData.RewardType.CARDBACK) {
            String cardbackType = reward.getCardbackType();
            String cardbackId = reward.getCardbackId();
            String currentCardback = SkinManager.getInstance().getAppliedCardback(cardbackType);
            return cardbackId.equals(currentCardback);
        }

        return false;
    }

    public void toggleRewardEquipped(SpirepassRewardData reward) {
        if (reward == null) {
            return;
        }

        if (reward.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL) {
            String entityId = reward.getEntityId();
            String modelId = reward.getModelId();
            String currentSkin = SkinManager.getInstance().getAppliedSkin(entityId);
            boolean shouldUnequip = modelId.equals(currentSkin);
            SkinManager.getInstance().setAppliedSkin(entityId, shouldUnequip ? "" : modelId);
            System.out.println((shouldUnequip ? "Unequipped " : "Equipped ") +
                    entityId + " skin: " + modelId);
        } else if (reward.getType() == SpirepassRewardData.RewardType.CARDBACK) {
            String cardbackType = reward.getCardbackType();
            String cardbackId = reward.getCardbackId();
            String currentCardback = SkinManager.getInstance().getAppliedCardback(cardbackType);
            boolean shouldUnequip = cardbackId.equals(currentCardback);
            SkinManager.getInstance().setAppliedCardback(cardbackType, shouldUnequip ? "" : cardbackId);
            System.out.println((shouldUnequip ? "Unequipped " : "Equipped ") +
                    cardbackType + " cardback: " + cardbackId);
        }

        // Save the config immediately after changing the reward state
        Spirepass.saveConfig();
    }

    // Getters
    public SpirepassRewardData getRewardData(int level) {
        return rewardData.getOrDefault(level, null);
    }

    public Texture getRewardTexture(int level) {
        if (rewardTextures.containsKey(level)) {
            return rewardTextures.get(level);
        } else {
            // Return the badge texture as default if level not found
            for (Integer key : rewardTextures.keySet()) {
                return rewardTextures.get(key);  // Return the first one we find
            }
            return null;  // Fallback
        }
    }
}
