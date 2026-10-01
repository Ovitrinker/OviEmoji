package ch.ovitrinker.oviemoji.chat;

import ch.ovitrinker.oviemoji.emoji.Emoji;
import ch.ovitrinker.oviemoji.emoji.EmojiText;
import ch.ovitrinker.oviemoji.emoji.Emojis;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/**
 * Inserts emojis into Minecraft texts.
 *
 * <p>An emoji is drawn as a character from the Unicode private use area in the font
 * {@code oviemoji:emoji}. This font is a bitmap font whose characters are the coloured Twemoji
 * images. Because an emoji is thus a completely normal character, line wrapping, chat fading,
 * click and hover events work like with any other text.
 *
 * <p>The conversion only happens when displaying. The shortcode is always what gets sent.
 */
public final class EmojiComponents {

    /** The font with the emoji images, {@code assets/oviemoji/font/emoji.json}. */
    public static final FontDescription FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("oviemoji", "emoji"));

    private static final Style PLAIN_EMOJI = emojiStyle(Style.EMPTY);

    private EmojiComponents() {
    }

    /**
     * Returns a single emoji as text, e.g. for the picker.
     *
     * @param emoji the emoji
     * @return a text of exactly one character
     */
    public static MutableComponent glyph(Emoji emoji) {
        return Component.literal(String.valueOf(emoji.glyph())).setStyle(PLAIN_EMOJI);
    }

    /**
     * Replaces shortcodes and Unicode emojis in a text with emoji images.
     *
     * <p>If the text contains no emoji, exactly the same object is returned. Otherwise the text is
     * split into flat sections that each keep their full style, including colour, click and hover
     * events.
     *
     * @param text the text
     * @return the text with emojis
     */
    public static Component transform(Component text) {
        if (text == null || !EmojiText.containsEmoji(Emojis.index(), text.getString())) {
            return text;
        }
        MutableComponent out = Component.empty();
        text.visit((style, content) -> {
            for (EmojiText.Piece piece : EmojiText.parse(Emojis.index(), content)) {
                if (piece.emoji() != null) {
                    out.append(Component.literal(String.valueOf(piece.emoji().glyph()))
                            .setStyle(emojiStyle(style)));
                } else {
                    out.append(Component.literal(piece.text()).setStyle(style));
                }
            }
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }

    /**
     * Derives an emoji's style from the style of the surrounding text.
     *
     * <p>The colour is set to white, because Minecraft multiplies the text colour with the image:
     * an emoji in grey chat text or on a sign with black writing would otherwise be tinted. Shadow
     * is turned off, as are bold and italic, because they would draw the image twice or slanted.
     * Click and hover events are kept.
     */
    private static Style emojiStyle(Style base) {
        return base.withFont(FONT)
                .withColor(0xFFFFFF)
                .withShadowColor(0)
                .withBold(false)
                .withItalic(false)
                .withObfuscated(false);
    }
}
