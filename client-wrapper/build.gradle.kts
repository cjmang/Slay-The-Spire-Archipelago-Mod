
plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.github.archipelagomw:Java-Client:latest.integration")
}

tasks.shadowJar {
    relocate("com.google", "io.cjmang.google")
}

