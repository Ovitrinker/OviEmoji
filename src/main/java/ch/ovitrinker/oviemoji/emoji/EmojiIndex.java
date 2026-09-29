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
 * Alle Emojis der Mod mit den Nachschlagewegen, die Chat, Auswahlfenster und
 * Autovervollstaendigung brauchen.
 *
 * <p>Die Klasse kennt keine Minecraft-Klassen und laesst sich darum mit reinem JUnit pruefen.
 */
public final class EmojiIndex {

    /** Die Kategorien in der Reihenfolge der Reiter im Auswahlfenster. */
    public static final List<String> CATEGORIES = List.of(
            "smileys", "people", "nature", "food", "travel", "activities", "objects", "symbols", "flags");

    private static final String VARIATION_SELECTOR = "️";

    private final List<Emoji> all;
    private final Map<String, Emoji> byAlias = new HashMap<>();
    private final Map<String, Emoji> byUnicode = new HashMap<>();
    private final Map<String, List<Emoji>> byCategory = new LinkedHashMap<>();
    private final int longestUnicode;

    /**
     * Baut den Index aus den Zeilen der TSV-Datei.
     *
     * @param lines die Zeilen; leere Zeilen und Zeilen mit {@code #} werden uebersprungen
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

            // Die Form mit U+FE0F gilt immer. Ohne ihn wird ein Emoji nur erkannt, wenn es aus
            // mehreren Zeichen besteht: ein einzelnes Zeichen wie das Copyright-Zeichen oder ein
            // Pfeil ist ohne U+FE0F gewoehnlicher Text und soll nicht zum Bild werden.
            byUnicode.putIfAbsent(emoji.unicode(), emoji);
            String bare = emoji.unicode().replace(VARIATION_SELECTOR, "");
            if (!bare.equals(emoji.unicode()) && bare.codePointCount(0, bare.length()) >= 2) {
                byUnicode.putIfAbsent(bare, emoji);
            }
            longest = Math.max(longest, emoji.unicode().length());
        }
        this.longestUnicode = longest;
    }

    /** Gibt alle Emojis in der Reihenfolge der Datei zurueck. */
    public List<Emoji> all() {
        return all;
    }

    /** Gibt die Emojis einer Kategorie zurueck, oder eine leere Liste. */
    public List<Emoji> category(String category) {
        return byCategory.getOrDefault(category, List.of());
    }

    /**
     * Sucht ein Emoji ueber seinen Kurznamen, Gross- und Kleinschreibung egal.
     *
     * @param alias der Kurzname ohne Doppelpunkte
     * @return das Emoji, oder {@code null}
     */
    public Emoji byAlias(String alias) {
        return byAlias.get(alias.toLowerCase(Locale.ROOT));
    }

    /**
     * Sucht das laengste Unicode-Emoji, das an einer Stelle beginnt.
     *
     * @param text  der Text, Hautton-Zeichen muessen bereits entfernt sein
     * @param start die Startstelle
     * @return die Laenge des Treffers in {@code char}, oder 0
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

    /** Gibt das Emoji zu genau dieser Unicode-Zeichenfolge zurueck, oder {@code null}. */
    Emoji byUnicode(String unicode) {
        return byUnicode.get(unicode);
    }

    /**
     * Ein Vorschlag der Autovervollstaendigung.
     *
     * @param emoji das Emoji
     * @param alias der Kurzname, der zur Eingabe gepasst hat
     */
    public record Suggestion(Emoji emoji, String alias) {
    }

    /**
     * Sucht Emojis zu einem angefangenen Kurznamen.
     *
     * <p>Rangfolge: exakter Kurzname, Kurzname beginnt mit der Eingabe, ein Wort im Kurznamen
     * beginnt damit, der Kurzname enthaelt sie, ein Stichwort beginnt damit. Innerhalb einer Stufe
     * kommen kurze Namen vor langen.
     *
     * @param query die Eingabe ohne Doppelpunkt
     * @param limit die hoechste Anzahl Vorschlaege
     * @return die Vorschlaege, bester zuerst
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
