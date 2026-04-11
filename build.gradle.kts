plugins {
    alias(libs.plugins.fabric.loom)
}

val lwjglVersion = "3.4.1"

val suffix: String = providers.gradleProperty("build_number").getOrElse("local")
version = "${libs.versions.minecraft.get()}-$suffix"
group = property("maven_group") as String
val minecraftVersion = property("minecraft_version") as String

base {
    archivesName = property("archives_base_name") as String
}

loom {
    accessWidenerPath = file("src/main/resources/pupper.classtweaker")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.lenni0451.net/everything")
    maven("https://repo.viaversion.com/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.florianreuth.de/snapshots")
}

configurations {
    create("modJij")
    "include" {
        extendsFrom(getByName("modJij"))
    }
    "implementation" {
        extendsFrom(getByName("modJij"))
    }
}

dependencies {
    // Minecraft & Fabric base
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    // Mod runtime
    implementation(libs.viafabricplus.api)
    runtimeOnly(libs.sodium)
    runtimeOnly(libs.iris)
    runtimeOnly(libs.lithium)
    runtimeOnly(libs.immediatelyfast)
    runtimeOnly(libs.entityculling)
    implementation(libs.modmenu)
    implementation(libs.viafabricplus)

    // lib
    "modJij"(libs.smartboot.aio)
    "modJij"(libs.caffeine)
    "modJij"(libs.humbleui.types)
    "modJij"(libs.skija.windows)
    "modJij"(libs.junixsocket.common)
    "modJij"(libs.java.websocket)
    "modJij"(libs.mp3agic)
    "modJij"(libs.jlayer)
    "modJij"(libs.mp3spi)
    "modJij"(libs.jaudiotagger)
    "modJij"(libs.jlayer.google)
    "modJij"(libs.mcping)
    "modJij"(libs.reflect)

    // JNA
    implementation(libs.jna)
    implementation(libs.jna.platform)

    // LWJGL NFD
    "modJij"(libs.lwjgl.nfd)

    // LWJGL NFD Natives
    val nfdNatives = listOf(
        "natives-linux",
        "natives-macos",
        "natives-macos-arm64",
        "natives-windows"
    )
    nfdNatives.forEach { classifier ->
        "modJij"("org.lwjgl:lwjgl-nfd:$lwjglVersion:$classifier")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", minecraftVersion)

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version,
                "minecraft_version" to minecraftVersion
            )
        )
    }

    doLast {
        val resourcePath = sourceSets.main.get().resources.srcDirs.first()
        val iconFile = File(resourcePath, "assets/pupper/logo.png")
        if (!iconFile.exists()) {
            throw GradleException("Pupper icon not found: ${iconFile.absolutePath}")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
}
