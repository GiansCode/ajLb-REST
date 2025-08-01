plugins {
	java
	id("com.gradleup.shadow").version("8.3.0")
	id("xyz.jpenilla.run-paper") version "2.3.1"
	id("org.openapi.generator") version "7.14.0"
}

group = "us.ajg0702"
version = "1.0.0"

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
	maven { url = uri("https://jcenter.bintray.com/") }
	maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }

	maven { url = uri("https://repo.ajg0702.us/releases/") }

	maven { url = uri("https://repo.opencollab.dev/main/") }
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility= JavaVersion.VERSION_1_8
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.18.2-R0.1-SNAPSHOT")
	implementation("com.github.masecla22:java-express:0.2.2")
	implementation("com.squareup.okhttp3:okhttp:4.11.0");
	implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
	implementation("javax.annotation:javax.annotation-api:1.3.2")
	//gsonfire
	implementation("io.gsonfire:gson-fire:1.9.0")

	compileOnly("org.spongepowered:configurate-yaml:4.0.0")

	compileOnly("net.kyori:adventure-api:4.10.1")
	compileOnly("net.kyori:adventure-text-minimessage:4.10.0")
	compileOnly("net.kyori:adventure-platform-bukkit:4.1.0")

	compileOnly("us.ajg0702:ajUtils:1.1.34")
	compileOnly("us.ajg0702.commands.platforms.bukkit:bukkit:1.0.0-pre14")
	compileOnly("us.ajg0702.commands.api:api:1.0.0-pre14")

	compileOnly("us.ajg0702:ajLeaderboards:2.10.1")
	
	compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")
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

tasks {
	runServer {
		// Configure the Minecraft version for our task.
		// This is the only required configuration besides applying the plugin.
		// Your plugin's jar (or shadowJar if present) will be used automatically.
		minecraftVersion("1.21.4")

		downloadPlugins {
			modrinth("ajLeaderboards", "2.10.1")
			hangar("PlaceholderAPI", "2.11.6")
			hangar("Geyser", "Geyser")
			hangar("Floodgate", "Floodgate")
			hangar("ViaVersion", "5.5.0-SNAPSHOT+793")
		}
	}
}
openApiGenerate {
	generatorName.set("java")
	remoteInputSpec.set("https://api.geysermc.org/openapi")
	skipValidateSpec.set(true)
	packageName.set("us.ajg0702.leaderboards.rest.generated.geyser")
	apiPackage.set("us.ajg0702.leaderboards.rest.generated.geyser.api")
	modelPackage.set("us.ajg0702.leaderboards.rest.generated.geyser.model")
	logToStderr = true
	cleanupOutput = true
}

tasks.compileJava {
	dependsOn(tasks.named("openApiGenerate"))
}

sourceSets {
	main {
		java {
			srcDirs("src/main/java", "${openApiGenerate.outputDir.get()}/src/main/java")
		}
	}
}

tasks.getByName<Test>("test") {
	useJUnitPlatform()
}