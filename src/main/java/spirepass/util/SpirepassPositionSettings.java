package spirepass.util;

import com.megacrit.cardcrawl.core.Settings;

/**
 * A simple class to hold position settings for the Spirepass UI elements.
 * This makes it easy to adjust positions from one central place.
 */
public class SpirepassPositionSettings {
    // Main screen elements
    public static final float TITLE_Y = Settings.HEIGHT * 0.9f;
    public static final float LEVEL_BOX_Y = Settings.HEIGHT * 0.3f;

    // Reward preview elements
    public static final float REWARD_PREVIEW_Y = Settings.HEIGHT * 0.65f;
    public static final float REWARD_NAME_Y = REWARD_PREVIEW_Y + 195.0f * Settings.scale;
    public static final float REWARD_DESCRIPTION_Y = REWARD_PREVIEW_Y + 165.0f * Settings.scale;
    public static final float REWARD_BUTTON_Y = Settings.HEIGHT * 0.48f;

    // Reward preview sizes and scaling
    public static final float REWARD_BACKGROUND_SCALE = 1.4f;  // Single scale factor for all backgrounds
    public static final float REWARD_CONTENT_SCALE = 1.4f;     // Single scale factor for all reward content
    public static final float CHARACTER_MODEL_SCALE = 1.5f;    // Scale factor for character models

    // Base sizes (before scaling)
    public static final float REWARD_PREVIEW_HEIGHT = 300.0f * Settings.scale;
    public static final float REWARD_IMAGE_HEIGHT = 200.0f * Settings.scale;

    // Button dimensions
    public static final float BUTTON_WIDTH = 160.0f * Settings.scale;
    public static final float BUTTON_HEIGHT = 50.0f * Settings.scale;

    // Character model offset (for proper positioning)
    public static final float CHARACTER_MODEL_Y_OFFSET = 100.0f * Settings.scale;
}