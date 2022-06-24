plugins {
    kotlin("jvm") version "1.7.0"
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.0.0-rc-2"
}

group = "com.rikonardo.papermake"
version = "1.0.3"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.rikonardo.com/releases")
    }
}

dependencies {
    compileOnly("dev.virefire.yok:Yok:1.0.4")
    compileOnly("dev.virefire.kson:KSON:1.3.1")
    compileOnly("org.apache.httpcomponents:httpmime:4.5.13")
    compileOnly("com.google.code.gson:gson:2.9.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

task("buildProperties") {
    this.temporaryDir.mkdirs()
    val propsFile = this.temporaryDir.resolve("build.properties")
    val buildProps = org.jetbrains.kotlin.konan.properties.Properties()
    buildProps.setProperty("version", project.version as String)
    buildProps.store(propsFile.outputStream(), null)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(project(":hook").tasks.build)
    dependsOn("buildProperties")
    from(project(":hook").buildDir.resolve("libs")) {
        include("**/*.jar").into("META-INF/papermake")
    }
    from(project.tasks.getByName("buildProperties").temporaryDir) {
        include("build.properties").into("META-INF/papermake")
    }
    from(configurations.compileClasspath.get().all.map { el ->
        el.dependencies.mapNotNull deps@ { dep ->
            if (dep !is org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency) return@deps null
            configurations.compileClasspath.get().files(dep).map {
                if (it.isDirectory) it else zipTree(it)
            }
        }.flatten()
    }.flatten().distinct())
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            pom {
                name.set("PaperMake")
                description.set("Tool for Bukkit-based Minecraft plugins development")
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

gradlePlugin {
    plugins {
        create("papermake") {
            id = "com.rikonardo.papermake"
            displayName = "PaperMake"
            description = "Tool for Bukkit-based Minecraft plugins development"
            implementationClass = "com.rikonardo.papermake.PaperMakePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/Rikonardo/PaperMake"
    vcsUrl = "https://github.com/Rikonardo/PaperMake"
    tags = listOf("paper", "bukkit", "spigot", "minecraft", "plugins", "devtools")
}
