package ch.ovitrinker.oviemoji.gui;

import ch.ovitrinker.oviemoji.chat.EmojiComponents;
import ch.ovitrinker.oviemoji.compat.Gfx;
import ch.ovitrinker.oviemoji.config.RecentEmojis;
import ch.ovitrinker.oviemoji.emoji.Emoji;
import ch.ovitrinker.oviemoji.emoji.EmojiIndex;
import ch.ovitrinker.oviemoji.emoji.Emojis;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/**
 * Der Emoji-Knopf unten rechts und das Auswahlfenster darueber.
 *
 * <p>Der Chat und der Schild-Bildschirm benutzen dieselbe Klasse. Was ein Klick auf ein Emoji
 * bewirkt, entscheidet der Bildschirm ueber den mitgegebenen {@code onPick}. Die Lage haengt nur
 * von der Bildschirmgroesse ab: der Knopf sitzt knapp ueber dem Chat-Eingabefeld, auf dem
 * Schild-Bildschirm ist diese Ecke frei.
 */
public final class EmojiPicker {

    private static final int COLUMNS = 10;
    private static final int ROWS = 7;
    private static final int CELL = 16;
    private static final int PAD = 4;
    private static final float GLYPH_SCALE = 1.5f;
    private static final int PANEL_WIDTH = PAD * 2 + COLUMNS * CELL;
    private static final int GRID_TOP = PAD + CELL + 3;
    private static final int FOOTER_TOP = GRID_TOP + ROWS * CELL + 3;
    private static final int PANEL_HEIGHT = FOOTER_TOP + 9 + PAD;

    private static final int BUTTON_WIDTH = 14;
    private static final int BUTTON_HEIGHT = 13;

    /** Reiter: zuerst die zuletzt benutzten, dann die Kategorien. */
    private static final List<String> TABS = new ArrayList<>();
    /** Das Symbol pro Reiter, als Kurzname. */
    private static final List<String> TAB_ICONS = List.of(
            "clock3", "smiley", "wave", "dog", "hamburger", "car", "soccer", "bulb", "heart", "checkered_flag");

    static {
        TABS.add("recent");
        TABS.addAll(EmojiIndex.CATEGORIES);
    }

    /** Ueberdauert das Schliessen des Bildschirms, damit das Fenster beim naechsten Mal gleich aussieht. */
    private static int lastTab = 1;

    private final Screen screen;
    private final Consumer<Emoji> onPick;

    private boolean open;
    private int tab = lastTab;
    private int scrollRow;

    /**
     * @param screen der Bildschirm, auf dem Knopf und Fenster liegen
     * @param onPick wird mit dem angeklickten Emoji aufgerufen
     */
    public EmojiPicker(Screen screen, Consumer<Emoji> onPick) {
        this.screen = screen;
        this.onPick = onPick;
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private static boolean enabled() {
        return !Emojis.index().all().isEmpty();
    }

    // ------------------------------------------------------------------ Lage der Elemente

    private int buttonX() {
        return screen.width - BUTTON_WIDTH - 2;
    }

    private int buttonY() {
        return screen.height - 14 - BUTTON_HEIGHT - 1;
    }

    private int panelX() {
        return screen.width - 2 - PANEL_WIDTH;
    }

    private int panelY() {
        return Math.max(2, buttonY() - 2 - PANEL_HEIGHT);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private boolean overButton(double mx, double my) {
        return inside(mx, my, buttonX(), buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    private boolean overPanel(double mx, double my) {
        return open && inside(mx, my, panelX(), panelY(), PANEL_WIDTH, PANEL_HEIGHT);
    }

    private List<Emoji> tabEmojis() {
        String key = TABS.get(tab);
        return key.equals("recent") ? RecentEmojis.list() : Emojis.index().category(key);
    }

    private int maxScroll() {
        int rows = (tabEmojis().size() + COLUMNS - 1) / COLUMNS;
        return Math.max(0, rows - ROWS);
    }

    /** Das Emoji unter der Maus im Raster, oder {@code null}. */
    private Emoji gridEmojiAt(double mx, double my) {
        int gx = panelX() + PAD;
        int gy = panelY() + GRID_TOP;
        if (!inside(mx, my, gx, gy, COLUMNS * CELL, ROWS * CELL)) {
            return null;
        }
        int col = (int) ((mx - gx) / CELL);
        int row = (int) ((my - gy) / CELL);
        int i = (scrollRow + row) * COLUMNS + col;
        List<Emoji> list = tabEmojis();
        return i < list.size() ? list.get(i) : null;
    }

    /** Der Reiter unter der Maus, oder -1. */
    private int tabAt(double mx, double my) {
        int tx = panelX() + PAD;
        int ty = panelY() + PAD;
        if (!inside(mx, my, tx, ty, TABS.size() * CELL, CELL)) {
            return -1;
        }
        return (int) ((mx - tx) / CELL);
    }

    // ------------------------------------------------------------------ Zeichnen

    /**
     * Zeichnet Knopf und, falls offen, das Auswahlfenster. Ruft vorher {@code nextStratum} auf,
     * damit beides ueber dem Text des Bildschirms liegt.
     *
     * @param g  das Zeichenziel
     * @param mx Mausposition x
     * @param my Mausposition y
     */
    public void render(Gfx g, int mx, int my) {
        if (!enabled()) {
            return;
        }
        Font font = font();

        boolean hoverButton = overButton(mx, my);
        int bx = buttonX();
        int by = buttonY();
        g.fill(bx, by, bx + BUTTON_WIDTH, by + BUTTON_HEIGHT,
                open || hoverButton ? 0xC0505050 : 0x80000000);
        Emoji buttonIcon = Emojis.index().byAlias("slightly_smiling_face");
        if (buttonIcon != null) {
            g.text(font, EmojiComponents.glyph(buttonIcon), bx + 3, by + 3, 0xFFFFFFFF);
        }

        if (open) {
            renderPanel(g, font, mx, my);
        }
        if (hoverButton && !open) {
            g.tooltip(font, Component.translatable("oviemoji.picker.button"), mx, my);
        }
    }

    private void renderPanel(Gfx g, Font font, int mx, int my) {
        int px = panelX();
        int py = panelY();
        g.fill(px, py, px + PANEL_WIDTH, py + PANEL_HEIGHT, 0xE8141414);
        g.frame(px, py, px + PANEL_WIDTH, py + PANEL_HEIGHT, 0xFF505050);

        // Reiter
        int hoveredTab = tabAt(mx, my);
        for (int i = 0; i < TABS.size(); i++) {
            int x = px + PAD + i * CELL;
            int y = py + PAD;
            if (i == tab) {
                g.fill(x, y, x + CELL, y + CELL, 0x70FFFFFF);
            } else if (i == hoveredTab) {
                g.fill(x, y, x + CELL, y + CELL, 0x30FFFFFF);
            }
            Emoji icon = Emojis.index().byAlias(TAB_ICONS.get(i));
            if (icon != null) {
                g.scaledText(font, EmojiComponents.glyph(icon), x + 2, y + 2, GLYPH_SCALE);
            }
        }
        g.fill(px + PAD, py + GRID_TOP - 2, px + PANEL_WIDTH - PAD, py + GRID_TOP - 1, 0xFF505050);

        // Raster
        List<Emoji> list = tabEmojis();
        scrollRow = Math.max(0, Math.min(scrollRow, maxScroll()));
        Emoji hovered = gridEmojiAt(mx, my);
        int gx = px + PAD;
        int gy = py + GRID_TOP;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int i = (scrollRow + row) * COLUMNS + col;
                if (i >= list.size()) {
                    break;
                }
                Emoji emoji = list.get(i);
                int x = gx + col * CELL;
                int y = gy + row * CELL;
                if (emoji == hovered) {
                    g.fill(x, y, x + CELL, y + CELL, 0x40FFFFFF);
                }
                g.scaledText(font, EmojiComponents.glyph(emoji), x + 2, y + 2, GLYPH_SCALE);
            }
        }
        if (list.isEmpty()) {
            g.text(font, Component.translatable("oviemoji.picker.empty"), gx + 2, gy + 4, 0xFF909090);
        }

        // Bildlaufleiste
        int max = maxScroll();
        if (max > 0) {
            int trackHeight = ROWS * CELL;
            int thumbHeight = Math.max(8, trackHeight * ROWS / (ROWS + max));
            int thumbTop = gy + (trackHeight - thumbHeight) * scrollRow / max;
            int sx = px + PANEL_WIDTH - 3;
            g.fill(sx, gy, sx + 2, gy + trackHeight, 0x40FFFFFF);
            g.fill(sx, thumbTop, sx + 2, thumbTop + thumbHeight, 0xC0FFFFFF);
        }

        // Fusszeile: Kurzcode des Emojis unter der Maus, sonst der Name des Reiters
        Component footer;
        if (hovered != null) {
            footer = Component.literal(hovered.shortcode());
        } else if (hoveredTab >= 0) {
            footer = Component.translatable("oviemoji.category." + TABS.get(hoveredTab));
        } else {
            footer = Component.translatable("oviemoji.category." + TABS.get(tab));
        }
        g.text(font, footer, px + PAD + 1, py + FOOTER_TOP, hovered != null ? 0xFFFFFF55 : 0xFFA0A0A0);
    }

    // ------------------------------------------------------------------ Eingabe

    /**
     * Nimmt einen Mausklick entgegen.
     *
     * @return {@code true}, wenn der Klick hier verbraucht wurde
     */
    public boolean mouseClicked(double mx, double my, int button) {
        if (!enabled()) {
            return false;
        }
        // Die Nummer der linken Maustaste ist nicht fest: 0 bis 26.2 (GLFW), 1 ab 26.3 (SDL3)
        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return overPanel(mx, my) || overButton(mx, my);
        }
        if (overButton(mx, my)) {
            open = !open;
            return true;
        }
        if (overPanel(mx, my)) {
            int clickedTab = tabAt(mx, my);
            if (clickedTab >= 0 && clickedTab < TABS.size()) {
                tab = clickedTab;
                lastTab = clickedTab;
                scrollRow = 0;
                return true;
            }
            Emoji emoji = gridEmojiAt(mx, my);
            if (emoji != null) {
                onPick.accept(emoji);
                RecentEmojis.add(emoji);
            }
            return true;
        }
        return false;
    }

    /**
     * Nimmt das Mausrad entgegen.
     *
     * @return {@code true}, wenn das Rad hier verbraucht wurde
     */
    public boolean mouseScrolled(double mx, double my, double amount) {
        if (overPanel(mx, my)) {
            if (amount != 0) {
                scrollRow = Math.max(0, Math.min(maxScroll(), scrollRow - (int) Math.signum(amount)));
            }
            return true;
        }
        return false;
    }

    /**
     * Escape schliesst zuerst das offene Fenster und erst beim zweiten Mal den Bildschirm.
     *
     * @return {@code true}, wenn die Taste hier verbraucht wurde
     */
    public boolean keyPressed(KeyEvent event) {
        if (open && event.isEscape()) {
            open = false;
            return true;
        }
        return false;
    }
}
