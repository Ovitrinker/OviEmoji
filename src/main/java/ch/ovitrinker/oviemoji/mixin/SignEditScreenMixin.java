package ch.ovitrinker.oviemoji.mixin;

import ch.ovitrinker.oviemoji.compat.Gfx;
import ch.ovitrinker.oviemoji.emoji.Emoji;
import ch.ovitrinker.oviemoji.gui.EmojiPicker;
import java.util.Comparator;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Puts the emoji button and picker on the screen for writing on signs and hanging signs.
 *
 * <p>A sign line is only about 90 pixels wide. That's why the shortest short name is inserted,
 * i.e. {@code :+1:} instead of {@code :thumbsup:}. If even that doesn't fit, Minecraft's width
 * check drops the input and the line stays as it was.
 *
 * <p>{@code AbstractSignEditScreen} doesn't override {@code mouseClicked} and
 * {@code mouseScrolled}, so this mixin extends {@link Screen} and overrides both itself.
 * {@code keyPressed} and drawing exist in the class and are extended via {@code @Inject}; drawing
 * is called {@code render} up to 1.21.11 and {@code extractRenderState} from 26.1 on.
 */
@Mixin(AbstractSignEditScreen.class)
public abstract class SignEditScreenMixin extends Screen {

    @Unique
    private EmojiPicker oviemoji$picker;

    protected SignEditScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private void oviemoji$insert(Emoji emoji) {
        TextFieldHelper field = ((SignEditScreenAccessor) this).oviemoji$signField();
        String shortest = emoji.aliases().stream().min(Comparator.comparingInt(String::length)).orElseThrow();
        field.insertText(":" + shortest + ":");
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void oviemoji$init(CallbackInfo ci) {
        if (oviemoji$picker == null) {
            oviemoji$picker = new EmojiPicker(this, this::oviemoji$insert);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void oviemoji$keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (oviemoji$picker != null && oviemoji$picker.keyPressed(event)) {
            cir.setReturnValue(Boolean.TRUE);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (oviemoji$picker != null && oviemoji$picker.mouseClicked(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (oviemoji$picker != null && oviemoji$picker.mouseScrolled(x, y, scrollY)) {
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    //? if <26.1 {
    @Inject(method = "render", at = @At("TAIL"))
    private void oviemoji$render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY,
                                 float partialTick, CallbackInfo ci) {
        if (oviemoji$picker != null) {
            Gfx g = new Gfx(graphics);
            g.nextStratum();
            oviemoji$picker.render(g, mouseX, mouseY);
        }
    }
    //?} else {
    /*@Inject(method = "extractRenderState", at = @At("TAIL"))
    private void oviemoji$render(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                 float partialTick, CallbackInfo ci) {
        if (oviemoji$picker != null) {
            Gfx g = new Gfx(graphics);
            g.nextStratum();
            oviemoji$picker.render(g, mouseX, mouseY);
        }
    }
    *///?}
}
