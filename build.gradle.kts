import java.util.regex.Pattern

plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.0"
}

description = "Archipelago Multi-World Integration into Slay the Spire"
version = "2.0.4"

defaultTasks = mutableListOf("deployLocal")

repositories {
    mavenCentral()
}

val steamPath = providers.gradleProperty("steam.path").get()

dependencies {
    // Use JUnit Jupiter for testing.
//    testImplementation(libs.junit.jupiter)

//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(project(":client-wrapper"))

    shadow(files("${steamPath}/common/SlayTheSpire/desktop-1.0.jar"))
    shadow(files("${steamPath}/workshop/content/646570/1605060445/ModTheSpire.jar"))
    shadow(files("${steamPath}/workshop/content/646570/1605833019/BaseMod.jar"))
    shadow(files("${steamPath}/workshop/content/646570/1610056683/downfall.jar"))
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

tasks.processResources {
    filesMatching("ModTheSpire.json") {
        expand(project.properties)
    }
}

tasks.register<Copy>("deployLocal") {
    project.version = project.version.toString() + "-dev"
    val mwJar = fileTree(project.layout.buildDirectory.dir("libs")).filter { f: File ->
        f.name.matches(Regex("ArchipelagoMW-${Pattern.quote(project.version.toString())}-all\\.jar"))
    }
    from(mwJar)
    into("${steamPath}/common/SlayTheSpire/mods/")
    dependsOn("shadowJar")
}

tasks.register<Copy>("workshopUpload") {
    val mwJar = fileTree(project.layout.buildDirectory.dir("libs")).filter { f: File ->
        f.name.matches(Regex("ArchipelagoMW-${Pattern.quote(project.version.toString())}-all\\.jar"))
    }
    val configJson = fileTree(project.layout.projectDirectory.file("workshop.json"))
    val workshopImage = fileTree(project.layout.buildDirectory.dir("resources")).filter { f: File  ->
        f.name.matches(Regex("workshop_image\\.jpg"))
    }
    into("${steamPath}/common/SlayTheSpire/ap/")
    from(mwJar) {
        into("content")
    }
    from(configJson) {
        rename("workshop.json", "config.json")
    }
    from(workshopImage) {
        rename("workshop_image.jpg", "image.jpg")
    }
    dependsOn("shadowJar")
}
