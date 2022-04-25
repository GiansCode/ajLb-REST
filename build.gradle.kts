plugins {
    java
    id("com.github.johnrengelman.shadow").version("6.1.0")
}

group = "us.ajg0702"
version = "1.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://jcenter.bintray.com/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }

    maven { url = uri("https://repo.ajg0702.us/") }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.18.2-R0.1-SNAPSHOT")
    implementation("io.vacco.java-express:java-express:0.2.1")

    compileOnly("org.spongepowered:configurate-yaml:4.0.0")

    compileOnly("net.kyori:adventure-api:4.10.1")
    compileOnly("net.kyori:adventure-text-minimessage:4.10.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.1.0")

    compileOnly("us.ajg0702:ajUtils:1.1.34")
    compileOnly("us.ajg0702.commands.platforms.bukkit:bukkit:1.0.0-pre14")
    compileOnly("us.ajg0702.commands.api:api:1.0.0-pre14")

    compileOnly("us.ajg0702:ajLeaderboards:2.4.3")
}

tasks.shadowJar {
    relocate("io.vacco", "us.ajg0702.leaderboards.rest.libs")

    relocate("us.ajg0702.utils", "us.ajg0702.leaderboards.libs.utils")
    relocate("us.ajg0702.commands", "us.ajg0702.leaderboards.commands.base")
    relocate("org.spongepowered", "us.ajg0702.leaderboards.libs")
    relocate("org.yaml", "us.ajg0702.leaderboards.libs")
    relocate("net.kyori", "us.ajg0702.leaderboards.libs.kyori")
    relocate("io.leangen", "us.ajg0702.leaderboards.libs")

    minimize()

    archiveBaseName.set("ajLb-REST")
    archiveClassifier.set("")
    exclude("junit/**/*")
    exclude("org/junit/**/*")
    exclude("org/slf4j/**/*")
    exclude("org/hamcrest/**/*")
    exclude("LICENSE-junit.txt")
}


tasks.withType<ProcessResources> {
    include("**/*.yml")
    filter<org.apache.tools.ant.filters.ReplaceTokens>(
        "tokens" to mapOf(
            "VERSION" to project.version.toString()
        )
    )
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}