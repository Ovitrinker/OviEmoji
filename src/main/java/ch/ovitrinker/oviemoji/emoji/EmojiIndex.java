package ch.ovitrinker.oviemoji.emoji;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * All of the mod's emojis with the lookups that chat, picker and autocomplete need.
 *
 * <p>The class doesn't know any Minecraft classes and can therefore be tested with plain JUnit.
 */
public final class EmojiIndex {

    /** The categories in the order of the tabs in the picker. */
    public static final List<String> CATEGORIES = List.of(
            "smileys", "people", "nature", "food", "travel", "activities", "objects", "symbols", "flags");

    private static final String VARIATION_SELECTOR = "️";

    private final List<Emoji> all;
    private final Map<String, Emoji> byAlias = new HashMap<>();
    private final Map<String, Emoji> byUnicode = new HashMap<>();
    private final Map<String, List<Emoji>> byCategory = new LinkedHashMap<>();
    private final int longestUnicode;

    /**
     * Builds the index from the lines of the TSV file.
     *
     * @param lines the lines; empty lines and lines starting with {@code #} are skipped
     */
    public EmojiIndex(List<String> lines) {
        List<Emoji> list = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] cols = line.split("\t", -1);
            char glyph = (char) Integer.parseInt(cols[0], 16);
            StringBuilder unicode = new StringBuilder();
            for (String hex : cols[1].split(" ")) {
                unicode.appendCodePoint(Integer.parseInt(hex, 16));
            }
            List<String> aliases = List.of(cols[3].split(","));
            List<String> tags = cols[4].isEmpty() ? List.of() : List.of(cols[4].split(","));
            list.add(new Emoji(glyph, unicode.toString(), cols[2], aliases, tags));
        }
        this.all = Collections.unmodifiableList(list);

        for (String category : CATEGORIES) {
            byCategory.put(category, new ArrayList<>());
        }
        int longest = 0;
        for (Emoji emoji : all) {
            for (String alias : emoji.aliases()) {
                byAlias.putIfAbsent(alias, emoji);
            }
            byCategory.computeIfAbsent(emoji.category(), k -> new ArrayList<>()).add(emoji);

            // The form with U+FE0F always counts. Without it, an emoji is only recognised if it
            // consists of several characters: a single character like the copyright sign or an
            // arrow is ordinary text without U+FE0F and must not become an image.
            byUnicode.putIfAbsent(emoji.unicode(), emoji);
            String bare = emoji.unicode().replace(VARIATION_SELECTOR, "");
            if (!bare.equals(emoji.unicode()) && bare.codePointCount(0, bare.length()) >= 2) {
                byUnicode.putIfAbsent(bare, emoji);
            }
            longest = Math.max(longest, emoji.unicode().length());
        }
        this.longestUnicode = longest;
    }

    /** Returns all emojis in file order. */
    public List<Emoji> all() {
        return all;
    }

    /** Returns the emojis of a category, or an empty list. */
    public List<Emoji> category(String category) {
        return byCategory.getOrDefault(category, List.of());
    }

    /**
     * Looks up an emoji by its short name, case-insensitively.
     *
     * @param alias the short name without colons
     * @return the emoji, or {@code null}
     */
    public Emoji byAlias(String alias) {
        return byAlias.get(alias.toLowerCase(Locale.ROOT));
    }

    /**
     * Finds the longest Unicode emoji starting at a position.
     *
     * @param text  the text, skin tone characters must already be removed
     * @param start the start position
     * @return the length of the match in {@code char}s, or 0
     */
    int unicodeMatchLength(String text, int start) {
        int max = Math.min(longestUnicode, text.length() - start);
        for (int len = max; len >= 1; len--) {
            if (byUnicode.containsKey(text.substring(start, start + len))) {
                return len;
            }
        }
        return 0;
    }

    /** Returns the emoji for exactly this Unicode sequence, or {@code null}. */
    Emoji byUnicode(String unicode) {
        return byUnicode.get(unicode);
    }

    /**
     * An autocomplete suggestion.
     *
     * @param emoji the emoji
     * @param alias the short name that matched the input
     */
    public record Suggestion(Emoji emoji, String alias) {
    }

    /**
     * Finds emojis for a started short name.
     *
     * <p>Ranking: exact short name, short name starts with the input, a word in the short name
     * starts with it, the short name contains it, a keyword starts with it. Within a rank, short
     * names come before long ones.
     *
     * @param query the input without colon
     * @param limit the maximum number of suggestions
     * @return the suggestions, best first
     */
    public List<Suggestion> search(String query, int limit) {
        String q = query.toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            return List.of();
        }
        record Hit(Suggestion suggestion, int rank, int order) {
        }
        List<Hit> hits = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            Emoji emoji = all.get(i);
            int bestRank = Integer.MAX_VALUE;
            String bestAlias = null;
            for (String alias : emoji.aliases()) {
                int rank;
                if (alias.equals(q)) {
                    rank = 0;
                } else if (alias.startsWith(q)) {
                    rank = 1;
                } else if (alias.contains("_" + q)) {
                    rank = 2;
                } else if (alias.contains(q)) {
                    rank = 3;
                } else {
                    continue;
                }
                if (rank < bestRank || (rank == bestRank && alias.length() < bestAlias.length())) {
                    bestRank = rank;
                    bestAlias = alias;
                }
            }
            if (bestAlias == null) {
                for (String tag : emoji.tags()) {
                    if (tag.startsWith(q)) {
                        bestRank = 4;
                        bestAlias = emoji.name();
                        break;
                    }
                }
            }
            if (bestAlias != null) {
                hits.add(new Hit(new Suggestion(emoji, bestAlias), bestRank, i));
            }
        }
        hits.sort(Comparator.comparingInt(Hit::rank)
                .thenComparingInt(h -> h.suggestion().alias().length())
                .thenComparingInt(Hit::order));
        return hits.stream().limit(limit).map(Hit::suggestion).toList();
    }
}
