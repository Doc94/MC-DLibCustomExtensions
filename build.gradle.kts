plugins {
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.lombok) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.jreleaser) apply false
    alias(libs.plugins.release) apply false
}

group = "dev.mrdoc.minecraft"
version = project.property("version").toString()

val projectArtifactName = rootProject.name.lowercase()
val isRelease = !version.toString().endsWith("-SNAPSHOT")
val projectVersion = version.toString()
val projectGroup = group.toString()

tasks.register("downloadDependencies") {
    description = "Download all dependencies to the Gradle cache"
    doLast {
        subprojects.forEach { subproject ->
            subproject.configurations.matching { it.isCanBeResolved }.forEach { it.resolve() }
        }
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
