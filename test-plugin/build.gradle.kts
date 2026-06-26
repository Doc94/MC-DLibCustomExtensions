plugins {
    alias(libs.plugins.java.library)
    alias(libs.plugins.lombok)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":core"))

    annotationProcessor(libs.incendo.cloud.annotations)
    annotationProcessor(project(":core"))

    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

val paperVersion = configurations.compileOnly.get().dependencies
    .find { it.group == "io.papermc.paper" && it.name == "paper-api" }
    ?.version ?: libs.versions.paper.get()

// Extract Minecraft version (e.g., "1.21.1" from "1.21.1-R0.1-SNAPSHOT" or "1.21.1.build-123")
val mcVersion = paperVersion.split("-")[0].split(".build")[0]

tasks {
    runServer {
        minecraftVersion(mcVersion)
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        val props = mapOf(
            "version" to project.version,
            "mcversion" to mcVersion
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        with(copySpec {
            from("src/main/resources") {
                include("paper-plugin.yml")
                expand(props)
            }
        })
    }

    shadowJar {
        relocate("org.incendo.cloud", "dev.mrdoc.minecraft.dlce.libs.org.incendo.cloud")
        relocate("com.github.fracpet", "dev.mrdoc.minecraft.dlce.libs.com.github.fracpet")
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }
}
