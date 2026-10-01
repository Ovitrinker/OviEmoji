package ch.ovitrinker.oviemoji.mixin;

import ch.ovitrinker.oviemoji.chat.EmojiChatUi;
import ch.ovitrinker.oviemoji.chat.OutgoingMessages;
import ch.ovitrinker.oviemoji.compat.Gfx;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Attaches emoji button, picker and suggestion list to the chat screen and converts Unicode emojis
 * to shortcodes before sending.
 *
 * <p>{@code init}, {@code onEdited}, {@code keyPressed(KeyEvent)},
 * {@code mouseClicked(MouseButtonEvent, boolean)}, {@code mouseScrolled} and
 * {@code handleChatInput} are the same in all target versions. Only drawing is called
 * {@code render(GuiGraphics, ...)} up to 1.21.11 and {@code extractRenderState(GuiGraphicsExtractor, ...)} from 26.1 on.
 */
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow
    protected EditBox input;

    @Unique
    private EmojiChatUi oviemoji$ui;

    @Inject(method = "init", at = @At("TAIL"))
    private void oviemoji$init(CallbackInfo ci) {
        if (oviemoji$ui == null) {
            oviemoji$ui = new EmojiChatUi((Screen) (Object) this, input);
        } else {
            oviemoji$ui.rebind(input);
        }
    }

    @Inject(method = "onEdited", at = @At("TAIL"))
    private void oviemoji$onEdited(String value, CallbackInfo ci) {
        if (oviemoji$ui != null) {
            oviemoji$ui.onEdited();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void oviemoji$keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (oviemoji$ui != null && oviemoji$ui.keyPressed(event)) {
            cir.setReturnValue(Boolean.TRUE);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void oviemoji$mouseClicked(MouseButtonEvent event, boolean doubled,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (oviemoji$ui != null && oviemoji$ui.mouseClicked(event.x(), event.y(), event.button())) {
            cir.setReturnValue(Boolean.TRUE);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void oviemoji$mouseScrolled(double x, double y, double scrollX, double scrollY,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (oviemoji$ui != null && oviemoji$ui.mouseScrolled(x, y, scrollY)) {
            cir.setReturnValue(Boolean.TRUE);
        }
    }

    //? if <26.1 {
    @Inject(method = "render", at = @At("TAIL"))
    private void oviemoji$render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY,
                                 float partialTick, CallbackInfo ci) {
        if (oviemoji$ui != null) {
            oviemoji$ui.render(new Gfx(graphics), mouseX, mouseY);
        }
    }
    //?} else {
    /*@Inject(method = "extractRenderState", at = @At("TAIL"))
    private void oviemoji$render(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                 float partialTick, CallbackInfo ci) {
        if (oviemoji$ui != null) {
            oviemoji$ui.render(new Gfx(graphics), mouseX, mouseY);
        }
    }
    *///?}

    @ModifyVariable(method = "handleChatInput", at = @At("HEAD"), argsOnly = true)
    private String oviemoji$prepareOutgoing(String message) {
        return OutgoingMessages.prepare(message);
    }
}
