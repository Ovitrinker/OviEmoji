plugins {
    // Applies the matching Loom variant depending on the Minecraft version
    id("dev.kikugie.loom-back-compat")
}

// group must not be set - Stonecutter/Loom handle it
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
    // Mojang mappings on the obfuscated 1.21.11 too (Yarn is discontinued)
    loomx.applyMojangMappings()

    // "mod..." configurations on 26.1+ too - loom-back-compat translates them
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    // The mod doesn't need any Fabric API module at compile time. At runtime the
    // Fabric API (Resource Loader) loads the font and translations from the mod jar.

    // Dev client only: the full Fabric API, so the "fabric-api" bundle mod required by
    // fabric.mod.json is present. Not included in the jar.
    val fabricApiVersion: String = sc.properties["deps.fabric_api"]
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    // EmojiIndex and EmojiText don't know any Minecraft classes and can
    // therefore be tested with plain JUnit, without starting a client.
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

loom {
    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run") // Shared run directory for all versions
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
        description = "Builds the mod jar and collects it in build/libs/{mod version}/"

        inputs.property("version", project.property("mod.version"))
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
