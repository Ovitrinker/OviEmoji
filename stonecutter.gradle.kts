plugins {
    id("dev.kikugie.stonecutter")
}

// Die Version, die im Arbeitsbaum und in der IDE aktiv ist.
// Stonecutter schaltet den Quellcode beim Wechsel an dieser Stelle um.
stonecutter active "1.21.11"

// Sammelt eine Aufgabe ueber alle Knoten der Versionsmatrix, damit sich mit einem
// Aufruf fuer jede Zielversion ein Jar bauen laesst.
stonecutter tasks {
    order("buildAndCollect")
}
