package ch.ovitrinker.oviemoji.mixin;

import ch.ovitrinker.oviemoji.chat.EmojiComponents;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Inserts emojis into every line that ends up in chat: player messages, system messages, command
 * output.
 *
 * <p>All paths into the chat end in a single {@code addMessage} method. Its signature has changed:
 * in 1.21.11 it is public with {@code (Component, MessageSignature, GuiMessageTag)}, from 26.2 on
 * private with an additional {@code GuiMessageSource}. The message is replaced here before it is
 * stored and wrapped; that's why line breaks are still correct after changing the chat width.
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    //? if <26.1 {
    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"), argsOnly = true)
    //?} else {
    /*@ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"), argsOnly = true)
    *///?}
    private Component oviemoji$insertEmojis(Component message) {
        return EmojiComponents.transform(message);
    }
}
