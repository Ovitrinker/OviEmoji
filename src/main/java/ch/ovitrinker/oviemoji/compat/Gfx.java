package ch.ovitrinker.oviemoji.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/**
 * Eine schmale Huelle um das Zeichenziel, weil sich dessen Klasse und einige Methodennamen
 * zwischen den Zielversionen unterscheiden.
 *
 * <p>Bis 1.21.11 heisst die Klasse {@code GuiGraphics} und die Textmethode {@code drawString}.
 * Ab 26.1 laeuft das Zeichnen in zwei Schritten; das Ziel heisst {@code GuiGraphicsExtractor} und
 * die Textmethode {@code text}. {@code fill}, {@code pose}, {@code nextStratum} und
 * {@code setTooltipForNextFrame} sind in allen Zielversionen gleich.
 *
 * <p>In den von Stonecutter umgeschalteten Bloecken stehen bewusst keine Kommentare: ein
 * {@code *}{@code /} darin wuerde den Kommentar zerreissen, mit dem Stonecutter den jeweils
 * nicht benutzten Zweig stilllegt.
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

    /** Fuellt ein Rechteck, Farbe als 0xAARRGGBB. */
    public void fill(int left, int top, int right, int bottom, int argb) {
        target.fill(left, top, right, bottom, argb);
    }

    /** Zeichnet einen Rahmen von einem Pixel Breite, Farbe als 0xAARRGGBB. */
    public void frame(int left, int top, int right, int bottom, int argb) {
        target.fill(left, top, right, top + 1, argb);
        target.fill(left, bottom - 1, right, bottom, argb);
        target.fill(left, top + 1, left + 1, bottom - 1, argb);
        target.fill(right - 1, top + 1, right, bottom - 1, argb);
    }

    /** Zeichnet einen Text mit Schatten, Farbe als 0xAARRGGBB. */
    public void text(Font font, Component text, int x, int y, int argb) {
        //? if <26.1 {
        target.drawString(font, text, x, y, argb);
        //?} else {
        /*target.text(font, text, x, y, argb);
        *///?}
    }

    /**
     * Zeichnet einen Text vergroessert, fuer die Emojis im Auswahlfenster.
     *
     * @param font  die Schrift
     * @param text  der Text
     * @param x     linke Kante
     * @param y     obere Kante
     * @param scale der Vergroesserungsfaktor
     */
    public void scaledText(Font font, Component text, float x, float y, float scale) {
        target.pose().pushMatrix();
        target.pose().translate(x, y);
        target.pose().scale(scale, scale);
        text(font, text, 0, 0, 0xFFFFFFFF);
        target.pose().popMatrix();
    }

    /** Alles danach liegt ueber dem bisher Gezeichneten, auch ueber Text. */
    public void nextStratum() {
        target.nextStratum();
    }

    /** Zeigt einen Tooltip an der Mausposition. */
    public void tooltip(Font font, Component text, int x, int y) {
        target.setTooltipForNextFrame(font, text, x, y);
    }
}
