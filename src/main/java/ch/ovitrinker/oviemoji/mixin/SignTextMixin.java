package ch.ovitrinker.oviemoji.mixin;

import ch.ovitrinker.oviemoji.chat.EmojiComponents;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Zeigt Emojis auf Schildern und Haengeschildern.
 *
 * <p>Der Schild-Renderer holt die Zeilen ueber {@code SignText.getRenderMessages} und uebergibt
 * dabei die Funktion, die eine Zeile in zeichenbare Zeichen umsetzt. Diese Funktion wird hier
 * umhuellt. Minecraft speichert das Ergebnis im {@code SignText} zwischen, die Umwandlung laeuft
 * also einmal pro Schild und nicht in jedem Bild.
 *
 * <p>Gespeichert und an den Server geschickt wird weiter der Kurzcode. Der Bearbeitungsbildschirm
 * zeigt ihn deshalb auch als Text.
 */
@Mixin(SignText.class)
public abstract class SignTextMixin {

    @ModifyVariable(method = "getRenderMessages", at = @At("HEAD"), argsOnly = true)
    private Function<Component, FormattedCharSequence> oviemoji$insertEmojis(
            Function<Component, FormattedCharSequence> formatter) {
        return line -> formatter.apply(EmojiComponents.transform(line));
    }
}
