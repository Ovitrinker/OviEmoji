pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // Multiversion-Verwaltung: https://stonecutter.kikugie.dev
    id("dev.kikugie.stonecutter") version "0.9.7"

    // Waehlt automatisch die passende Loom-Variante:
    // - Minecraft < 26.1 (obfuskiert)    -> "fabric-loom"
    // - Minecraft >= 26.1 (unobfuskiert) -> "net.fabricmc.fabric-loom"
    id("dev.kikugie.loom-back-compat") version "0.4.2"

    // Laedt fehlende JDKs (Java 21 fuer 1.21.11, Java 25 ab 26.1) automatisch nach
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        // Versionsmatrix. Der erste Wert ist der Name des Build-Knotens,
        // der zweite die tatsaechliche Minecraft-Version, gegen die kompiliert wird.
        version("1.21.11", "1.21.11")
        version("26.2.x", "26.2")
        version("26.3.x", "26.3")

        // Version, die im Git-/IDE-Zustand aktiv ist
        vcsVersion = "1.21.11"
    }
}

rootProject.name = "OviEmoji"
