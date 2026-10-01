package ch.ovitrinker.oviemoji.emoji;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into ordinary sections and emojis.
 *
 * <p>Shortcodes like {@code :smile:} and real Unicode emojis are recognised. An unknown shortcode
 * stays as text, so that e.g. a time like {@code 12:30:15} stays untouched.
 */
public final class EmojiText {

    /**
     * A piece of the split text: exactly one of the two fields is set.
     *
     * @param text  ordinary text, or {@code null}
     * @param emoji an emoji, or {@code null}
     */
    public record Piece(String text, Emoji emoji) {
    }

    private EmojiText() {
    }

    /**
     * Splits a text.
     *
     * @param index the emoji index
     * @param input the text
     * @return the pieces in order; without an emoji exactly one text piece with the original text
     */
    public static List<Piece> parse(EmojiIndex index, String input) {
        List<Piece> pieces = new ArrayList<>();
        if (input.indexOf(':') < 0 && !hasNonAscii(input)) {
            pieces.add(new Piece(input, null));
            return pieces;
        }
        String text = stripSkinTones(input);
        StringBuilder plain = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);

            if (c == ':') {
                int end = text.indexOf(':', i + 1);
                if (end > i + 1 && isShortcodeName(text, i + 1, end)) {
                    Emoji emoji = index.byAlias(text.substring(i + 1, end));
                    if (emoji != null) {
                        flush(pieces, plain);
                        pieces.add(new Piece(null, emoji));
                        i = end + 1;
                        continue;
                    }
                }
            } else if (mayStartUnicodeEmoji(c)) {
                int len = index.unicodeMatchLength(text, i);
                if (len > 0) {
                    flush(pieces, plain);
                    pieces.add(new Piece(null, index.byUnicode(text.substring(i, i + len))));
                    i = skipVariationSelector(text, i + len);
                    continue;
                }
            }
            plain.append(c);
            i++;
        }
        flush(pieces, plain);
        if (pieces.size() == 1 && pieces.get(0).emoji() == null) {
            // Nothing found: return the original text, not the version without skin tones
            pieces.set(0, new Piece(input, null));
        }
        return pieces;
    }

    /**
     * Checks whether a text contains at least one emoji.
     *
     * @param index the emoji index
     * @param input the text
     * @return {@code true} if {@link #parse} would return an emoji
     */
    public static boolean containsEmoji(EmojiIndex index, String input) {
        for (Piece piece : parse(index, input)) {
            if (piece.emoji() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces Unicode emojis with their shortcodes.
     *
     * <p>That way a message arrives at players without the mod as a readable {@code :smile:}
     * instead of an empty box. Shortcodes and ordinary text stay unchanged.
     *
     * @param index the emoji index
     * @param input the text
     * @return the text with shortcodes
     */
    public static String toShortcodes(EmojiIndex index, String input) {
        if (!hasNonAscii(input)) {
            return input;
        }
        String text = stripSkinTones(input);
        StringBuilder out = new StringBuilder();
        boolean changed = false;
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (mayStartUnicodeEmoji(c)) {
                int len = index.unicodeMatchLength(text, i);
                if (len > 0) {
                    out.append(index.byUnicode(text.substring(i, i + len)).shortcode());
                    i = skipVariationSelector(text, i + len);
                    changed = true;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return changed ? out.toString() : input;
    }

    /** A surplus U+FE0F after a match would otherwise be drawn as an empty box. */
    private static int skipVariationSelector(String text, int i) {
        return i < text.length() && text.charAt(i) == '️' ? i + 1 : i;
    }

    private static boolean hasNonAscii(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 0x7F) {
                return true;
            }
        }
        return false;
    }

    /** Of the ASCII characters, only keycaps like 1-FE0F-20E3 start an emoji. */
    private static boolean mayStartUnicodeEmoji(char c) {
        return c > 0x7F || c == '#' || c == '*' || (c >= '0' && c <= '9');
    }

    private static boolean isShortcodeName(String text, int from, int to) {
        for (int i = from; i < to; i++) {
            char c = text.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || c == '_' || c == '+' || c == '-';
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the skin tone characters U+1F3FB to U+1F3FF.
     *
     * <p>The mod only has each emoji in its yellow base form. That way an emoji with a skin tone
     * becomes the base form instead of an image followed by an empty box.
     */
    static String stripSkinTones(String text) {
        if (text.indexOf('\uD83C') < 0) {
            return text;
        }
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\uD83C' && i + 1 < text.length()) {
                char low = text.charAt(i + 1);
                if (low >= '\uDFFB' && low <= '\uDFFF') {
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private static void flush(List<Piece> pieces, StringBuilder plain) {
        if (!plain.isEmpty()) {
            pieces.add(new Piece(plain.toString(), null));
            plain.setLength(0);
        }
    }
}
