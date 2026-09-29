package ch.ovitrinker.oviemoji.config;

import ch.ovitrinker.oviemoji.emoji.Emoji;
import ch.ovitrinker.oviemoji.emoji.Emojis;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merkt sich die zuletzt benutzten Emojis fuer den ersten Reiter im Auswahlfenster.
 *
 * <p>Gespeichert werden die Kurznamen in {@code config/oviemoji.json}, damit die Liste auch nach
 * einem Update mit neu sortierter Emoji-Tabelle stimmt.
 */
public final class RecentEmojis {

    /** Zwei volle Zeilen im Auswahlfenster. */
    public static final int MAX = 20;

    private static final Logger LOGGER = LoggerFactory.getLogger("oviemoji");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<String> recent = new ArrayList<>();

    /** Aufbau der Datei. */
    private static final class Data {
        List<String> recent = new ArrayList<>();
    }

    private RecentEmojis() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("oviemoji.json");
    }

    /** Liest die Datei. Fehlt sie oder ist sie kaputt, beginnt die Liste leer. */
    public static void load() {
        Path file = file();
        if (!Files.exists(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Data data = GSON.fromJson(reader, Data.class);
            recent.clear();
            if (data != null && data.recent != null) {
                for (String name : data.recent) {
                    if (name != null && recent.size() < MAX) {
                        recent.add(name);
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("config/oviemoji.json konnte nicht gelesen werden", e);
        }
    }

    /**
     * Setzt ein Emoji an den Anfang der Liste und speichert.
     *
     * @param emoji das benutzte Emoji
     */
    public static void add(Emoji emoji) {
        recent.remove(emoji.name());
        recent.add(0, emoji.name());
        while (recent.size() > MAX) {
            recent.remove(recent.size() - 1);
        }
        save();
    }

    /** Gibt die zuletzt benutzten Emojis zurueck, das neueste zuerst. */
    public static List<Emoji> list() {
        List<Emoji> out = new ArrayList<>();
        for (String name : recent) {
            Emoji emoji = Emojis.index().byAlias(name);
            if (emoji != null) {
                out.add(emoji);
            }
        }
        return out;
    }

    private static void save() {
        Data data = new Data();
        data.recent = new ArrayList<>(recent);
        try {
            Files.createDirectories(file().getParent());
            try (Writer writer = Files.newBufferedWriter(file(), StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("config/oviemoji.json konnte nicht geschrieben werden", e);
        }
    }
}
