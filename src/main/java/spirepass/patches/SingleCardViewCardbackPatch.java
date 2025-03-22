package spirepass.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;
import spirepass.Spirepass;
import spirepass.spirepassutil.SkinManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

@SpirePatch(
        clz = SingleCardViewPopup.class,
        method = "getCardBackAtlasRegion"
)
public class SingleCardViewCardbackPatch {
    // Cache textures to avoid reloading
    private static HashMap<String, TextureAtlas.AtlasRegion> cardbackRegionCache = new HashMap<>();

    @SpirePostfixPatch
    public static TextureAtlas.AtlasRegion Postfix(TextureAtlas.AtlasRegion __result, SingleCardViewPopup __instance) {
        // Get the card from the SingleCardViewPopup instance
        AbstractCard card = (AbstractCard) ReflectionHacks.getPrivate(__instance, SingleCardViewPopup.class, "card");

        // Only apply for colorless (excluding status) and curse cards
        if ((card.color == AbstractCard.CardColor.COLORLESS && card.type != AbstractCard.CardType.STATUS) ||
                card.color == AbstractCard.CardColor.CURSE) {
            String cardbackType = card.color == AbstractCard.CardColor.COLORLESS ?
                    SkinManager.CARDBACK_COLORLESS : SkinManager.CARDBACK_CURSE;
            String cardbackId = SkinManager.getInstance().getAppliedCardback(cardbackType);
            if (cardbackId != null && !cardbackId.isEmpty()) {
                try {
                    // Get the path for the large cardback texture
                    String texturePath = getLargeCardbackTexturePath(cardbackType, cardbackId, card);
                    if (texturePath == null) {
                        return __result;
                    }
                    // Get or create the atlas region
                    TextureAtlas.AtlasRegion region = getOrCreateAtlasRegion(texturePath);
                    if (region != null) {
                        return region;
                    }
                } catch (Exception e) {
                    logError(e, cardbackType, cardbackId);
                }
            }
        }
        return __result;
    }

    private static String getLargeCardbackTexturePath(String cardbackType, String cardbackId, AbstractCard card) {
        if (cardbackType.equals(SkinManager.CARDBACK_COLORLESS)) {
            String cardType = "";

            // Determine card type
            if (card.type == AbstractCard.CardType.ATTACK) {
                cardType = "Attack";
            } else if (card.type == AbstractCard.CardType.SKILL) {
                cardType = "Skill";
            } else if (card.type == AbstractCard.CardType.POWER) {
                cardType = "Power";
            } else {
                // Use Skill as default for STATUS and other types
                cardType = "Skill";
            }

            if (cardbackId.equals("COLORLESS_SPONSORED")) {
                return "spirepass/images/rewards/cardbacks/colorless/sponsored/RAID" + cardType + "Large.png";
            }

            if (cardbackId.equals("COLORLESS_FAVORITECUSTOMER")) {
                return "spirepass/images/rewards/cardbacks/colorless/favoritecustomer/FavoriteCustomer" + cardType + "Large.png";
            }

            if (cardbackId.equals("COLORLESS_BLISSFUL")) {
                return "spirepass/images/rewards/cardbacks/colorless/blissful/Bliss" + cardType + "Large.png";
            }

            if (cardbackId.equals("COLORLESS_JIMBO")) {
                return "spirepass/images/rewards/cardbacks/colorless/jimbo/Jimbo" + cardType + "Large.png";
            }
            // Add other colorless cardbacks here
        } else if (cardbackType.equals(SkinManager.CARDBACK_CURSE)) {
            if (cardbackId.equals("CURSE_HAROLD")) {
                return "spirepass/images/rewards/cardbacks/curse/HaroldLarge.png";
            }

            if (cardbackId.equals("CURSE_NOTSTONKS")) {
                return "spirepass/images/rewards/cardbacks/curse/NotStonksLarge.png";
            }
            // Add other curse cardbacks here
        }
        return null; // Unknown format or default cardback
    }

    private static TextureAtlas.AtlasRegion getOrCreateAtlasRegion(String path) {
        if (!cardbackRegionCache.containsKey(path)) {
            try {
                FileHandle fileHandle = Gdx.files.internal(path);
                if (fileHandle.exists()) {
                    Texture texture = new Texture(fileHandle);
                    // Create a new AtlasRegion with the full texture size
                    TextureAtlas.AtlasRegion region = new TextureAtlas.AtlasRegion(
                            texture, 0, 0, texture.getWidth(), texture.getHeight());
                    cardbackRegionCache.put(path, region);
                } else {
                    Spirepass.logger.error("Large cardback texture not found: " + path);
                    return null;
                }
            } catch (Exception e) {
                Spirepass.logger.error("Failed to load large cardback texture: " + path);
                e.printStackTrace();
                return null;
            }
        }
        return cardbackRegionCache.get(path);
    }

    private static void logError(Exception e, String cardbackType, String cardbackId) {
        Spirepass.logger.error("ERROR APPLYING LARGE CARDBACK " + cardbackId + " TO " + cardbackType + ": " + e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Spirepass.logger.error(sw.toString());
    }
}