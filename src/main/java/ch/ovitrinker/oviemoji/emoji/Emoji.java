package ch.ovitrinker.oviemoji.emoji;

import java.util.List;

/**
 * A single emoji, as listed in {@code assets/oviemoji/emoji.tsv}.
 *
 * @param glyph    the character from the Unicode private use area under which the mod's font
 *                 holds the image. It is never sent, only drawn locally.
 * @param unicode  the real Unicode emoji
 * @param category the short name of the category, e.g. {@code smileys}
 * @param aliases  the short names without colons, the first one is the preferred one
 * @param tags     additional keywords for searching
 */
public record Emoji(char glyph, String unicode, String category, List<String> aliases, List<String> tags) {

    /** Returns the preferred short name. */
    public String name() {
        return aliases.get(0);
    }

    /** Returns the preferred shortcode including colons, e.g. {@code :smile:}. */
    public String shortcode() {
        return ":" + aliases.get(0) + ":";
    }
}
