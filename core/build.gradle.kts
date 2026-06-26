plugins {
    alias(libs.plugins.java.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.signing)
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.release)
}

group = "dev.mrdoc.minecraft"
version = project.property("version").toString()

val projectArtifactName = rootProject.name.lowercase()
val isRelease = !version.toString().endsWith("-SNAPSHOT")
val projectVersion = version.toString()
val projectGroup = group.toString()

dependencies {
    compileOnly(libs.paper)
    compileOnly(libs.configurate)
    compileOnly(libs.google.autoservice)

    implementation(libs.bundles.incendocloud)
    implementation(libs.romannumerals4j)

    annotationProcessor(libs.google.autoservice)
    annotationProcessor(libs.incendo.cloud.annotations)
}

java {
    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    relocate("org.incendo.cloud", "dev.mrdoc.minecraft.dlce.libs.org.incendo.cloud")
    relocate("com.github.fracpet", "dev.mrdoc.minecraft.dlce.libs.com.github.fracpet")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.named("shadowJar"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.isIncremental = false
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked", "-Xdoclint:html,syntax,reference,accessibility"))
}

tasks.withType<Javadoc>().configureEach {
    title = "$projectArtifactName $projectVersion API"
    (options as StandardJavadocDocletOptions).windowTitle = "$projectArtifactName ($projectVersion)"
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:all,-missing", true)
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    javadoc {
        options.encoding = "UTF-8"
    }
    named("jreleaserSign") {
        dependsOn(named("publish"))
    }
    named("jreleaserDeploy") {
        dependsOn(named("jreleaserSign"))
    }
}

tasks.register("downloadDependencies") {
    description = "Download all dependencies to the Gradle cache"
    doLast {
        configurations.matching { it.isCanBeResolved }.forEach { it.resolve() }
    }
}

tasks.register("publishJar") {
    description = "Publish the JAR where need to go"

    dependsOn(tasks.named("publish"))
    mustRunAfter(tasks.named("publish"))

    if (isRelease) {
        dependsOn(tasks.named("jreleaserSign"), tasks.named("jreleaserDeploy"))
        mustRunAfter(tasks.named("jreleaserSign"))
    }
}

release {
    setProperty("preTagCommitMessage", "chore: Release version")
    setProperty("tagCommitMessage", "chore: Release version")
    setProperty("newVersionCommitMessage", "chore: Next development version")
    setProperty("failOnSnapshotDependencies", false)
    git {
        requireBranch.set("")
        pushToRemote.set("")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = projectGroup
            version = projectVersion
            artifactId = projectArtifactName

            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            pom {
                name.set(projectArtifactName)
                description.set("A Minecraft library for custom things")
                url.set("https://mrdoc.dev/")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://spdx.org/licenses/MIT.html")
                    }
                }
                developers {
                    developer {
                        id.set("doc")
                        name.set("Pedro")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/Doc94/MC-DLibCustomExtensions.git")
                    developerConnection.set("scm:git:ssh://github.com/Doc94/MC-DLibCustomExtensions.git")
                    url.set("https://github.com/Doc94/MC-DLibCustomExtensions")
                }
            }
        }
    }

    repositories {
        maven {
            if (isRelease) {
                url = uri(layout.buildDirectory.dir("staging-deploy/release"))
            } else {
                credentials {
                    username = System.getenv("JRELEASER_MAVENCENTRAL_USERNAME")
                    password = System.getenv("JRELEASER_MAVENCENTRAL_PASSWORD")
                }
                url = uri("https://central.sonatype.com/repository/maven-snapshots/")
            }
        }
    }
}

jreleaser {
    if (isRelease) {
        signing {
            pgp {
                active.set(org.jreleaser.model.Active.ALWAYS)
                armored.set(true)
            }
        }
    }
    release {
        github {
            skipRelease.set(true) // we are releasing through GitHub UI
            skipTag.set(true)
            token.set("empty")
            changelog {
                enabled.set(false)
            }
        }
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(org.jreleaser.model.Active.RELEASE)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    namespace.set(projectGroup)
                    snapshotSupported.set(true)
                    retryDelay.set(120)
                    if (isRelease) {
                        applyMavenCentralRules.set(true)
                        stagingRepositories.add("build/staging-deploy/release")
                    } else {
                        sign.set(false)
                        stagingRepositories.add("build/staging-deploy/snapshot")
                    }
                }
            }
        }
    }
}
