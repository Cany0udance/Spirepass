package spirepass.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import spirepass.util.SpirepassRewardData;

public class SpirepassLevelBox {
    private static final float BOX_SIZE = 120.0f * Settings.scale;
    private static final float BUTTON_Y = Settings.HEIGHT * 0.6f;
    private static final float BUTTON_WIDTH = 160.0f * Settings.scale;
    private static final float BUTTON_HEIGHT = 50.0f * Settings.scale;

    private int level;
    private float x;
    private float y;
    private boolean isSelected;
    private boolean isUnlocked;

    private Texture boxTexture;
    private Texture rewardTexture;
    private Hitbox boxHitbox;
    private Hitbox buttonHitbox;
    private SpirepassRewardData rewardData;

    public SpirepassLevelBox(int level, float x, float y, boolean isUnlocked, Texture boxTexture, Texture rewardTexture) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.isSelected = false;
        this.isUnlocked = isUnlocked;
        this.boxTexture = boxTexture;
        this.rewardTexture = rewardTexture;

        this.boxHitbox = new Hitbox(BOX_SIZE, BOX_SIZE);
        this.buttonHitbox = new Hitbox(BUTTON_WIDTH, BUTTON_HEIGHT);
        updateHitboxPositions();
    }

    public void update(float newX) {
        this.x = newX;
        updateHitboxPositions();

        // Update the hitboxes
        this.boxHitbox.update();

        // We no longer need to check isSelected here since the button is now
        // rendered by SpirepassScreenRenderer
    }
    public boolean checkForClick(float mouseX, float mouseY) {
        return this.boxHitbox.hovered &&
                mouseX >= x - BOX_SIZE/2 && mouseX <= x + BOX_SIZE/2 &&
                mouseY >= y - BOX_SIZE/2 && mouseY <= y + BOX_SIZE/2;
    }

    private void updateHitboxPositions() {
        this.boxHitbox.move(x, y);
        this.buttonHitbox.move(Settings.WIDTH / 2.0f, BUTTON_Y);
    }

    public void setRewardData(SpirepassRewardData rewardData) {
        this.rewardData = rewardData;
    }

    // Add this getter:
    public SpirepassRewardData getRewardData() {
        return this.rewardData;
    }

    private void onButtonClicked() {
        if (isUnlocked) {
            // Equip the reward
            CardCrawlGame.sound.play("UI_CLICK_1");
            // TODO: Implement equipping logic
            System.out.println("Equipped reward for level " + level);
        } else {
            // Can't equip - level is locked
            CardCrawlGame.sound.play("UI_CLICK_2");
            System.out.println("Level " + level + " is locked!");
        }
    }

    public void render(SpriteBatch sb) {
        // Render the level box
        Color boxColor = isUnlocked ? Color.WHITE : Color.GRAY;
        sb.setColor(boxColor);
        sb.draw(
                boxTexture,
                x - BOX_SIZE/2, y - BOX_SIZE/2,
                BOX_SIZE, BOX_SIZE
        );

        // Render the level number
        FontHelper.renderFontCentered(
                sb,
                FontHelper.buttonLabelFont,
                String.valueOf(level),
                x,
                y,
                isUnlocked ? Color.WHITE : Color.DARK_GRAY
        );

        // Remove the if(isSelected) block that used to render preview and button
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