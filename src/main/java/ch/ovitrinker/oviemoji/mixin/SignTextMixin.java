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
 * Shows emojis on signs and hanging signs.
 *
 * <p>The sign renderer fetches the lines via {@code SignText.getRenderMessages}, passing the
 * function that turns a line into drawable characters. That function is wrapped here. Minecraft
 * caches the result in the {@code SignText}, so the conversion runs once per sign and not every
 * frame.
 *
 * <p>The shortcode is still what is stored and sent to the server. That's why the editing screen
 * shows it as text.
 */
@Mixin(SignText.class)
public abstract class SignTextMixin {

    @ModifyVariable(method = "getRenderMessages", at = @At("HEAD"), argsOnly = true)
    private Function<Component, FormattedCharSequence> oviemoji$insertEmojis(
            Function<Component, FormattedCharSequence> formatter) {
        return line -> formatter.apply(EmojiComponents.transform(line));
    }
}
