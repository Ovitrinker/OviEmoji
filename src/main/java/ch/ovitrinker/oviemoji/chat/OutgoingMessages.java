package ch.ovitrinker.oviemoji.chat;

import ch.ovitrinker.oviemoji.emoji.EmojiText;
import ch.ovitrinker.oviemoji.emoji.Emojis;
import java.util.Locale;
import java.util.Set;

/**
 * Bereitet eine Chatnachricht vor dem Senden auf.
 *
 * <p>Eingefuegte Unicode-Emojis werden zu Kurzcodes. Spieler ohne Mod sehen dann {@code :smile:}
 * statt eines leeren Kaestchens, Spieler mit Mod sehen in beiden Faellen das Bild. Die Umwandlung
 * passiert vor dem Signieren, die Nachricht bleibt also gueltig signiert.
 */
public final class OutgoingMessages {

    /**
     * Befehle, deren Text als Chatnachricht bei anderen Spielern ankommt. Bei allen anderen
     * Befehlen bleibt die Eingabe unangetastet, damit etwa ein Item-Name mit Emoji so ankommt,
     * wie er getippt wurde.
     */
    private static final Set<String> MESSAGE_COMMANDS =
            Set.of("msg", "tell", "w", "me", "say", "teammsg", "tm");

    private OutgoingMessages() {
    }

    /**
     * Wandelt Unicode-Emojis in einer Eingabe in Kurzcodes um.
     *
     * @param input die Eingabe aus dem Chatfeld
     * @return die Eingabe, wie sie gesendet werden soll
     */
    public static String prepare(String input) {
        if (input.startsWith("/")) {
            int space = input.indexOf(' ');
            String command = (space < 0 ? input.substring(1) : input.substring(1, space))
                    .toLowerCase(Locale.ROOT);
            if (!MESSAGE_COMMANDS.contains(command)) {
                return input;
            }
        }
        return EmojiText.toShortcodes(Emojis.index(), input);
    }
}
