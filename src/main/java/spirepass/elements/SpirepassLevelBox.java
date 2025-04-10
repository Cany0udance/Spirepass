package spirepass.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import spirepass.screens.SpirepassRewardManager;
import spirepass.spirepassutil.SkinManager;
import spirepass.spirepassutil.SpirepassRewardData;

public class SpirepassLevelBox {
    // ==================== CONSTANTS ====================

    private static final float BOX_SIZE = 120.0f * Settings.scale;
    private static final float BUTTON_Y = Settings.HEIGHT * 0.6f;
    private static final float BUTTON_WIDTH = 160.0f * Settings.scale;
    private static final float BUTTON_HEIGHT = 50.0f * Settings.scale;
    private static final float STAR_SIZE = 32.0f * Settings.scale;
    private static final float STAR_Y_OFFSET = -10.0f * Settings.scale;
    private static final float STARWALKER_CHANCE = 0.005f; // 1 in 200 chance

    // ==================== STATE VARIABLES ====================

    private int level;
    private float x;
    private float y;
    private boolean isSelected;
    private boolean isUnlocked;
    private boolean useStarwalker;

    // ==================== VISUAL ELEMENTS ====================

    private Texture boxTexture;
    private Texture rewardTexture;
    private static Texture starTexture;
    private static Texture starwalkerTexture;
    private SpirepassRewardManager rewardManager;
    private SpirepassRewardData rewardData;

    // ==================== INTERACTION ====================

    private Hitbox boxHitbox;
    private Hitbox buttonHitbox;

    // ==================== INITIALIZATION ====================

    static {
        try {
            starTexture = ImageMaster.loadImage("spirepass/images/starbage.png");
            starwalkerTexture = ImageMaster.loadImage("spirepass/images/starwalker.png");
        } catch (Exception e) {
            System.err.println("Failed to load star textures: " + e.getMessage());
        }
    }

    public SpirepassLevelBox(int level, float x, float y, boolean isUnlocked, Texture boxTexture, SpirepassRewardManager rewardManager) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.isSelected = false;
        this.isUnlocked = isUnlocked;
        this.boxTexture = boxTexture;
        this.rewardManager = rewardManager;

        // Fetch reward data using the manager
        this.rewardData = this.rewardManager.getRewardData(level);

        this.useStarwalker = MathUtils.randomBoolean(STARWALKER_CHANCE);
        this.boxHitbox = new Hitbox(BOX_SIZE, BOX_SIZE);
        updateHitboxPositions();
    }

    // ==================== UPDATE METHODS ====================

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
    }

    // ==================== RENDER METHODS ====================

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

    // ==================== HELPER METHODS ====================

    private boolean isRewardEquipped() {
        // Delegate the check to the central reward manager
        if (this.rewardManager != null && this.rewardData != null) {
            return this.rewardManager.isRewardEquipped(this.rewardData);
        }
        return false;
    }

    // ==================== GETTERS & SETTERS ====================

    public void setRewardData(SpirepassRewardData rewardData) {
        this.rewardData = rewardData;
    }

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