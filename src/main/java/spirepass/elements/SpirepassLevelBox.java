package spirepass.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import spirepass.spirepassutil.SkinManager;
import spirepass.spirepassutil.SpirepassRewardData;

public class SpirepassLevelBox {
    // Constants
    private static final float BOX_SIZE = 120.0f * Settings.scale;
    private static final float BUTTON_Y = Settings.HEIGHT * 0.6f;
    private static final float BUTTON_WIDTH = 160.0f * Settings.scale;
    private static final float BUTTON_HEIGHT = 50.0f * Settings.scale;
    private static final float STAR_SIZE = 32.0f * Settings.scale;
    private static final float STAR_Y_OFFSET = -10.0f * Settings.scale;
    private static final float STARWALKER_CHANCE = 0.005f; // 1 in 200 chance

    // State
    private int level;
    private float x;
    private float y;
    private boolean isSelected;
    private boolean isUnlocked;
    private boolean useStarwalker; // New field to store which star texture to use

    // Visual elements
    private Texture boxTexture;
    private Texture rewardTexture;
    private static Texture starTexture;  // Static - shared by all instances
    private static Texture starwalkerTexture;  // Easter egg texture
    private SpirepassRewardData rewardData;

    // Interaction
    private Hitbox boxHitbox;
    private Hitbox buttonHitbox;

    // Static initializer to load the star textures once
    static {
        try {
            starTexture = ImageMaster.loadImage("spirepass/images/starbage.png");
            starwalkerTexture = ImageMaster.loadImage("spirepass/images/starwalker.png");
        } catch (Exception e) {
            System.err.println("Failed to load star textures: " + e.getMessage());
        }
    }

    public SpirepassLevelBox(int level, float x, float y, boolean isUnlocked, Texture boxTexture, Texture rewardTexture) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.isSelected = false;
        this.isUnlocked = isUnlocked;
        this.boxTexture = boxTexture;
        this.rewardTexture = rewardTexture;

        // Determine if this level box should use the starwalker
        this.useStarwalker = MathUtils.randomBoolean(STARWALKER_CHANCE);

        // Create hitboxes
        this.boxHitbox = new Hitbox(BOX_SIZE, BOX_SIZE);
        this.buttonHitbox = new Hitbox(BUTTON_WIDTH, BUTTON_HEIGHT);
        updateHitboxPositions();
    }

    public void update(float newX) {
        this.x = newX;
        updateHitboxPositions();
        this.boxHitbox.update();
    }

    public boolean checkForClick(float mouseX, float mouseY) {
        return this.boxHitbox.hovered;
    }

    private void updateHitboxPositions() {
        this.boxHitbox.move(x, y);
        this.buttonHitbox.move(Settings.WIDTH / 2.0f, BUTTON_Y);
    }

    // Helper method to check if reward is currently equipped
    private boolean isRewardEquipped() {
        if (rewardData == null) {
            return false;
        }

        if (rewardData.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL) {
            String entityId = rewardData.getEntityId();
            String modelId = rewardData.getModelId();
            String currentSkin = SkinManager.getInstance().getAppliedSkin(entityId);
            return modelId.equals(currentSkin);
        }
        else if (rewardData.getType() == SpirepassRewardData.RewardType.CARDBACK) {
            String cardbackType = rewardData.getCardbackType();
            String cardbackId = rewardData.getCardbackId();
            String currentCardback = SkinManager.getInstance().getAppliedCardback(cardbackType);
            return cardbackId.equals(currentCardback);
        }

        return false;
    }

    public void render(SpriteBatch sb) {
        // Render box with appropriate color
        Color boxColor = isUnlocked ? Color.WHITE : Color.GRAY;
        sb.setColor(boxColor);
        sb.draw(
                boxTexture,
                x - BOX_SIZE/2, y - BOX_SIZE/2,
                BOX_SIZE, BOX_SIZE
        );

        // Render level number
        FontHelper.renderFontCentered(
                sb,
                FontHelper.buttonLabelFont,
                String.valueOf(level),
                x,
                y,
                isUnlocked ? Color.WHITE : Color.DARK_GRAY
        );

        // Render star if reward is equipped
        if (isRewardEquipped()) {
            Texture textureToUse = useStarwalker ? starwalkerTexture : starTexture;

            if (textureToUse != null) {
                sb.setColor(Color.WHITE);
                sb.draw(
                        textureToUse,
                        x - (STAR_SIZE / 2),
                        y - (BOX_SIZE / 2) - STAR_SIZE - STAR_Y_OFFSET,
                        STAR_SIZE,
                        STAR_SIZE
                );
            }
        }
    }

    // Data methods
    public void setRewardData(SpirepassRewardData rewardData) {
        this.rewardData = rewardData;
    }

    // Getters and setters
    public SpirepassRewardData getRewardData() {
        return this.rewardData;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public int getLevel() {
        return level;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getBoxSize() {
        return BOX_SIZE;
    }
}