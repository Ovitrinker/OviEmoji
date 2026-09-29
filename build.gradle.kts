plugins {
    // Wendet je nach Minecraft-Version die passende Loom-Variante an
    id("dev.kikugie.loom-back-compat")
}

// group darf nicht gesetzt werden - Stonecutter/Loom regeln das
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

/** Ab 26.1 verlangt Minecraft Java 25, davor Java 21. */
val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Mojang-Mappings auch auf der obfuskierten Version 1.21.11 (Yarn ist eingestellt)
    loomx.applyMojangMappings()

    // "mod..."-Konfigurationen auch auf 26.1+ - loom-back-compat setzt sie um
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    // Die Mod braucht zur Kompilierzeit kein Fabric-API-Modul. Zur Laufzeit laedt die
    // Fabric API (Resource Loader) die Schrift und die Uebersetzungen aus dem Mod-Jar.

    // Nur fuer den Entwicklungsclient: die vollstaendige Fabric API, damit das Bundle-Mod
    // "fabric-api" vorhanden ist, das die fabric.mod.json voraussetzt. Landet nicht im Jar.
    val fabricApiVersion: String = sc.properties["deps.fabric_api"]
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    // EmojiIndex und EmojiText kennen keine Minecraft-Klassen und lassen
    // sich deshalb mit reinem JUnit pruefen, ohne einen Client zu starten.
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

loom {
    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run") // Gemeinsames Run-Verzeichnis aller Versionen
        jvmArguments.add("-Dmixin.debug.export=true")
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    processResources {
        fun MutableMap<String, String>.register(key: String, property: String) {
            val value: String = sc.properties[property]
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", "mod.id")
            register("name", "mod.name")
            register("version", "mod.version")
            register("minecraft", "mod.mc_compat")
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Baut das Mod-Jar und sammelt es unter build/libs/{mod version}/"

        inputs.property("version", project.property("mod.version"))
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
