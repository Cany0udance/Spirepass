package spirepass.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuButton;
import javassist.CtBehavior;
import spirepass.screens.SpirepassScreen;
import spirepass.spirepassutil.SpirepassManager;

import static spirepass.Spirepass.makeID;

public class MainMenuPatches {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("MainMenuButton"));
    // Define new enum values
    public static class Enums {
        @SpireEnum
        public static MenuButton.ClickResult SPIREPASS_BUTTON;
        @SpireEnum
        public static MainMenuScreen.CurScreen SPIREPASS_VIEW;
    }

    // Patch to add our button to the menu
    @SpirePatch2(clz = MainMenuScreen.class, method = "setMainMenuButtons")
    public static class ButtonAdderPatch {
        @SpireInsertPatch(locator = ButtonLocator.class, localvars = {"index"})
        public static void setMainMenuButtons(MainMenuScreen __instance, @ByRef int[] index) {
            // Add our button at the current index and increment
            __instance.buttons.add(new MenuButton(Enums.SPIREPASS_BUTTON, index[0]));
            index[0]++;
        }

        // Target location to insert our button
        private static class ButtonLocator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(Settings.class, "isShowBuild");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    // Patch to set the label text for our button
    @SpirePatch2(clz = MenuButton.class, method = "setLabel")
    public static class SetText {
        @SpirePostfixPatch
        public static void setLabelText(MenuButton __instance, @ByRef String[] ___label) {
            if (__instance.result == Enums.SPIREPASS_BUTTON) {
                ___label[0] = uiStrings.TEXT[0];
            }
        }
    }

    // Patch to handle clicks on our button
    @SpirePatch2(clz = MenuButton.class, method = "buttonEffect")
    public static class OnClickButton {
        @SpirePostfixPatch
        public static void handleClick(MenuButton __instance) {
            if (__instance.result == Enums.SPIREPASS_BUTTON) {
                CardCrawlGame.sound.play("UNLOCK_PING");
                // Open the SpirePass screen
                if (SpirepassManager.spirePassScreen == null) {
                    SpirepassManager.spirePassScreen = new SpirepassScreen();
                }
                SpirepassManager.spirePassScreen.open();
                CardCrawlGame.mainMenuScreen.screen = Enums.SPIREPASS_VIEW;
            }
        }
    }

    // Patch to handle SpirePass screen updates
    @SpirePatch2(clz = MainMenuScreen.class, method = "update")
    public static class UpdatePatch {
        @SpirePostfixPatch
        public static void updateSpirePass(MainMenuScreen __instance) {
            if (__instance.screen == Enums.SPIREPASS_VIEW) {
                SpirepassManager.spirePassScreen.update();
            }
        }
    }

    // Patch to handle SpirePass screen rendering
    @SpirePatch2(clz = MainMenuScreen.class, method = "render")
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void renderSpirePass(MainMenuScreen __instance, SpriteBatch sb) {
            if (__instance.screen == Enums.SPIREPASS_VIEW) {
                SpirepassManager.spirePassScreen.render(sb);
            }
        }
    }
}
