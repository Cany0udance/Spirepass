package spirepass.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import spirepass.spirepassutil.SpirepassRewardData;

public class SpirepassLevelBox {
    // Constants
    private static final float BOX_SIZE = 120.0f * Settings.scale;
    private static final float BUTTON_Y = Settings.HEIGHT * 0.6f;
    private static final float BUTTON_WIDTH = 160.0f * Settings.scale;
    private static final float BUTTON_HEIGHT = 50.0f * Settings.scale;

    // State
    private int level;
    private float x;
    private float y;
    private boolean isSelected;
    private boolean isUnlocked;

    // Visual elements
    private Texture boxTexture;
    private Texture rewardTexture;
    private SpirepassRewardData rewardData;

    // Interaction
    private Hitbox boxHitbox;
    private Hitbox buttonHitbox;

    public SpirepassLevelBox(int level, float x, float y, boolean isUnlocked, Texture boxTexture, Texture rewardTexture) {
        this.level = level;
        this.isSelected = false;
        this.isUnlocked = isUnlocked;
        this.boxTexture = boxTexture;
        this.rewardTexture = rewardTexture;

        // Create hitboxes
        this.boxHitbox = new Hitbox(BOX_SIZE, BOX_SIZE);
        this.buttonHitbox = new Hitbox(BUTTON_WIDTH, BUTTON_HEIGHT);

        // Position the box (also updates hitboxes)
        update(x);
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
}