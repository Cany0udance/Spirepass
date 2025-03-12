package spirepass.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;

public class SpirepassScreen {
    public MenuCancelButton cancelButton;
    public boolean isScreenOpened;
    private SpirepassScreenRenderer renderer;
    // Scrolling variables
    private float scrollX = 0f;
    private float targetScrollX = 0f;
    private boolean isDragging = false;
    private float dragStartX = 0f;
    private float lastMouseX = 0f;
    // Spirepass level configuration
    private int maxLevel = 30; // Default max level
    private int currentLevel = 0; // Player's current level
    private float levelBoxSpacing = 150f * Settings.scale; // Spacing between level boxes
    public float edgePadding = 100f * Settings.scale; // Adjust this value as needed
    private static final float DRAG_THRESHOLD = 5.0f * Settings.scale;
    public SpirepassScreen() {
        this.cancelButton = new MenuCancelButton();
        this.isScreenOpened = false;
        this.renderer = new SpirepassScreenRenderer();
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        calculateScrollBounds();
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = Math.min(Math.max(0, currentLevel), maxLevel);
    }

    public void setLevelBoxSpacing(float spacing) {
        this.levelBoxSpacing = spacing * Settings.scale;
        calculateScrollBounds();
    }

    private float minScrollX = 0f;

    private float maxScrollX = 0f;
    private void calculateScrollBounds() {
        // Content width should include both edge paddings
        float contentWidth = (maxLevel * levelBoxSpacing) + (2 * edgePadding);
        minScrollX = 0; // Start at zero
        maxScrollX = Math.max(0, contentWidth - Settings.WIDTH);
    }

    public void centerOnLevel(int level) {
        float targetX = (level * levelBoxSpacing) + edgePadding - (Settings.WIDTH / 2);
        targetScrollX = Math.max(minScrollX, Math.min(targetX, maxScrollX));
    }

    public float getEdgePadding() {
        return edgePadding;
    }

    public void open() {
        // Don't darken the menu screen since we're replacing it with our own background
        this.cancelButton.show(CardCrawlGame.languagePack.getUIString("DungeonMapScreen").TEXT[1]);
        this.isScreenOpened = true;
        calculateScrollBounds();
        centerOnLevel(currentLevel);
    }

    public void close() {
        CardCrawlGame.mainMenuScreen.lighten();
        this.cancelButton.hide();
        CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
        this.isScreenOpened = false;
    }

    public void update() {
        this.cancelButton.update();
        if (this.cancelButton.hb.clicked || InputHelper.pressedEscape) {
            InputHelper.pressedEscape = false;
            this.cancelButton.hb.clicked = false;
            close();
            return;
        }
        updateScrolling();
        updateLevelBoxes();
    }

    private void updateScrolling() {
        // Handle mouse drag scrolling
        if (InputHelper.justClickedLeft) {
            isDragging = true;
            dragStartX = InputHelper.mX;
            lastMouseX = dragStartX;
        } else if (InputHelper.justReleasedClickLeft) {
            isDragging = false;
        }

        if (isDragging && InputHelper.isMouseDown) {
            // Only apply scrolling if we've dragged beyond the threshold
            if (Math.abs(dragStartX - InputHelper.mX) > DRAG_THRESHOLD) {
                float deltaX = lastMouseX - InputHelper.mX;
                targetScrollX += deltaX;
            }
            lastMouseX = InputHelper.mX;
        }

        // Clamp scrolling
        targetScrollX = Math.max(minScrollX, Math.min(targetScrollX, maxScrollX));
        // Smooth scrolling
        scrollX = MathHelper.scrollSnapLerpSpeed(scrollX, targetScrollX);
    }
    private void updateLevelBoxes() {
        // Calculate which level boxes are currently visible
        int firstVisibleLevel = Math.max(0, (int)((scrollX - edgePadding) / levelBoxSpacing) - 1);
        int lastVisibleLevel = Math.min(maxLevel, (int)((scrollX + Settings.WIDTH - edgePadding) / levelBoxSpacing) + 1);

        // Update each visible level box
        for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
            float boxX = (i * levelBoxSpacing) + edgePadding - scrollX;
            float boxY = Settings.HEIGHT / 2f;

            // Only count as a click if we haven't dragged beyond the threshold
            if (InputHelper.justReleasedClickLeft && (!isDragging || Math.abs(dragStartX - InputHelper.mX) <= DRAG_THRESHOLD)) {
                if (isPointInLevelBox(InputHelper.mX, InputHelper.mY, boxX, boxY)) {
                    onLevelBoxClicked(i);
                }
            }
        }
    }

    private boolean isPointInLevelBox(float pointX, float pointY, float boxX, float boxY) {
        float boxSize = SpirepassScreenRenderer.LEVEL_BOX_SIZE;
        return (pointX >= boxX - boxSize/2 && pointX <= boxX + boxSize/2 &&
                pointY >= boxY - boxSize/2 && pointY <= boxY + boxSize/2);
    }

    private void onLevelBoxClicked(int level) {
        // For now, just center on the level that was clicked
        centerOnLevel(level);
        // Later, this could show level details, rewards, etc.
    }

    public void render(SpriteBatch sb) {
        this.renderer.render(sb, this, scrollX, edgePadding);
    }

    // For proper resource management
    public void dispose() {
        this.renderer.dispose();
    }

    // Getters for the renderer
    public int getMaxLevel() {
        return maxLevel;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public float getLevelBoxSpacing() {
        return levelBoxSpacing;
    }
}