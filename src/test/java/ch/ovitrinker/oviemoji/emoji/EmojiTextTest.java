package ch.ovitrinker.oviemoji.emoji;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Tests splitting, conversion and search against the real {@code emoji.tsv}. */
class EmojiTextTest {

    private static EmojiIndex index;

    @BeforeAll
    static void load() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                EmojiTextTest.class.getResourceAsStream("/assets/oviemoji/emoji.tsv"),
                StandardCharsets.UTF_8))) {
            index = new EmojiIndex(reader.lines().toList());
        }
    }

    /** Writes the pieces compactly: text as is, emojis as [shortname]. */
    private static String render(String input) {
        return EmojiText.parse(index, input).stream()
                .map(p -> p.emoji() != null ? "[" + p.emoji().name() + "]" : p.text())
                .collect(Collectors.joining());
    }

    @Test
    void indexContainsAllEntries() {
        assertEquals(1870, index.all().size());
        assertEquals('', index.all().get(0).glyph());
    }

    @Test
    void shortcodes() {
        assertEquals("hallo [smile] du", render("hallo :smile: du"));
        assertEquals("[+1][+1]", render(":+1::thumbsup:"));
        assertEquals("[smile]", render(":SMILE:"));
    }

    @Test
    void unknownShortcodesAndTimesStayText() {
        assertEquals("um 12:30:15 Uhr", render("um 12:30:15 Uhr"));
        assertEquals(":gibtsnicht: [smile]", render(":gibtsnicht: :smile:"));
        assertEquals("a: b :smile", render("a: b :smile"));
        assertEquals("::", render("::"));
    }

    @Test
    void unicodeEmojis() {
        assertEquals("[grinning]!", render("😀!"));
        assertEquals("[switzerland]", render("🇨🇭"));
        assertEquals("[technologist]", render("🧑‍💻"));
    }

    @Test
    void skinTonesFallBackToBaseEmoji() {
        assertEquals("[+1]", render("👍🏽"));
        assertEquals("[man_technologist]", render("👨🏿‍💻"));
    }

    @Test
    void textStyleCharactersNeedVariationSelector() {
        // Without U+FE0F the copyright sign and heart stay ordinary text
        assertEquals("© 2026", render("© 2026"));
        assertEquals("[copyright]", render("©️"));
        assertEquals("[heart]", render("❤️"));
    }

    @Test
    void keycapsButNotPlainDigits() {
        assertEquals("[one]", render("1️⃣"));
        assertEquals("[one]", render("1⃣"));
        assertEquals("123", render("123"));
    }

    @Test
    void plainTextIsReturnedUnchanged() {
        List<EmojiText.Piece> pieces = EmojiText.parse(index, "Grüezi mitenand");
        assertEquals(1, pieces.size());
        assertEquals("Grüezi mitenand", pieces.get(0).text());
        assertNull(pieces.get(0).emoji());
        assertFalse(EmojiText.containsEmoji(index, "Grüezi"));
        assertTrue(EmojiText.containsEmoji(index, "Grüezi :wave:"));
    }

    @Test
    void unicodeToShortcodes() {
        assertEquals("Hallo :grinning:", EmojiText.toShortcodes(index, "Hallo 😀"));
        assertEquals(":+1: ok", EmojiText.toShortcodes(index, "👍🏻 ok"));
        assertEquals(":heart:", EmojiText.toShortcodes(index, "❤️"));
        assertEquals("schon :smile:", EmojiText.toShortcodes(index, "schon :smile:"));
        assertEquals("Grüezi", EmojiText.toShortcodes(index, "Grüezi"));
    }

    @Test
    void searchRanksPrefixAndShortNamesFirst() {
        List<EmojiIndex.Suggestion> hits = index.search("thu", 5);
        assertEquals("thumbsup", hits.get(0).alias());
        assertEquals("thumbsdown", hits.get(1).alias());

        assertEquals("smile", index.search("smi", 3).get(0).alias());
        assertEquals("fire", index.search("fire", 1).get(0).alias());
        assertTrue(index.search("", 5).isEmpty());
        assertEquals(8, index.search("a", 8).size());
    }

    @Test
    void searchFallsBackToTags() {
        // "happy" isn't a short name, but a keyword of :grinning:
        List<EmojiIndex.Suggestion> hits = index.search("happy", 20);
        assertTrue(hits.stream().anyMatch(h -> h.emoji().name().equals("grinning")));
    }

    @Test
    void categoriesAreComplete() {
        int total = 0;
        for (String category : EmojiIndex.CATEGORIES) {
            assertFalse(index.category(category).isEmpty(), category);
            total += index.category(category).size();
        }
        assertEquals(index.all().size(), total);
    }
}
