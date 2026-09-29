package ch.ovitrinker.oviemoji.emoji;

import java.util.ArrayList;
import java.util.List;

/**
 * Zerlegt Text in gewoehnliche Abschnitte und Emojis.
 *
 * <p>Erkannt werden Kurzcodes wie {@code :smile:} und echte Unicode-Emojis. Ein unbekannter
 * Kurzcode bleibt als Text stehen, damit etwa eine Uhrzeit wie {@code 12:30:15} unberuehrt bleibt.
 */
public final class EmojiText {

    /**
     * Ein Stueck des zerlegten Texts: genau eines der beiden Felder ist gesetzt.
     *
     * @param text  gewoehnlicher Text, oder {@code null}
     * @param emoji ein Emoji, oder {@code null}
     */
    public record Piece(String text, Emoji emoji) {
    }

    private EmojiText() {
    }

    /**
     * Zerlegt einen Text.
     *
     * @param index der Emoji-Index
     * @param input der Text
     * @return die Stuecke in Reihenfolge; ohne Emoji genau ein Textstueck mit dem Originaltext
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
            // Nichts gefunden: den Originaltext zurueckgeben, nicht die Fassung ohne Hauttoene
            pieces.set(0, new Piece(input, null));
        }
        return pieces;
    }

    /**
     * Prueft, ob ein Text mindestens ein Emoji enthaelt.
     *
     * @param index der Emoji-Index
     * @param input der Text
     * @return {@code true}, wenn {@link #parse} ein Emoji liefern wuerde
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
     * Ersetzt Unicode-Emojis durch ihre Kurzcodes.
     *
     * <p>So kommt eine Nachricht bei Spielern ohne Mod als lesbares {@code :smile:} an statt als
     * leeres Kaestchen. Kurzcodes und gewoehnlicher Text bleiben unveraendert.
     *
     * @param index der Emoji-Index
     * @param input der Text
     * @return der Text mit Kurzcodes
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

    /** Ein ueberzaehliges U+FE0F nach einem Treffer wuerde sonst als leeres Kaestchen gezeichnet. */
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

    /** Von den ASCII-Zeichen beginnen nur die Tastenkappen wie 1-FE0F-20E3 ein Emoji. */
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
     * Entfernt die Hautton-Zeichen U+1F3FB bis U+1F3FF.
     *
     * <p>Die Mod fuehrt jedes Emoji nur in der gelben Grundform. Ein Emoji mit Hautton wird so zur
     * Grundform, statt zu einem Bild mit einem leeren Kaestchen dahinter.
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
