package ch.ovitrinker.oviemoji.chat;

import ch.ovitrinker.oviemoji.emoji.Emoji;
import ch.ovitrinker.oviemoji.emoji.EmojiText;
import ch.ovitrinker.oviemoji.emoji.Emojis;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/**
 * Setzt Emojis in Minecraft-Texte ein.
 *
 * <p>Ein Emoji wird als ein Zeichen aus dem privaten Unicode-Bereich in der Schrift
 * {@code oviemoji:emoji} gezeichnet. Diese Schrift ist eine Bitmap-Schrift, deren Zeichen die
 * farbigen Twemoji-Bilder sind. Weil ein Emoji damit ein ganz normales Zeichen ist, funktionieren
 * Zeilenumbruch, Ausblenden des Chats, Klick- und Hover-Ereignisse wie bei jedem anderen Text.
 *
 * <p>Die Umwandlung geschieht nur beim Anzeigen. Verschickt wird immer der Kurzcode.
 */
public final class EmojiComponents {

    /** Die Schrift mit den Emoji-Bildern, {@code assets/oviemoji/font/emoji.json}. */
    public static final FontDescription FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("oviemoji", "emoji"));

    private static final Style PLAIN_EMOJI = emojiStyle(Style.EMPTY);

    private EmojiComponents() {
    }

    /**
     * Gibt ein einzelnes Emoji als Text zurueck, etwa fuer das Auswahlfenster.
     *
     * @param emoji das Emoji
     * @return ein Text aus genau einem Zeichen
     */
    public static MutableComponent glyph(Emoji emoji) {
        return Component.literal(String.valueOf(emoji.glyph())).setStyle(PLAIN_EMOJI);
    }

    /**
     * Ersetzt Kurzcodes und Unicode-Emojis in einem Text durch Emoji-Bilder.
     *
     * <p>Enthaelt der Text kein Emoji, kommt genau dasselbe Objekt zurueck. Andernfalls wird der
     * Text in flache Abschnitte zerlegt, die jeweils ihren vollstaendigen Stil behalten, also auch
     * Farbe, Klick- und Hover-Ereignisse.
     *
     * @param text der Text
     * @return der Text mit Emojis
     */
    public static Component transform(Component text) {
        if (text == null || !EmojiText.containsEmoji(Emojis.index(), text.getString())) {
            return text;
        }
        MutableComponent out = Component.empty();
        text.visit((style, content) -> {
            for (EmojiText.Piece piece : EmojiText.parse(Emojis.index(), content)) {
                if (piece.emoji() != null) {
                    out.append(Component.literal(String.valueOf(piece.emoji().glyph()))
                            .setStyle(emojiStyle(style)));
                } else {
                    out.append(Component.literal(piece.text()).setStyle(style));
                }
            }
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }

    /**
     * Leitet den Stil eines Emojis aus dem Stil des umgebenden Texts ab.
     *
     * <p>Die Farbe wird auf Weiss gesetzt, weil Minecraft die Textfarbe mit dem Bild multipliziert:
     * ein Emoji in grauem Chattext oder auf einem schwarz beschrifteten Schild waere sonst
     * eingefaerbt. Der Schatten wird abgeschaltet, fett und kursiv ebenso, weil beides das Bild
     * doppelt beziehungsweise schraeg zeichnen wuerde. Klick- und Hover-Ereignisse bleiben.
     */
    private static Style emojiStyle(Style base) {
        return base.withFont(FONT)
                .withColor(0xFFFFFF)
                .withShadowColor(0)
                .withBold(false)
                .withItalic(false)
                .withObfuscated(false);
    }
}
