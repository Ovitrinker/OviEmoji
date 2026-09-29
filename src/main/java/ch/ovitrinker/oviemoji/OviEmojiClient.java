package ch.ovitrinker.oviemoji;

import ch.ovitrinker.oviemoji.config.RecentEmojis;
import ch.ovitrinker.oviemoji.emoji.Emojis;
import net.fabricmc.api.ClientModInitializer;

/**
 * Der Einstiegspunkt der Mod auf der Client-Seite.
 *
 * <p>OviEmoji ist rein clientseitig. Verschickt werden nur gewoehnliche Kurzcodes wie
 * {@code :smile:}; erst beim Anzeigen macht die Mod daraus ein Bild. Ein Server braucht die Mod
 * nicht, und Spieler ohne Mod lesen den Kurzcode als Text.
 */
public class OviEmojiClient implements ClientModInitializer {

    /** Die Mod-ID, wie sie auch in der {@code fabric.mod.json} steht. */
    public static final String MOD_ID = "oviemoji";

    @Override
    public void onInitializeClient() {
        Emojis.load();
        RecentEmojis.load();
    }
}
