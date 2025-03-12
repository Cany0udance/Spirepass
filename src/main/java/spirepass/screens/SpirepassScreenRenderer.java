package spirepass.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;

public class SpirepassScreenRenderer {
    private Texture backgroundTexture;
    private Texture levelBoxTexture;
    private Texture currentLevelBoxTexture;
    private Texture lockedLevelBoxTexture;
    // Constants for rendering
    public static final float LEVEL_BOX_SIZE = 120.0f * Settings.scale;
    private static final float TITLE_Y = Settings.HEIGHT * 0.9f;
    public static final float LEVEL_BOX_Y = Settings.HEIGHT * 0.3f;
    public SpirepassScreenRenderer() {
        // Load the textures
        try {
            this.backgroundTexture = ImageMaster.loadImage("spirepass/images/screen/SpirepassBackground.jpg");
            // Using default textures as placeholders - replace with your own
            this.levelBoxTexture = ImageMaster.OPTION_CONFIRM;
            this.currentLevelBoxTexture = ImageMaster.OPTION_YES;
            this.lockedLevelBoxTexture = ImageMaster.OPTION_NO;
        } catch (Exception e) {
            // Fallback in case images can't be loaded
            System.err.println("Failed to load SpirePass textures: " + e.getMessage());
        }
    }
    public void render(SpriteBatch sb, SpirepassScreen screen, float scrollX, float edgePadding) {
        // Render the background image to fill the entire screen
        if (this.backgroundTexture != null) {
            sb.setColor(Color.WHITE);
            sb.draw(
                    this.backgroundTexture,
                    0, 0,
                    0, 0,
                    Settings.WIDTH, Settings.HEIGHT,
                    1, 1,
                    0,
                    0, 0,
                    this.backgroundTexture.getWidth(), this.backgroundTexture.getHeight(),
                    false, false
            );
        }
        // Render title
        FontHelper.renderFontCentered(
                sb,
                FontHelper.bannerNameFont,
                "SPIREPASS",
                Settings.WIDTH / 2.0f,
                TITLE_Y,
                Color.WHITE
        );
        // Render level boxes - pass the scrollX parameter
        renderLevelBoxes(sb, screen, scrollX, edgePadding);
        // Render the cancel button on top of everything
        screen.cancelButton.render(sb);
    }
    private void renderLevelBoxes(SpriteBatch sb, SpirepassScreen screen, float scrollX, float edgePadding) {
        int maxLevel = screen.getMaxLevel();
        int currentLevel = screen.getCurrentLevel();
        float levelBoxSpacing = screen.getLevelBoxSpacing();
        // Calculate which level boxes are currently visible
        int firstVisibleLevel = Math.max(0, (int)(scrollX / levelBoxSpacing) - 1);
        int lastVisibleLevel = Math.min(maxLevel, (int)((scrollX + Settings.WIDTH) / levelBoxSpacing) + 1);
        // Render each visible level box
        for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
            float boxX = (i * levelBoxSpacing) + edgePadding - scrollX;
            float boxY = LEVEL_BOX_Y;
            // Select the appropriate texture based on level status
            Texture boxTexture;
            Color boxColor;
            if (i == currentLevel) {
                // Current level
                boxTexture = currentLevelBoxTexture;
                boxColor = Color.WHITE;
            } else if (i > currentLevel) {
                // Locked level
                boxTexture = lockedLevelBoxTexture;
                boxColor = Color.GRAY;
            } else {
                // Unlocked level
                boxTexture = levelBoxTexture;
                boxColor = Color.WHITE;
            }
            // Render the box
            sb.setColor(boxColor);
            sb.draw(
                    boxTexture,
                    boxX - LEVEL_BOX_SIZE/2, boxY - LEVEL_BOX_SIZE/2,
                    LEVEL_BOX_SIZE, LEVEL_BOX_SIZE
            );
            // Render the level number
            FontHelper.renderFontCentered(
                    sb,
                    FontHelper.buttonLabelFont,
                    String.valueOf(i),
                    boxX,
                    boxY,
                    i <= currentLevel ? Color.WHITE : Color.DARK_GRAY
            );
        }
    }
    // For proper resource management
    public void dispose() {
        if (this.backgroundTexture != null) {
            this.backgroundTexture.dispose();
        }
        // No need to dispose of levelBoxTexture etc. as they're from ImageMaster
    }
}