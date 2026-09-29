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
 * Setzt den Emoji-Knopf samt Auswahlfenster auf den Bildschirm zum Beschriften von Schildern und
 * Haengeschildern.
 *
 * <p>Eine Schildzeile ist nur etwa 90 Pixel breit. Darum wird der kuerzeste Kurzname eingefuegt,
 * also {@code :+1:} statt {@code :thumbsup:}. Passt auch der nicht mehr, laesst Minecrafts
 * Breitenpruefung die Eingabe fallen und die Zeile bleibt, wie sie war.
 *
 * <p>{@code AbstractSignEditScreen} ueberschreibt {@code mouseClicked} und {@code mouseScrolled}
 * nicht, deshalb erweitert dieses Mixin {@link Screen} und ueberschreibt beide selbst.
 * {@code keyPressed} und das Zeichnen gibt es in der Klasse und werden per {@code @Inject}
 * ergaenzt; das Zeichnen heisst bis 1.21.11 {@code render}, ab 26.1 {@code extractRenderState}.
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
