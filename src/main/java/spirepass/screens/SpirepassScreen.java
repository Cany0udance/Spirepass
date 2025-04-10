package spirepass.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import spirepass.Spirepass;
import spirepass.elements.SpirepassLevelBox;
import spirepass.spirepassutil.SpirepassPositionSettings;
import spirepass.spirepassutil.SpirepassRewardData;

import java.util.ArrayList;

public class SpirepassScreen {
    // UI components
    public MenuCancelButton cancelButton;
    private SpirepassScreenRenderer renderer;
    private SpirepassRewardManager rewardManager;
    private SpirepassAnimationManager animationManager;

    // State variables
    public boolean isScreenOpened;
    private int selectedLevel = -1;

    // Scrolling variables
    private float scrollX = 0f;
    private float targetScrollX = 0f;
    private float minScrollX = 0f;
    private float maxScrollX = 0f;

    // Drag handling
    private boolean isDragging = false;
    private float dragStartX = 0f;
    private float lastMouseX = 0f;
    private boolean hasDraggedSignificantly = false;
    private static final float DRAG_THRESHOLD = 5.0f * Settings.scale;

    // Configuration
    private int maxLevel = 30;
    private int currentLevel = 0;
    private float levelBoxSpacing = 150f * Settings.scale;
    public float edgePadding = 100f * Settings.scale;

    // Level boxes
    private ArrayList<SpirepassLevelBox> levelBoxes;

    // ==================== LIFECYCLE METHODS ====================

    public SpirepassScreen() {
        this.cancelButton = new MenuCancelButton();
        this.isScreenOpened = false;
        this.renderer = new SpirepassScreenRenderer();
        this.rewardManager = this.renderer.getRewardManager();
        this.animationManager = this.renderer.getAnimationManager();
        this.levelBoxes = new ArrayList<>();
    }

    public void open() {
        this.cancelButton.show(CardCrawlGame.languagePack.getUIString("DungeonMapScreen").TEXT[1]);
        this.isScreenOpened = true;

        this.maxLevel = Spirepass.MAX_LEVEL;
        this.currentLevel = Spirepass.getCurrentLevel();

        initializeLevelBoxes();
        calculateScrollBounds();

        if (Spirepass.jumpToCurrentLevel) {
            centerOnLevel(currentLevel);
            setSelectedLevel(currentLevel);
        } else {
            centerOnLevel(0);
            setSelectedLevel(0);
        }

        InputHelper.justClickedLeft = false;
        InputHelper.justReleasedClickLeft = false;
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
        updateLevelBoxPositions();
        updateLevelBoxes();

        renderer.updateUI();
    }

    public void render(SpriteBatch sb) {
        this.renderer.render(sb, this, scrollX, edgePadding, levelBoxes);

        if (selectedLevel != -1 && selectedLevel < levelBoxes.size()) {
            SpirepassLevelBox selectedBox = levelBoxes.get(selectedLevel);
            this.renderer.renderSelectedLevelReward(sb, selectedBox);
        }

        this.cancelButton.render(sb);
        sb.setColor(Color.WHITE);
        this.renderer.renderUIElements(sb);
    }

    // ==================== LEVEL BOX MANAGEMENT ====================

    private void initializeLevelBoxes() {
        levelBoxes.clear();

        for (int i = 0; i <= maxLevel; i++) {
            Texture boxTexture;
            if (i == currentLevel) {
                boxTexture = renderer.getCurrentLevelBoxTexture();
            } else if (i > currentLevel) {
                boxTexture = renderer.getLockedLevelBoxTexture();
            } else {
                boxTexture = renderer.getLevelBoxTexture();
            }

            float boxX = (i * levelBoxSpacing) + edgePadding - scrollX;
            float boxY = SpirepassPositionSettings.LEVEL_BOX_Y;
            boolean isUnlocked = i <= currentLevel;

            SpirepassLevelBox levelBox = new SpirepassLevelBox(i, boxX, boxY, isUnlocked, boxTexture, this.rewardManager);

            levelBoxes.add(levelBox);
        }
        updateLevelBoxPositions();
    }

    private void updateLevelBoxPositions() {
        for (int i = 0; i < levelBoxes.size(); i++) {
            float boxX = (i * levelBoxSpacing) + edgePadding - scrollX;
            levelBoxes.get(i).update(boxX);
        }
    }

    private void updateLevelBoxes() {
        int firstVisibleLevel = Math.max(0, (int)((scrollX - edgePadding) / levelBoxSpacing) - 1);
        int lastVisibleLevel = Math.min(maxLevel, (int)((scrollX + Settings.WIDTH - edgePadding) / levelBoxSpacing) + 1);

        boolean checkForClicks = InputHelper.justReleasedClickLeft && !hasDraggedSignificantly;
        float mouseX = InputHelper.mX;
        float mouseY = InputHelper.mY;

        for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
            if (i < levelBoxes.size()) {
                SpirepassLevelBox box = levelBoxes.get(i);
                box.update((i * levelBoxSpacing) + edgePadding - scrollX);

                if (checkForClicks && box.checkForClick(mouseX, mouseY)) {
                    onLevelBoxClicked(i);
                    break;
                }
            }
        }
    }

    private void onLevelBoxClicked(int level) {
        centerOnLevel(level);
        CardCrawlGame.sound.play("UI_CLICK_1");
    }

    private void setSelectedLevel(int level) {
        if (selectedLevel != -1 && selectedLevel < levelBoxes.size()) {
            levelBoxes.get(selectedLevel).setSelected(false);
        }

        selectedLevel = level;

        if (selectedLevel != -1 && selectedLevel < levelBoxes.size()) {
            levelBoxes.get(selectedLevel).setSelected(true);
        }
    }

    public void centerOnLevel(int level) {
        float targetX = (level * levelBoxSpacing) + edgePadding - (Settings.WIDTH / 2);
        targetScrollX = Math.max(minScrollX, Math.min(targetX, maxScrollX));
        setSelectedLevel(level);
    }

    // ==================== SCROLLING MANAGEMENT ====================

    private void calculateScrollBounds() {
        float contentWidth = (maxLevel * levelBoxSpacing) + (2 * edgePadding);
        minScrollX = 0;
        maxScrollX = Math.max(0, contentWidth - Settings.WIDTH);
    }

    private void updateScrolling() {
        if (InputHelper.justClickedLeft) {
            isDragging = true;
            hasDraggedSignificantly = false;
            dragStartX = InputHelper.mX;
            lastMouseX = dragStartX;
        } else if (InputHelper.justReleasedClickLeft) {
            isDragging = false;
        }

        if (isDragging && InputHelper.isMouseDown) {
            if (Math.abs(dragStartX - InputHelper.mX) > DRAG_THRESHOLD) {
                float deltaX = lastMouseX - InputHelper.mX;
                targetScrollX += deltaX;
                hasDraggedSignificantly = true;
            }
            lastMouseX = InputHelper.mX;
        }

        targetScrollX = Math.max(minScrollX, Math.min(targetScrollX, maxScrollX));
        scrollX = MathHelper.scrollSnapLerpSpeed(scrollX, targetScrollX);
    }

    // ==================== GETTERS ====================

    public int getMaxLevel() {
        return maxLevel;
    }

    public float getLevelBoxSpacing() {
        return levelBoxSpacing;
    }

    // ==================== CLEANUP ====================

    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
    }
}