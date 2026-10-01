plugins {
    id("dev.kikugie.stonecutter")
}

// The version active in the working tree and the IDE.
// Stonecutter switches the source code here when it changes.
stonecutter active "1.21.11"

// Collects one task across all nodes of the version matrix, so a single call
// builds a jar for every target version.
stonecutter tasks {
    order("buildAndCollect")
}
