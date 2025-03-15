package spirepass.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import spirepass.Spirepass;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

@SpirePatch(
        clz = AbstractCard.class,
        method = "renderCardBg"
)
public class CardbackPatch {
    // Cache textures to avoid reloading
    private static HashMap<String, Texture> cardbackTextureCache = new HashMap<>();

    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractCard __instance, SpriteBatch sb, float x, float y) {
        // Only apply for colorless and curse cards
        if (__instance.color == AbstractCard.CardColor.COLORLESS ||
                __instance.color == AbstractCard.CardColor.CURSE) {

            String cardbackType = __instance.color == AbstractCard.CardColor.COLORLESS ?
                    Spirepass.CARDBACK_COLORLESS : Spirepass.CARDBACK_CURSE;

            String cardbackId = Spirepass.getAppliedCardback(cardbackType);

            if (cardbackId != null && !cardbackId.isEmpty()) {
                try {
                    // Get the path for the cardback texture
                    String texturePath = getCardbackTexturePath(cardbackType, cardbackId);
                    if (texturePath == null) {
                        return SpireReturn.Continue();
                    }

                    // Get or load the texture
                    Texture cardbackTexture = getOrLoadTexture(texturePath);
                    if (cardbackTexture == null) {
                        return SpireReturn.Continue();
                    }

                    // Create a temporary AtlasRegion from our texture
                    TextureAtlas.AtlasRegion cardbackRegion = new TextureAtlas.AtlasRegion(
                            cardbackTexture, 0, 0, cardbackTexture.getWidth(), cardbackTexture.getHeight());

                    // Get the renderColor using reflection
                    Color renderColor = (Color)ReflectionHacks.getPrivate(__instance, AbstractCard.class, "renderColor");

                    // Render the custom cardback
                    // We need to call renderHelper which is a private method in AbstractCard
                    ReflectionHacks.privateMethod(AbstractCard.class, "renderHelper",
                                    SpriteBatch.class, Color.class, TextureAtlas.AtlasRegion.class,
                                    float.class, float.class)
                            .invoke(__instance, sb, renderColor, cardbackRegion, x, y);

                    return SpireReturn.Return();
                } catch (Exception e) {
                    logError(e, cardbackType, cardbackId);
                }
            }
        }

        return SpireReturn.Continue();
    }

    private static String getCardbackTexturePath(String cardbackType, String cardbackId) {
        if (cardbackType.equals(Spirepass.CARDBACK_COLORLESS)) {
            if (cardbackId.equals("COLORLESS_CYAN")) {
                return "spirepass/images/rewards/cardbacks/colorless/Cyan.png";
            }
            // Add other colorless cardbacks here
        } else if (cardbackType.equals(Spirepass.CARDBACK_CURSE)) {
            if (cardbackId.equals("CURSE_HAROLD")) {
                return "spirepass/images/rewards/cardbacks/curse/Harold.png";
            }
            // Add other curse cardbacks here
        }

        return null; // Unknown format or default cardback
    }

    private static Texture getOrLoadTexture(String path) {
        if (!cardbackTextureCache.containsKey(path)) {
            try {
                FileHandle fileHandle = Gdx.files.internal(path);
                if (fileHandle.exists()) {
                    cardbackTextureCache.put(path, new Texture(fileHandle));
                } else {
                    return null;
                }
            } catch (Exception e) {
                Spirepass.logger.error("Failed to load cardback texture: " + path);
                e.printStackTrace();
                return null;
            }
        }
        return cardbackTextureCache.get(path);
    }

    private static void logError(Exception e, String cardbackType, String cardbackId) {
        Spirepass.logger.error("ERROR APPLYING CARDBACK " + cardbackId + " TO " + cardbackType + ": " + e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Spirepass.logger.error(sw.toString());
    }
}