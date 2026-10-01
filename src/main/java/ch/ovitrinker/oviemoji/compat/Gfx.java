package ch.ovitrinker.oviemoji.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/**
 * A thin wrapper around the draw target, because its class and some method names differ between
 * the target versions.
 *
 * <p>Up to 1.21.11 the class is called {@code GuiGraphics} and the text method {@code drawString}.
 * From 26.1 on drawing happens in two steps; the target is called {@code GuiGraphicsExtractor} and
 * the text method {@code text}. {@code fill}, {@code pose}, {@code nextStratum} and
 * {@code setTooltipForNextFrame} are the same in all target versions.
 *
 * <p>The blocks switched by Stonecutter deliberately contain no comments: a {@code *}{@code /}
 * inside them would break the comment Stonecutter uses to disable the unused branch.
 */
public final class Gfx {

    //? if <26.1 {
    public final net.minecraft.client.gui.GuiGraphics target;

    public Gfx(net.minecraft.client.gui.GuiGraphics target) {
        this.target = target;
    }
    //?} else {
    /*public final net.minecraft.client.gui.GuiGraphicsExtractor target;

    public Gfx(net.minecraft.client.gui.GuiGraphicsExtractor target) {
        this.target = target;
    }
    *///?}

    /** Fills a rectangle, colour as 0xAARRGGBB. */
    public void fill(int left, int top, int right, int bottom, int argb) {
        target.fill(left, top, right, bottom, argb);
    }

    /** Draws a one-pixel-wide border, colour as 0xAARRGGBB. */
    public void frame(int left, int top, int right, int bottom, int argb) {
        target.fill(left, top, right, top + 1, argb);
        target.fill(left, bottom - 1, right, bottom, argb);
        target.fill(left, top + 1, left + 1, bottom - 1, argb);
        target.fill(right - 1, top + 1, right, bottom - 1, argb);
    }

    /** Draws text with a shadow, colour as 0xAARRGGBB. */
    public void text(Font font, Component text, int x, int y, int argb) {
        //? if <26.1 {
        target.drawString(font, text, x, y, argb);
        //?} else {
        /*target.text(font, text, x, y, argb);
        *///?}
    }

    /**
     * Draws scaled-up text, for the emojis in the picker.
     *
     * @param font  the font
     * @param text  the text
     * @param x     left edge
     * @param y     top edge
     * @param scale the scale factor
     */
    public void scaledText(Font font, Component text, float x, float y, float scale) {
        target.pose().pushMatrix();
        target.pose().translate(x, y);
        target.pose().scale(scale, scale);
        text(font, text, 0, 0, 0xFFFFFFFF);
        target.pose().popMatrix();
    }

    /** Everything after this lies above what has been drawn so far, including text. */
    public void nextStratum() {
        target.nextStratum();
    }

    /** Shows a tooltip at the mouse position. */
    public void tooltip(Font font, Component text, int x, int y) {
        target.setTooltipForNextFrame(font, text, x, y);
    }
}
