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
 * Holds the mod's single emoji index.
 *
 * <p>The list is read directly from the mod jar and not via the resource manager, because it isn't
 * a resource a resource pack should replace: the mapping from short name to character has to match
 * the font in the same jar.
 */
public final class Emojis {

    private static final Logger LOGGER = LoggerFactory.getLogger("oviemoji");
    private static final String PATH = "/assets/oviemoji/emoji.tsv";

    private static EmojiIndex index = new EmojiIndex(List.of());

    private Emojis() {
    }

    /** Loads the emoji list. On error the index stays empty and the mod does nothing. */
    public static void load() {
        try (InputStream in = Emojis.class.getResourceAsStream(PATH)) {
            if (in == null) {
                LOGGER.error("{} is missing from the mod jar", PATH);
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            index = new EmojiIndex(reader.lines().toList());
            LOGGER.info("{} emojis loaded", index.all().size());
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not read the emoji list", e);
        }
    }

    /** Returns the index; it is empty before {@link #load()}. */
    public static EmojiIndex index() {
        return index;
    }
}
