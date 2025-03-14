package spirepass.screens;

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
import spirepass.util.SpirepassPositionSettings;
import spirepass.util.SpirepassRewardData;

import java.util.ArrayList;

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
    private static final float DRAG_THRESHOLD = 5.0f * Settings.scale;
    private boolean hasDraggedSignificantly = false;
    // Spirepass level configuration
    private int maxLevel = 30; // Default max level
    private int currentLevel = 30; // Player's current level
    private float levelBoxSpacing = 150f * Settings.scale; // Spacing between level boxes
    public float edgePadding = 100f * Settings.scale; // Adjust this value as needed

    // Level boxes
    private ArrayList<SpirepassLevelBox> levelBoxes;
    private int selectedLevel = -1;

    public SpirepassScreen() {
        this.cancelButton = new MenuCancelButton();
        this.isScreenOpened = false;
        this.renderer = new SpirepassScreenRenderer();
        this.levelBoxes = new ArrayList<>();
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

        // Set the selected level
        setSelectedLevel(level);
    }

    private void setSelectedLevel(int level) {
        // Deselect previous selection
        if (selectedLevel != -1 && selectedLevel < levelBoxes.size()) {
            levelBoxes.get(selectedLevel).setSelected(false);
        }

        // Set new selection
        selectedLevel = level;

        // Select new level box
        if (selectedLevel != -1 && selectedLevel < levelBoxes.size()) {
            levelBoxes.get(selectedLevel).setSelected(true);
        }
    }

    public float getEdgePadding() {
        return edgePadding;
    }

    public void open() {
        // Don't darken the menu screen since we're replacing it with our own background
        this.cancelButton.show(CardCrawlGame.languagePack.getUIString("DungeonMapScreen").TEXT[1]);
        this.isScreenOpened = true;

        // Initialize or update level boxes
        initializeLevelBoxes();

        calculateScrollBounds();
        centerOnLevel(currentLevel);

        // Explicitly set the current level as selected
        setSelectedLevel(currentLevel);

        // Ignore clicks for a brief moment
        InputHelper.justClickedLeft = false;
        InputHelper.justReleasedClickLeft = false;
    }

    // Replace your initializeLevelBoxes method with this updated version:
    private void initializeLevelBoxes() {
        levelBoxes.clear();

        // Create level boxes
        for (int i = 0; i <= maxLevel; i++) {
            // Calculate initial position
            float boxX = (i * levelBoxSpacing) + edgePadding - scrollX;
            float boxY = SpirepassPositionSettings.LEVEL_BOX_Y;

            // Choose appropriate texture
            Texture boxTexture;
            if (i == currentLevel) {
                boxTexture = renderer.getCurrentLevelBoxTexture();
            } else if (i > currentLevel) {
                boxTexture = renderer.getLockedLevelBoxTexture();
            } else {
                boxTexture = renderer.getLevelBoxTexture();
            }

            // Get reward texture for this level
            Texture rewardTexture = renderer.getRewardTexture(i);

            // Create the level box
            boolean isUnlocked = i <= currentLevel;
            SpirepassLevelBox levelBox = new SpirepassLevelBox(i, boxX, boxY, isUnlocked, boxTexture, rewardTexture);

            // Set the reward data if available
            SpirepassRewardData rewardData = renderer.getRewardData(i);
            if (rewardData != null) {
                levelBox.setRewardData(rewardData);
            }

            levelBoxes.add(levelBox);
        }
    }

    private void updateLevelBoxPositions() {
        // Update the positions of all level boxes based on current scroll
        for (int i = 0; i < levelBoxes.size(); i++) {
            float boxX = (i * levelBoxSpacing) + edgePadding - scrollX;
            levelBoxes.get(i).update(boxX);
        }
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

        // Check for clicks on the equip button when a level is selected
        if (selectedLevel != -1 && selectedLevel < levelBoxes.size() && InputHelper.justReleasedClickLeft) {
            SpirepassLevelBox selectedBox = levelBoxes.get(selectedLevel);
            float BUTTON_Y = Settings.HEIGHT * 0.6f;
            float BUTTON_WIDTH = 160.0f * Settings.scale;
            float BUTTON_HEIGHT = 50.0f * Settings.scale;

            // Check if mouse is over the button
            if (InputHelper.mX >= Settings.WIDTH / 2.0f - BUTTON_WIDTH / 2.0f &&
                    InputHelper.mX <= Settings.WIDTH / 2.0f + BUTTON_WIDTH / 2.0f &&
                    InputHelper.mY >= BUTTON_Y - BUTTON_HEIGHT / 2.0f &&
                    InputHelper.mY <= BUTTON_Y + BUTTON_HEIGHT / 2.0f) {

                // Handle button click
                if (selectedBox.isUnlocked()) {
                    // Equip the reward
                    CardCrawlGame.sound.play("UI_CLICK_1");

                    // Get the reward data from the selected box
                    SpirepassRewardData rewardData = selectedBox.getRewardData();

                    if (rewardData != null && rewardData.getType() == SpirepassRewardData.RewardType.CHARACTER_MODEL) {
                        String modelId = rewardData.getModelId();
                        String entityId = rewardData.getEntityId();

                        // Toggle the skin equipped state using entity ID
                        toggleSkinEquipped(entityId, modelId);
                    }
                }
            }
        }
    }

    private void toggleSkinEquipped(String entityId, String modelId) {
        // Get the current skin for this entity type
        String currentSkin = Spirepass.getAppliedSkin(entityId);

        // Check if this skin is already equipped - if so, unequip it
        boolean isUnequipping = modelId.equals(currentSkin);

        // Set the new value
        Spirepass.setAppliedSkin(entityId, isUnequipping ? "" : modelId);

        try {
            // No need to manually save config here, setAppliedSkin handles it
            System.out.println((isUnequipping ? "Unequipped " : "Equipped ") +
                    entityId + " skin: " + modelId);
        } catch (Exception e) {
            System.err.println("Failed to save skin preference: " + e.getMessage());
        }

        // Debug print
        System.out.println("DEBUG - Current skins: " + Spirepass.appliedSkins);
    }


    private void updateScrolling() {
        // Handle mouse drag scrolling
        if (InputHelper.justClickedLeft) {
            isDragging = true;
            hasDraggedSignificantly = false; // Reset at the start of a new drag
            dragStartX = InputHelper.mX;
            lastMouseX = dragStartX;
        } else if (InputHelper.justReleasedClickLeft) {
            isDragging = false;
            // Don't reset hasDraggedSignificantly here, so it persists until the next click
        }

        if (isDragging && InputHelper.isMouseDown) {
            // Only apply scrolling if we've dragged beyond the threshold
            if (Math.abs(dragStartX - InputHelper.mX) > DRAG_THRESHOLD) {
                float deltaX = lastMouseX - InputHelper.mX;
                targetScrollX += deltaX;
                hasDraggedSignificantly = true; // Mark that we've dragged significantly
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

        // Update all visible level boxes first
        for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
            if (i < levelBoxes.size()) {
                levelBoxes.get(i).update((i * levelBoxSpacing) + edgePadding - scrollX);
            }
        }

        // Handle manual click detection - only if we haven't dragged significantly
        if (InputHelper.justReleasedClickLeft && !hasDraggedSignificantly) {
            float mouseX = InputHelper.mX;
            float mouseY = InputHelper.mY;

            for (int i = firstVisibleLevel; i <= lastVisibleLevel; i++) {
                if (i < levelBoxes.size()) {
                    SpirepassLevelBox box = levelBoxes.get(i);
                    if (box.checkForClick(mouseX, mouseY)) {
                        onLevelBoxClicked(i);
                        break;
                    }
                }
            }
        }
    }

    private void onLevelBoxClicked(int level) {
        // Center on the level that was clicked
        centerOnLevel(level);
        // Play a sound
        CardCrawlGame.sound.play("UI_CLICK_1");
    }

    public void render(SpriteBatch sb) {
        this.renderer.render(sb, this, scrollX, edgePadding, levelBoxes);

        // Always render the selected level rewards, regardless of visibility
        if (selectedLevel != -1 && selectedLevel < levelBoxes.size()) {
            SpirepassLevelBox selectedBox = levelBoxes.get(selectedLevel);
            this.renderer.renderSelectedLevelReward(sb, selectedBox);
        }

        this.cancelButton.render(sb);
    }

    // Getters for the renderer
    public int getMaxLevel() {
        return maxLevel;
    }

    public float getLevelBoxSpacing() {
        return levelBoxSpacing;
    }

}