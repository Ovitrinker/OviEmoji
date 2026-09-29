package ch.ovitrinker.oviemoji.mixin;

import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Gibt das Textfeld des Schild-Bildschirms frei.
 *
 * <p>Ein Accessor statt eines {@code @Shadow}, weil das Feld ab 26.3 {@code final} ist und davor
 * nicht.
 */
@Mixin(AbstractSignEditScreen.class)
public interface SignEditScreenAccessor {

    @Accessor("signField")
    TextFieldHelper oviemoji$signField();
}
