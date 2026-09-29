package ch.ovitrinker.oviemoji.emoji;

import java.util.List;

/**
 * Ein einzelnes Emoji, wie es in {@code assets/oviemoji/emoji.tsv} steht.
 *
 * @param glyph    das Zeichen aus dem privaten Unicode-Bereich, unter dem die Schrift der Mod das
 *                 Bild fuehrt. Es wird nie verschickt, nur lokal gezeichnet.
 * @param unicode  das echte Unicode-Emoji
 * @param category der Kurzname der Kategorie, etwa {@code smileys}
 * @param aliases  die Kurznamen ohne Doppelpunkte, der erste ist der bevorzugte
 * @param tags     zusaetzliche Stichwoerter fuer die Suche
 */
public record Emoji(char glyph, String unicode, String category, List<String> aliases, List<String> tags) {

    /** Gibt den bevorzugten Kurznamen zurueck. */
    public String name() {
        return aliases.get(0);
    }

    /** Gibt den bevorzugten Kurzcode samt Doppelpunkten zurueck, etwa {@code :smile:}. */
    public String shortcode() {
        return ":" + aliases.get(0) + ":";
    }
}
