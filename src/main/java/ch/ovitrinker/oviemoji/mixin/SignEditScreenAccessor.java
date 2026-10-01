package ch.ovitrinker.oviemoji.mixin;

import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the text field of the sign screen.
 *
 * <p>An accessor instead of {@code @Shadow}, because the field is {@code final} from 26.3 on and
 * not before.
 */
@Mixin(AbstractSignEditScreen.class)
public interface SignEditScreenAccessor {

    @Accessor("signField")
    TextFieldHelper oviemoji$signField();
}
