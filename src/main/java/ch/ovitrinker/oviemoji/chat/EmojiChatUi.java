package ch.ovitrinker.oviemoji.chat;

import ch.ovitrinker.oviemoji.compat.Gfx;
import ch.ovitrinker.oviemoji.config.RecentEmojis;
import ch.ovitrinker.oviemoji.emoji.EmojiIndex;
import ch.ovitrinker.oviemoji.emoji.Emojis;
import ch.ovitrinker.oviemoji.gui.EmojiPicker;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/**
 * The emoji part of the chat screen: the picker ({@link EmojiPicker}) and the suggestion list
 * while typing {@code :name}.
 *
 * <p>Both only insert shortcodes like {@code :smile:} into the input field. What they become in
 * chat is decided by the receiver alone: an image with the mod, the readable shortcode without.
 */
public final class EmojiChatUi {

    private static final int MAX_SUGGESTIONS = 8;
    private static final int SUGGESTION_HEIGHT = 12;

    private final Screen screen;
    private final EmojiPicker picker;
    private EditBox input;

    private List<EmojiIndex.Suggestion> suggestions = List.of();
    private int selected;
    private int tokenStart = -1;
    /** After Escape the suggestions stay hidden until the input changes. */
    private String dismissedFor;

    /**
     * @param screen the chat screen
     * @param input  its input field
     */
    public EmojiChatUi(Screen screen, EditBox input) {
        this.screen = screen;
        this.input = input;
        this.picker = new EmojiPicker(screen, emoji -> this.input.insertText(emoji.shortcode()));
    }

    /**
     * Takes over the new input field the chat creates on every resize. The picker stays open.
     */
    public void rebind(EditBox input) {
        this.input = input;
        updateSuggestions();
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int suggestionBoxWidth() {
        int widest = 0;
        for (EmojiIndex.Suggestion s : suggestions) {
            widest = Math.max(widest, font().width(":" + s.alias() + ":"));
        }
        return widest + 18;
    }

    private int suggestionBoxX() {
        String value = input.getValue();
        int x = 4 + font().width(value.substring(0, Math.min(tokenStart, value.length())));
        return Math.max(2, Math.min(x, screen.width - suggestionBoxWidth() - 2));
    }

    private int suggestionBoxY() {
        return screen.height - 14 - suggestions.size() * SUGGESTION_HEIGHT - 2;
    }

    // ------------------------------------------------------------------ Drawing

    /**
     * Draws button, picker and suggestions over the chat.
     *
     * @param g  the draw target
     * @param mx mouse position x
     * @param my mouse position y
     */
    public void render(Gfx g, int mx, int my) {
        g.nextStratum();
        picker.render(g, mx, my);
        if (!suggestions.isEmpty()) {
            renderSuggestions(g, font(), mx, my);
        }
    }

    private void renderSuggestions(Gfx g, Font font, int mx, int my) {
        int x = suggestionBoxX();
        int y = suggestionBoxY();
        int w = suggestionBoxWidth();
        int h = suggestions.size() * SUGGESTION_HEIGHT + 2;
        g.fill(x, y, x + w, y + h, 0xE0000000);
        for (int i = 0; i < suggestions.size(); i++) {
            EmojiIndex.Suggestion s = suggestions.get(i);
            int rowY = y + 1 + i * SUGGESTION_HEIGHT;
            boolean hot = i == selected || inside(mx, my, x, rowY, w, SUGGESTION_HEIGHT);
            if (i == selected) {
                g.fill(x, rowY, x + w, rowY + SUGGESTION_HEIGHT, 0x40FFFFFF);
            }
            g.text(font, EmojiComponents.glyph(s.emoji()), x + 3, rowY + 2, 0xFFFFFFFF);
            g.text(font, Component.literal(":" + s.alias() + ":"), x + 15, rowY + 2,
                    hot ? 0xFFFFFF55 : 0xFFA8A8A8);
        }
    }

    // ------------------------------------------------------------------ Input

    /**
     * Handles a mouse click.
     *
     * @return {@code true} if the click was consumed here
     */
    public boolean mouseClicked(double mx, double my, int button) {
        if (!suggestions.isEmpty()) {
            int x = suggestionBoxX();
            int y = suggestionBoxY() + 1;
            if (inside(mx, my, x, y, suggestionBoxWidth(), suggestions.size() * SUGGESTION_HEIGHT)) {
                accept((int) ((my - y) / SUGGESTION_HEIGHT));
                return true;
            }
        }
        return picker.mouseClicked(mx, my, button);
    }

    /**
     * Handles the mouse wheel.
     *
     * @return {@code true} if the scroll was consumed here
     */
    public boolean mouseScrolled(double mx, double my, double amount) {
        return picker.mouseScrolled(mx, my, amount);
    }

    /**
     * Handles a key press before the chat sees it.
     *
     * <p>While suggestions are open, arrow up/down, Tab, Enter and Escape belong to the suggestion
     * list. So Enter inserts the suggestion and doesn't send anything yet. If only the picker is
     * open, Escape closes the picker first and the chat only on the second press.
     *
     * @return {@code true} if the key was consumed here
     */
    public boolean keyPressed(KeyEvent event) {
        if (!suggestions.isEmpty()) {
            if (event.isUp()) {
                selected = (selected - 1 + suggestions.size()) % suggestions.size();
                return true;
            }
            if (event.isDown()) {
                selected = (selected + 1) % suggestions.size();
                return true;
            }
            if (event.isCycleFocus() || event.isConfirmation()) {
                accept(selected);
                return true;
            }
            if (event.isEscape()) {
                dismissedFor = input.getValue();
                suggestions = List.of();
                return true;
            }
        }
        return picker.keyPressed(event);
    }

    /** Called after every change in the input field. */
    public void onEdited() {
        updateSuggestions();
    }

    /**
     * Looks for a started shortcode before the cursor.
     *
     * <p>It has to be at the start or after a space and have at least two characters, so that a
     * time like {@code 12:30} doesn't open any suggestions. In commands the list stays off;
     * Minecraft's own command completion takes priority there.
     */
    private void updateSuggestions() {
        String value = input.getValue();
        if (dismissedFor != null && !dismissedFor.equals(value)) {
            dismissedFor = null;
        }
        suggestions = List.of();
        tokenStart = -1;
        if (value.startsWith("/") || dismissedFor != null) {
            return;
        }
        int cursor = Math.min(input.getCursorPosition(), value.length());
        int start = cursor;
        while (start > 0 && isNameChar(value.charAt(start - 1))) {
            start--;
        }
        int colon = start - 1;
        if (colon < 0 || value.charAt(colon) != ':' || cursor - start < 2) {
            return;
        }
        if (colon > 0 && !Character.isWhitespace(value.charAt(colon - 1))) {
            return;
        }
        suggestions = Emojis.index().search(value.substring(start, cursor), MAX_SUGGESTIONS);
        tokenStart = colon;
        selected = 0;
    }

    private static boolean isNameChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || c == '_' || c == '+' || c == '-';
    }

    /** Replaces the started shortcode with the selected suggestion. */
    private void accept(int index) {
        if (index < 0 || index >= suggestions.size() || tokenStart < 0) {
            return;
        }
        EmojiIndex.Suggestion s = suggestions.get(index);
        String value = input.getValue();
        int cursor = Math.min(input.getCursorPosition(), value.length());
        String code = ":" + s.alias() + ": ";
        String updated = value.substring(0, tokenStart) + code + value.substring(cursor);
        int newCursor = tokenStart + code.length();
        suggestions = List.of();
        input.setValue(updated);
        input.moveCursorTo(Math.min(newCursor, input.getValue().length()), false);
        RecentEmojis.add(s.emoji());
    }
}
