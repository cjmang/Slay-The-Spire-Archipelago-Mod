plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.0"
}

description = "Archipelago Multi-World Integration into Slay the Spire"
version = "1.15-SNAPSHOT"

defaultTasks = mutableListOf("deployLocal")

repositories {
    mavenCentral()
}

val steamPath = providers.gradleProperty("steam.path").get()

dependencies {
    // Use JUnit Jupiter for testing.
//    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("io.github.ArchipelagoMW:Java-Client:latest.integration")

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

//tasks.named<Test>("test") {
//    // Use JUnit Platform for unit tests.
//    useJUnitPlatform()
//}

tasks.register<Copy>("deployLocal") {
    val mwJar = fileTree(project.layout.buildDirectory.dir("libs")).filter { f: File ->
        f.name.matches(Regex("ArchipelagoMW.*\\.jar"))
    }
    from(mwJar)
    into("${steamPath}/common/SlayTheSpire/mods/")
    dependsOn("shadowJar")
}