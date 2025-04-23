package spirepass.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import spirepass.Spirepass;
import spirepass.spirepassutil.SkinManager;
import spirepass.spirepassutil.SpirepassPositionSettings;
import spirepass.spirepassutil.SpirepassRewardData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static spirepass.Spirepass.*;

public class SpirepassRewardManager implements Disposable {
    // ==================== STATIC & MEMBER VARIABLES ====================

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID(""));
    private HashMap<Integer, SpirepassRewardData> rewardData;
    private HashMap<Integer, Texture> rewardTextures;
    private HashMap<String, Texture> backgroundTextures;
    private Texture lockTexture;
    private SpirepassAnimationManager animationManager;

    // ==================== INITIALIZATION ====================

    public SpirepassRewardManager() {
        this.rewardData = new HashMap<>();
        this.rewardTextures = new HashMap<>();
        this.backgroundTextures = new HashMap<>();
        this.animationManager = new SpirepassAnimationManager();

        loadManagedTextures();
        initializeRewardData();
    }

    private void loadManagedTextures() {
        String[] backgroundPaths = {
                "spirepass/images/screen/CommonRewardBackground.png",
                "spirepass/images/screen/UncommonRewardBackground.png",
                "spirepass/images/screen/RareRewardBackground.png",
                "spirepass/images/screen/LockedRewardBackground.png"
        };

        for (String path : backgroundPaths) {
            try {
                backgroundTextures.put(path, ImageMaster.loadImage(path));
            } catch (Exception e) {
                System.err.println("Failed to load background texture: " + path);
            }
        }

        try {
            this.lockTexture = ImageMaster.loadImage("spirepass/images/screen/lock.png");
        } catch (Exception e) {
            System.err.println("Failed to load lock texture: " + e.getMessage());
            this.lockTexture = null;
        }
    }

    // ==================== RENDERING METHODS ====================

    private void renderLockedReward(SpriteBatch sb) {
        Texture lockedBackgroundTexture = backgroundTextures.get("spirepass/images/screen/LockedRewardBackground.png");

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

        if (this.lockTexture != null) {
            float lockSize = 120.0f * Settings.scale;
            sb.setColor(Color.WHITE);
            sb.draw(
                    this.lockTexture,
                    Settings.WIDTH / 2.0f - lockSize / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y - lockSize / 4.0f,
                    lockSize,
                    lockSize
            );
        } else {
            FontHelper.renderFontCentered(sb, FontHelper.tipBodyFont, "[X]", Settings.WIDTH / 2.0f, SpirepassPositionSettings.REWARD_PREVIEW_Y, Color.RED);
        }

        FontHelper.renderFontCentered(
                sb,
                FontHelper.tipBodyFont,
                "Reward locked. Go complete some challenges!",
                Settings.WIDTH / 2.0f,
                SpirepassPositionSettings.REWARD_PREVIEW_Y - 80.0f * Settings.scale,
                Color.WHITE
        );
    }

    public void renderReward(SpriteBatch sb, int level, boolean isUnlocked) {
        if (!isUnlocked) {
            renderLockedReward(sb);
            return;
        }
        SpirepassRewardData reward = getRewardData(level);
        if (reward != null) {
            renderRewardWithData(sb, level, isUnlocked, reward);
        } else {
            renderDefaultReward(sb, level);
        }
    }

    private void renderTextReward(SpriteBatch sb, SpirepassRewardData reward) {
        String backgroundPath = reward.getBackgroundTexturePath();
        Texture backgroundTexture = backgroundPath != null ? backgroundTextures.get(backgroundPath) : null;
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
        if (reward.getLevel() == 0) {
            UIStrings welcomeStrings = CardCrawlGame.languagePack.getUIString(makeID("SpirepassWelcomeMessage"));
            String messageText = welcomeStrings.TEXT[0];
            FontHelper.renderFontCentered(
                    sb, FontHelper.tipBodyFont, messageText, Settings.WIDTH / 2.0f, SpirepassPositionSettings.REWARD_PREVIEW_Y, Color.WHITE
            );
        } else if (reward.getLevel() == 40) {
            if (CardCrawlGame.playerName != null && !easterEggActivated && CardCrawlGame.playerName.equals("pee")) {
                // Special message for Frost
                UIStrings frostStrings = CardCrawlGame.languagePack.getUIString(makeID("SpirepassFrostMessage"));
                float baseY = SpirepassPositionSettings.REWARD_PREVIEW_Y;
                float lineGap = 30.0f * Settings.scale;

                // Calculate total height to center all lines properly
                int totalLines = frostStrings.TEXT.length;
                float totalHeight = (totalLines - 1) * lineGap;
                float startY = baseY + totalHeight / 2.0f;

                // Render each line
                for (int i = 0; i < frostStrings.TEXT.length; i++) {
                    FontHelper.renderFontCentered(
                            sb, FontHelper.tipBodyFont, frostStrings.TEXT[i],
                            Settings.WIDTH / 2.0f,
                            startY - i * lineGap,
                            Color.GOLD
                    );
                }
            } else {
                // Default max level message for everyone else
                UIStrings maxLevelStrings = CardCrawlGame.languagePack.getUIString(makeID("SpirepassMaxLevelMessage"));
                String line1 = maxLevelStrings.TEXT[0];
                String line2 = maxLevelStrings.TEXT[1];

                FontHelper.renderFontCentered(
                        sb, FontHelper.tipBodyFont, line1, Settings.WIDTH / 2.0f,
                        SpirepassPositionSettings.REWARD_PREVIEW_Y + 15.0f * Settings.scale, Color.GOLD
                );
                FontHelper.renderFontCentered(
                        sb, FontHelper.tipBodyFont, line2, Settings.WIDTH / 2.0f,
                        SpirepassPositionSettings.REWARD_PREVIEW_Y - 15.0f * Settings.scale, Color.GOLD
                );
            }
        } else {
            String messageText = "Level " + reward.getLevel() + " Reward";
            FontHelper.renderFontCentered(
                    sb, FontHelper.tipBodyFont, messageText, Settings.WIDTH / 2.0f, SpirepassPositionSettings.REWARD_PREVIEW_Y, Color.WHITE
            );
        }
    }

    private void renderRewardWithData(SpriteBatch sb, int level, boolean isUnlocked, SpirepassRewardData reward) {
        String backgroundPath = reward.getBackgroundTexturePath();
        Texture backgroundTexture = backgroundPath != null ? backgroundTextures.get(backgroundPath) : null;

        if (reward.getType() == SpirepassRewardData.RewardType.TEXT) {
            renderTextReward(sb, reward);
            return;
        }

        if (backgroundTexture != null) {
            float bgPreviewHeight = SpirepassPositionSettings.REWARD_PREVIEW_HEIGHT * SpirepassPositionSettings.REWARD_BACKGROUND_SCALE;
            float bgPreviewWidth = bgPreviewHeight * (backgroundTexture.getWidth() / (float) backgroundTexture.getHeight());
            sb.setColor(Color.WHITE);
            sb.draw(
                    backgroundTexture,
                    Settings.WIDTH / 2.0f - bgPreviewWidth / 2.0f,
                    SpirepassPositionSettings.REWARD_PREVIEW_Y - bgPreviewHeight / 2.0f,
                    bgPreviewWidth,
                    bgPreviewHeight
            );
        }

        if (reward.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL) {
            String modelId = reward.getModelId();
            String entityId = reward.getEntityId();
            String variant = SpirepassAnimationManager.getVariantFromModelId(entityId, modelId);
            animationManager.renderAnimationPreview(sb, entityId, variant);

        } else if (reward.getType() == SpirepassRewardData.RewardType.IMAGE) {
            Texture rewardTexture = getRewardTexture(level);
            if (rewardTexture != null) {
                float scale = SpirepassPositionSettings.REWARD_CONTENT_SCALE;
                float previewHeight = SpirepassPositionSettings.REWARD_IMAGE_HEIGHT * scale;
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
        } else if (reward.getType() == SpirepassRewardData.RewardType.CARDBACK) {
            Texture rewardTexture = getRewardTexture(level);
            if (rewardTexture != null) {
                float scale = SpirepassPositionSettings.CARDBACK_CONTENT_SCALE;
                float previewHeight = SpirepassPositionSettings.REWARD_IMAGE_HEIGHT * scale;
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

        Color titleColor;
        switch (reward.getRarity()) {
            case UNCOMMON:
                titleColor = new Color(0.5058f, 0.8862f, 0.9098f, 1.0f);
                break;
            case RARE:
                titleColor = new Color(0.9804f, 0.7333f, 0.2627f, 1.0f);
                break;
            case COMMON:
            default:
                titleColor = Color.WHITE;
                break;
        }
        FontHelper.renderFontCentered(
                sb, FontHelper.tipBodyFont, reward.getName(), Settings.WIDTH / 2.0f, SpirepassPositionSettings.REWARD_NAME_Y, titleColor
        );
        FontHelper.renderFontCentered(
                sb, FontHelper.tipBodyFont, reward.getDescription(), Settings.WIDTH / 2.0f, SpirepassPositionSettings.REWARD_DESCRIPTION_Y, Color.LIGHT_GRAY
        );
    }

    private void renderDefaultReward(SpriteBatch sb, int level) {
        Texture rewardTexture = getRewardTexture(level);
        if (rewardTexture != null) {
            float previewHeight = 200.0f * Settings.scale;
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
            renderFallbackText(sb, "No preview available for Level " + level, Color.ORANGE);
        }
        FontHelper.renderFontCentered(
                sb, FontHelper.tipBodyFont, "Level " + level + " Reward", Settings.WIDTH / 2.0f, SpirepassPositionSettings.REWARD_NAME_Y, Color.WHITE
        );
    }

    private void renderFallbackText(SpriteBatch sb, String text, Color color) {
        FontHelper.renderFontCentered(
                sb, FontHelper.tipBodyFont, text, Settings.WIDTH / 2.0f, SpirepassPositionSettings.REWARD_PREVIEW_Y, color
        );
    }

    // ==================== REWARD MANAGEMENT ====================

    public void initializeRewardData() {
        rewardData.put(0, new SpirepassRewardData(
                0,
                "",
                "",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.TEXT
        ));

        // Law Abiding Citizen (level 1)
        rewardData.put(1, createReward(
                1,
                "LawAbidingCitizen",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_DEFECT,
                "DEFECT_LAWABIDINGCITIZEN"
        ));

        // Giantama (level 2)
        rewardData.put(2, createReward(
                2,
                "Giantama",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_GIANT_HEAD,
                "GIANT_HEAD_GIANTAMA"
        ));

        // Space Blanket (level 3)
        rewardData.put(3, createReward(
                3,
                "SpaceBlanket",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SILENT,
                "SILENT_SPACEBLANKET"
        ));

        // My Cat Background (level 4)
        rewardData.put(4, createReward(
                4,
                "MyCatBackground",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.IMAGE,
                "spirepass/images/screen/SpirepassCatBackground.jpg"
        ));

        // Sponsored (level 5)
        rewardData.put(5, createReward(
                5,
                "Sponsored",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_COLORLESS,
                "COLORLESS_SPONSORED",
                "spirepass/images/rewards/cardbacks/colorless/sponsored/RAIDSkillLarge.png"
        ));

        // Donut (level 6)
        rewardData.put(6, createReward(
                6,
                "Donut",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_DONU,
                "DONU_DONUT"
        ));

        // Lovestruck (level 7)
        rewardData.put(7, createReward(
                7,
                "Lovestruck",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_CENTURION,
                "CENTURION_LOVESTRUCK"
        ));

        // Bear (level 8)
        rewardData.put(8, createReward(
                8,
                "Bear",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_BEAR,
                "BEAR_ACTUALBEAR"
        ));

        rewardData.put(9, createReward(
                9,
                "Blue",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_RED_SLAVER,
                "RED_SLAVER_BLUE"
        ));

        // Red (level 10)
        rewardData.put(10, createReward(
                10,
                "Red",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_BLUE_SLAVER,
                "BLUE_SLAVER_RED"
        ));

        // Blissful (level 11)
        rewardData.put(11, createReward(
                11,
                "Blissful",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_COLORLESS,
                "COLORLESS_BLISSFUL",
                "spirepass/images/rewards/cardbacks/colorless/blissful/BlissSkillLarge.png"
        ));

        // Slendernob (level 12)
        rewardData.put(12, createReward(
                12,
                "Slendernob",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_GREMLIN_NOB,
                "GREMLIN_NOB_URBANLEGEND"
        ));

        // Colgate (level 13)
        rewardData.put(13, createReward(
                13,
                "Colgate",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SNECKO,
                "SNECKO_COLGATE"
        ));

        // Purple Guy (level 14)
        rewardData.put(14, createReward(
                14,
                "PurpleGuy",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_ROMEO,
                "ROMEO_PURPLEGUY"
        ));

        // Downtrend (level 15)
        rewardData.put(15, createReward(
                15,
                "Downtrend",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_CURSE,
                "CURSE_NOTSTONKS",
                "spirepass/images/rewards/cardbacks/curse/NotStonksLarge.png"
        ));

        // Invisible Man (level 16)
        rewardData.put(16, createReward(
                16,
                "InvisibleMan",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_IRONCLAD,
                "IRONCLAD_INVISIBLEMAN"
        ));

        // Chalkboard (level 17)
        rewardData.put(17, createReward(
                17,
                "Chalkboard",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_DECA,
                "DECA_CHALKBOARD"
        ));

        // avwejkanaklwjv e (level 18)
        rewardData.put(18, createReward(
                18,
                "avwejkanaklwjve",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_DEFECT,
                "DEFECT_GARBLE"
        ));

        // kris deltarune (level 19)
        rewardData.put(19, createReward(
                19,
                "krisdeltarune",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_WATCHER,
                "WATCHER_DREEMURR"
        ));

        // Disarmed (level 20)
        rewardData.put(20, createReward(
                20,
                "Disarmed",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_CULTIST,
                "CULTIST_DISARMED"
        ));

        // Pajama Sam (level 21)
        rewardData.put(21, createReward(
                21,
                "PajamaSam",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_DEFECT,
                "DEFECT_PAJAMASAM"
        ));

        // Big Bird (level 22)
        rewardData.put(22, createReward(
                22,
                "BigBird",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_AWAKENED_ONE,
                "AWAKENED_ONE_BIGBIRD"
        ));

        // Prismatic (level 23)
        rewardData.put(23, createReward(
                23,
                "Prismatic",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SENTRY,
                "SENTRY_PRISMATIC"
        ));

        // Blurry (level 24)
        rewardData.put(24, createReward(
                24,
                "Blurry",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SILENT,
                "SILENT_BLURRY"
        ));

        // Harold Curse Cardback (level 25)
        rewardData.put(25, createReward(
                25,
                "HaroldCurseCardback",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_CURSE,
                "CURSE_HAROLD",
                "spirepass/images/rewards/cardbacks/curse/HaroldLarge.png"
        ));

        // Spaghetti & Meatballs (level 26)
        rewardData.put(26, createReward(
                26,
                "SpaghettiMeatballs",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_WRITHING_MASS,
                "WRITHING_MASS_SPAGHETTI"
        ));

        // Favorite Customer (level 27)
        rewardData.put(27, createReward(
                27,
                "FavoriteCustomer",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_COLORLESS,
                "COLORLESS_FAVORITECUSTOMER",
                "spirepass/images/rewards/cardbacks/colorless/favoritecustomer/FavoriteCustomerSkillLarge.png"
        ));

        // Weaponized 115 (level 28)
        rewardData.put(28, createReward(
                28,
                "Weaponized115",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_IRONCLAD,
                "IRONCLAD_WEAPONIZED115"
        ));

        // Jimbo Joker (level 29)
        rewardData.put(29, createReward(
                29,
                "JimboJoker",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CARDBACK,
                SkinManager.CARDBACK_COLORLESS,
                "COLORLESS_JIMBO",
                "spirepass/images/rewards/cardbacks/colorless/jimbo/JimboSkillLarge.png"
        ));

        rewardData.put(30, createReward(
                30,
                "MagicConch",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SHELLED_PARASITE,
                "SHELLED_PARASITE_MAGICCONCH"
        ));

        rewardData.put(31, createReward(
                31,
                "Robot",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SNEAKY_GREMLIN,
                "SNEAKY_GREMLIN_ROBOT"
        ));

        rewardData.put(32, createReward(
                32,
                "Pretty",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_JAW_WORM,
                "JAW_WORM_PRETTY"
        ));

        rewardData.put(33, createReward(
                33,
                "Slaverhot",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_RED_SLAVER,
                "RED_SLAVER_SLAVERHOT"
        ));

        rewardData.put(34, createReward(
                34,
                "BlueBaby",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SLIME_BOSS,
                "SLIME_BOSS_BLUEBABY"
        ));

        rewardData.put(35, createReward(
                35,
                "Simon",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_ORB_WALKER,
                "ORB_WALKER_SIMON"
        ));

        rewardData.put(36, createReward(
                36,
                "Kraken",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_SPIRE_GROWTH,
                "SPIRE_GROWTH_KRAKEN"
        ));

        rewardData.put(37, createReward(
                37,
                "Alien",
                SpirepassRewardData.RewardRarity.UNCOMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_MAD_GREMLIN,
                "MAD_GREMLIN_ALIEN"
        ));

        rewardData.put(38, createReward(
                38,
                "1Up",
                SpirepassRewardData.RewardRarity.COMMON,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_FUNGI_BEAST,
                "FUNGI_BEAST_1UP"
        ));

        rewardData.put(39, createReward(
                39,
                "ChristmasTree",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.CHARACTER_MODEL,
                SkinManager.ENTITY_CULTIST,
                "CULTIST_CHRISTMASTREE"
        ));

        rewardData.put(40, new SpirepassRewardData(
                40,
                "",
                "",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.TEXT
        ));

        rewardData.put(41, createReward(
                41,
                "TheDate",
                SpirepassRewardData.RewardRarity.RARE,
                SpirepassRewardData.RewardType.IMAGE,
                "spirepass/images/screen/SpirepassDateBackground.jpg"
        ));


        // Default reward for all other levels (badge image)
        Texture badgeTexture = null;
        try {
            badgeTexture = ImageMaster.loadImage("spirepass/images/badge.png");
        } catch (Exception e) {
            System.err.println("Failed to load badge texture: " + e.getMessage());
        }

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
        if (badgeTexture != null) {
            for (int i = 0; i <= 40; i++) {
                if (!rewardTextures.containsKey(i) && !rewardData.containsKey(i)) {
                    rewardTextures.put(i, badgeTexture);
                }
            }
        }
    }

    private SpirepassRewardData createReward(int id, String nameKey, SpirepassRewardData.RewardRarity rarity, SpirepassRewardData.RewardType type) {
        UIStrings strings = CardCrawlGame.languagePack.getUIString(makeID("SpirepassReward" + nameKey));
        return new SpirepassRewardData(id, strings.TEXT[0], strings.TEXT[1], rarity, type);
    }

    private SpirepassRewardData createReward(int id, String nameKey, SpirepassRewardData.RewardRarity rarity, SpirepassRewardData.RewardType type, String entityOrPath) {
        UIStrings strings = CardCrawlGame.languagePack.getUIString(makeID("SpirepassReward" + nameKey));
        return new SpirepassRewardData(id, strings.TEXT[0], strings.TEXT[1], rarity, type, entityOrPath);
    }

    private SpirepassRewardData createReward(int id, String nameKey, SpirepassRewardData.RewardRarity rarity, SpirepassRewardData.RewardType type, String entity, String skin) {
        UIStrings strings = CardCrawlGame.languagePack.getUIString(makeID("SpirepassReward" + nameKey));
        return new SpirepassRewardData(id, strings.TEXT[0], strings.TEXT[1], rarity, type, entity, skin);
    }

    private SpirepassRewardData createReward(int id, String nameKey, SpirepassRewardData.RewardRarity rarity, SpirepassRewardData.RewardType type, String entity, String skin, String imagePath) {
        UIStrings strings = CardCrawlGame.languagePack.getUIString(makeID("SpirepassReward" + nameKey));
        return new SpirepassRewardData(id, strings.TEXT[0], strings.TEXT[1], rarity, type, entity, skin, imagePath);
    }

    // ==================== REWARD STATE MANAGEMENT ====================

    public boolean isRewardEquipped(SpirepassRewardData reward) {
        if (reward == null) return false;
        if (reward.isBackgroundReward()) {
            String currentBackground = SkinManager.getInstance().getAppliedSkin(SkinManager.BACKGROUND_SCREEN);
            return reward.getImagePath().equals(currentBackground);
        } else if (reward.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL) {
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
        if (reward == null) return;
        boolean stateChanged = false;
        if (reward.isBackgroundReward()) {
            String currentBackground = SkinManager.getInstance().getAppliedSkin(SkinManager.BACKGROUND_SCREEN);
            boolean shouldUnequip = reward.getImagePath().equals(currentBackground);
            String newValue = shouldUnequip ? "" : reward.getImagePath();
            if (!shouldUnequip && reward.getLevel() == 41) {
                float volumeMultiplier = 4.0f;
                CardCrawlGame.sound.playV(LIP_THING_KEY, volumeMultiplier);
            }
            if (!newValue.equals(currentBackground)) {
                SkinManager.getInstance().setAppliedSkin(SkinManager.BACKGROUND_SCREEN, newValue);
                stateChanged = true;
            }
        } else if (reward.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL) {
            String entityId = reward.getEntityId();
            String modelId = reward.getModelId();
            String currentSkin = SkinManager.getInstance().getAppliedSkin(entityId);
            boolean shouldUnequip = modelId.equals(currentSkin);
            String newValue = shouldUnequip ? "" : modelId;
            if (!newValue.equals(currentSkin)) {
                SkinManager.getInstance().setAppliedSkin(entityId, newValue);
                stateChanged = true;
                // Sound stuff can go here if needed
                if (!shouldUnequip && reward.getLevel() == 12) {
                    float volumeMultiplier = 4.0f;
                    CardCrawlGame.sound.playV(DRUM_KEY, volumeMultiplier);
                }
            }
        } else if (reward.getType() == SpirepassRewardData.RewardType.CARDBACK) {
            String cardbackType = reward.getCardbackType();
            String cardbackId = reward.getCardbackId();
            String currentCardback = SkinManager.getInstance().getAppliedCardback(cardbackType);
            boolean shouldUnequip = cardbackId.equals(currentCardback);
            String newValue = shouldUnequip ? "" : cardbackId;
            if (!newValue.equals(currentCardback)) {
                SkinManager.getInstance().setAppliedCardback(cardbackType, newValue);
                stateChanged = true;
            }
        }
        if (stateChanged) {
            Spirepass.saveConfig();
        }
    }

    // ==================== GETTERS ====================

    public SpirepassRewardData getRewardData(int level) {
        if (level == 41 && !Spirepass.isEasterEggActivated()) {
            return null;
        }
        return rewardData.getOrDefault(level, null);
    }

    public Texture getRewardTexture(int level) {
        if (level == 41 && !Spirepass.isEasterEggActivated()) {
            return null;
        }

        if (rewardTextures != null && rewardTextures.containsKey(level)) {
            return rewardTextures.get(level);
        }

        return null;
    }

    // ==================== CLEANUP ====================

    @Override
    public void dispose() {
        Set<Texture> uniqueTextures = new HashSet<>();

        if (backgroundTextures != null) {
            for (Texture texture : backgroundTextures.values()) {
                if (texture != null) {
                    uniqueTextures.add(texture);
                }
            }
            backgroundTextures.clear();
        }

        if (rewardTextures != null) {
            for (Texture texture : rewardTextures.values()) {
                if (texture != null) {
                    uniqueTextures.add(texture);
                }
            }
            rewardTextures.clear();
        }

        if (this.lockTexture != null) {
            uniqueTextures.add(this.lockTexture);
        }

        for (Texture texture : uniqueTextures) {
            try {
                texture.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing texture: " + e.getMessage());
            }
        }
        uniqueTextures.clear();

        backgroundTextures = null;
        rewardTextures = null;
        lockTexture = null;
        animationManager = null;
    }
}