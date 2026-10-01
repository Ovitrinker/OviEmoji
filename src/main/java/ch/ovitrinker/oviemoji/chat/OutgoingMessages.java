package ch.ovitrinker.oviemoji.chat;

import ch.ovitrinker.oviemoji.emoji.EmojiText;
import ch.ovitrinker.oviemoji.emoji.Emojis;
import java.util.Locale;
import java.util.Set;

/**
 * Prepares a chat message before sending.
 *
 * <p>Pasted Unicode emojis become shortcodes. Players without the mod then see {@code :smile:}
 * instead of an empty box; players with the mod see the image in both cases. The conversion
 * happens before signing, so the message stays validly signed.
 */
public final class OutgoingMessages {

    /**
     * Commands whose text arrives at other players as a chat message. For all other commands the
     * input stays untouched, so that e.g. an item name with an emoji arrives exactly as typed.
     */
    private static final Set<String> MESSAGE_COMMANDS =
            Set.of("msg", "tell", "w", "me", "say", "teammsg", "tm");

    private OutgoingMessages() {
    }

    /**
     * Converts Unicode emojis in an input into shortcodes.
     *
     * @param input the input from the chat field
     * @return the input as it should be sent
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
