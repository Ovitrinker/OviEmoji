package ch.ovitrinker.oviemoji.mixin;

import ch.ovitrinker.oviemoji.chat.EmojiComponents;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Setzt Emojis in jede Zeile ein, die im Chat landet: Spielernachrichten, Systemmeldungen,
 * Befehlsausgaben.
 *
 * <p>Alle Wege in den Chat enden in einer einzigen {@code addMessage}-Methode. Deren Signatur hat
 * sich geaendert: in 1.21.11 ist sie oeffentlich mit {@code (Component, MessageSignature,
 * GuiMessageTag)}, ab 26.2 privat mit einer zusaetzlichen {@code GuiMessageSource}. Die Nachricht
 * wird hier ersetzt, bevor sie gespeichert und umgebrochen wird; darum stimmen Zeilenumbrueche
 * auch nach dem Aendern der Chatbreite.
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
