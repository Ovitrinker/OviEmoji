package ch.ovitrinker.oviemoji.emoji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Haelt den einen Emoji-Index der Mod.
 *
 * <p>Die Liste wird direkt aus dem Mod-Jar gelesen und nicht ueber den Ressourcen-Manager, weil
 * sie keine Ressource ist, die ein Ressourcenpaket ersetzen soll: die Zuordnung Kurzname zu
 * Zeichen muss zur Schrift im selben Jar passen.
 */
public final class Emojis {

    private static final Logger LOGGER = LoggerFactory.getLogger("oviemoji");
    private static final String PATH = "/assets/oviemoji/emoji.tsv";

    private static EmojiIndex index = new EmojiIndex(List.of());

    private Emojis() {
    }

    /** Laedt die Emoji-Liste. Bei einem Fehler bleibt der Index leer und die Mod tut nichts. */
    public static void load() {
        try (InputStream in = Emojis.class.getResourceAsStream(PATH)) {
            if (in == null) {
                LOGGER.error("{} fehlt im Mod-Jar", PATH);
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            index = new EmojiIndex(reader.lines().toList());
            LOGGER.info("{} Emojis geladen", index.all().size());
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Emoji-Liste konnte nicht gelesen werden", e);
        }
    }

    /** Gibt den Index zurueck, vor {@link #load()} ist er leer. */
    public static EmojiIndex index() {
        return index;
    }
}
