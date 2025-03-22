package spirepass.spirepassutil;

import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.core.Settings;

/**
 * A simple class to hold position settings for the Spirepass UI elements.
 * This makes it easy to adjust positions from one central place.
 */
public class SpirepassPositionSettings {
    // Main screen elements
    public static final float TITLE_Y = Settings.HEIGHT * 0.93f;
    public static final float LEVEL_BOX_Y = Settings.HEIGHT * 0.3f;

    // Reward preview elements
    public static final float REWARD_PREVIEW_Y = Settings.HEIGHT * 0.63f;
    public static final float REWARD_NAME_Y = REWARD_PREVIEW_Y + 260.0f * Settings.scale;
    public static final float REWARD_DESCRIPTION_Y = REWARD_PREVIEW_Y + 230.0f * Settings.scale;
    public static final float REWARD_BUTTON_Y = Settings.HEIGHT * 0.4f;

    // Reward preview sizes and scaling
    public static final float REWARD_BACKGROUND_SCALE = 1.4f;  // Single scale factor for all backgrounds
    public static final float REWARD_CONTENT_SCALE = 1.4f;     // Single scale factor for all reward content
    public static final float CHARACTER_MODEL_SCALE = 1.3f;    // Scale factor for character models
    public static final float MONSTER_MODEL_SCALE = 1.3f; // Adjust this value as needed for monsters

    // Base sizes (before scaling)
    public static final float REWARD_PREVIEW_HEIGHT = 300.0f * Settings.scale;
    public static final float REWARD_IMAGE_HEIGHT = 200.0f * Settings.scale;

    // Button dimensions
    public static final float BUTTON_WIDTH = 160.0f * Settings.scale;
    public static final float BUTTON_HEIGHT = 50.0f * Settings.scale;

    // Character model offset (for proper positioning)
    public static final float CHARACTER_MODEL_Y_OFFSET = 125.0f * Settings.scale;
    public static final float MONSTER_MODEL_Y_OFFSET = 50.0f * Settings.scale; // Adjust as needed
    // Progress bar settings
    public static final float PROGRESS_BAR_HEIGHT = 8.0f * Settings.scale;
    public static final float PROGRESS_BAR_Y_OFFSET = -55.0f * Settings.scale; // Offset from the level box bottom
    public static final float PROGRESS_BAR_X_OFFSET = 5.0f * Settings.scale; // Horizontal offset
    public static final Color PROGRESS_BAR_BG_COLOR = new Color(0.3f, 0.3f, 0.3f, 0.8f);
    public static final Color PROGRESS_BAR_CURRENT_COLOR = new Color(0.2f, 0.6f, 1.0f, 0.9f); // Blue for current level
    public static final Color PROGRESS_BAR_COMPLETED_COLOR = new Color(0.2f, 0.8f, 0.2f, 0.9f); // Green for completed levels
}