package ch.ovitrinker.oviemoji;

import ch.ovitrinker.oviemoji.config.RecentEmojis;
import ch.ovitrinker.oviemoji.emoji.Emojis;
import net.fabricmc.api.ClientModInitializer;

/**
 * The mod's client-side entry point.
 *
 * <p>OviEmoji is purely client-side. Only ordinary shortcodes like {@code :smile:} are sent; the
 * mod turns them into an image only when displaying them. A server doesn't need the mod, and
 * players without the mod read the shortcode as text.
 */
public class OviEmojiClient implements ClientModInitializer {

    /** The mod ID, as also listed in {@code fabric.mod.json}. */
    public static final String MOD_ID = "oviemoji";

    @Override
    public void onInitializeClient() {
        Emojis.load();
        RecentEmojis.load();
    }
}
