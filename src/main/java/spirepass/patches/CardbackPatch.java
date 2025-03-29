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
import spirepass.spirepassutil.SkinManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

@SpirePatch(
        clz = AbstractCard.class,
        method = "renderCardBg"
)
public class CardbackPatch {
    // ==================== TEXTURE CACHING ====================

    private static HashMap<String, Texture> cardbackTextureCache = new HashMap<>();

    // ==================== PATCH METHODS ====================

    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(AbstractCard __instance, SpriteBatch sb, float x, float y) {
        // Only apply for colorless (excluding status) and curse cards
        if ((__instance.color == AbstractCard.CardColor.COLORLESS && __instance.type != AbstractCard.CardType.STATUS) ||
                __instance.color == AbstractCard.CardColor.CURSE) {

            String cardbackType = __instance.color == AbstractCard.CardColor.COLORLESS ?
                    SkinManager.CARDBACK_COLORLESS : SkinManager.CARDBACK_CURSE;
            String cardbackId = SkinManager.getInstance().getAppliedCardback(cardbackType);

            if (cardbackId != null && !cardbackId.isEmpty()) {
                try {
                    String texturePath = getCardbackTexturePath(cardbackType, cardbackId, __instance);
                    if (texturePath == null) {
                        return SpireReturn.Continue();
                    }

                    Texture cardbackTexture = getOrLoadTexture(texturePath);
                    if (cardbackTexture == null) {
                        return SpireReturn.Continue();
                    }

                    TextureAtlas.AtlasRegion cardbackRegion = new TextureAtlas.AtlasRegion(
                            cardbackTexture, 0, 0, cardbackTexture.getWidth(), cardbackTexture.getHeight());

                    Color renderColor = (Color)ReflectionHacks.getPrivate(__instance, AbstractCard.class, "renderColor");

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

    // ==================== HELPER METHODS ====================

    private static String getCardbackTexturePath(String cardbackType, String cardbackId, AbstractCard card) {
        if (cardbackType.equals(SkinManager.CARDBACK_COLORLESS)) {
            String cardType = "";

            if (card.type == AbstractCard.CardType.ATTACK) {
                cardType = "Attack";
            } else if (card.type == AbstractCard.CardType.SKILL) {
                cardType = "Skill";
            } else if (card.type == AbstractCard.CardType.POWER) {
                cardType = "Power";
            } else {
                cardType = "Skill"; // Default for STATUS and other types
            }

            if (cardbackId.equals("COLORLESS_SPONSORED")) {
                return "spirepass/images/rewards/cardbacks/colorless/sponsored/RAID" + cardType + ".png";
            }

            if (cardbackId.equals("COLORLESS_FAVORITECUSTOMER")) {
                return "spirepass/images/rewards/cardbacks/colorless/favoritecustomer/FavoriteCustomer" + cardType + ".png";
            }

            if (cardbackId.equals("COLORLESS_BLISSFUL")) {
                return "spirepass/images/rewards/cardbacks/colorless/blissful/Bliss" + cardType + ".png";
            }

            if (cardbackId.equals("COLORLESS_JIMBO")) {
                return "spirepass/images/rewards/cardbacks/colorless/jimbo/Jimbo" + cardType + ".png";
            }
        } else if (cardbackType.equals(SkinManager.CARDBACK_CURSE)) {
            if (cardbackId.equals("CURSE_HAROLD")) {
                return "spirepass/images/rewards/cardbacks/curse/Harold.png";
            }

            if (cardbackId.equals("CURSE_NOTSTONKS")) {
                return "spirepass/images/rewards/cardbacks/curse/NotStonks.png";
            }
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
                e.printStackTrace();
                return null;
            }
        }
        return cardbackTextureCache.get(path);
    }

    private static void logError(Exception e, String cardbackType, String cardbackId) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
    }
}