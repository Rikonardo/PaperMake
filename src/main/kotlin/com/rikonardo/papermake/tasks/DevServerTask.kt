package com.rikonardo.papermake.tasks

import com.rikonardo.papermake.ReleaseData
import com.rikonardo.papermake.utils.freePort
import com.rikonardo.papermake.utils.getFileSHA256
import dev.virefire.yok.Yok
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.StringWriter
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.*
import javax.inject.Inject
import kotlin.io.path.writeText

open class DevServerTask : JavaExec() {
    init {
        group = "PaperMake"
        description = "Runs development Minecraft server."
    }

    open class PreDevServerTask @Inject constructor(private val devServer: DevServerTask) : DefaultTask() {
        private val dir = project.buildDir.resolve("papermake")
        private var runDir = dir.resolve("run")
        private var hook = runDir.resolve("plugins/_papermake_hook.jar")
        private val serversDir = dir.resolve("servers")
        private val buildDir = dir.resolve("build")
        private var version = ""
            set(value) {
                field = value
                val split = value.split(".")
                minorVersion = split[1].toInt()
                patchVersion = if (split.size >= 3) split[2].toInt() else 0
            }
        private var minorVersion = -1
        private var patchVersion = -1

        init {
            description = "This task is executed before launching devServer, you don't need to run it manually."
        }

        @TaskAction
        fun execute() {
            if (project.hasProperty("pmake.dir")) {
                runDir = project.projectDir.resolve(project.property("pmake.dir").toString())
                hook = runDir.resolve("plugins/_papermake_hook.jar")
            }
            runDir.mkdirs()
            println("*** By using PaperMake dev server, you agree to Minecraft EULA ***")
            val server = getServer()
            val propertiesFile = runDir.resolve("server.properties")
            val firstRun = !propertiesFile.exists()
            val properties = Properties()
            if (!propertiesFile.exists()) {
                properties.setProperty("max-tick-time", "0")
                properties.setProperty("motd", "PaperMake Dev Server")
            } else {
                properties.load(propertiesFile.inputStream())
                if ((properties["max-tick-time"]?.toString()?.toIntOrNull() ?: 0) > 0) {
                    println("WARNING: max-tick-time is set to non-zero value in server.properties, this may cause server to crash when using breakpoints")
                }
            }
            if (project.hasProperty("pmake.serverprops")) {
                val rawProps = project.property("pmake.serverprops").toString()
                val props = rawProps.split(",")
                for (prop in props) {
                    val args = prop.split("=")
                    if (args.size != 2) {
                        throw Exception("Invalid server property '$prop'")
                    }
                    properties.setProperty(args[0], args[1])
                }
            }
            properties.store(propertiesFile.outputStream(), "Minecraft server properties")
            installHook()
            val args = mutableListOf<String>()
            if ((minorVersion >= 15 && (minorVersion != 15 || patchVersion > 1)) && (!project.hasProperty("pmake.gui") || !project.property("pmake.gui").toString().toBoolean()))
                args.add("-nogui")
            val port = freePort(
                if (project.hasProperty("pmake.port")) project.property("pmake.port").toString().toInt()
                else 25565
            )
            args.add("-p=$port")
            if (project.hasProperty("pmake.args")) {
                args.addAll(project.property("pmake.args").toString().split(" "))
            }
            runDir.resolve("plugins").listFiles()?.forEach {
                if (it.name.startsWith("_papermake_hooked_")) it.delete()
            }
            buildDir.resolve("artifacts").listFiles()?.forEach {
                it.copyTo(runDir.resolve("plugins").resolve("_papermake_hooked_" + it.name))
            }
            val iconFile = runDir.resolve("server-icon.png")
            if (firstRun && !iconFile.exists()) {
                val internalIcon = DevServerTask::class.java.classLoader.getResourceAsStream("server-icon.png")
                if (internalIcon == null) {
                    println("Failed to find default server icon!")
                } else {
                    Files.write(iconFile.toPath(), internalIcon.readBytes(), StandardOpenOption.CREATE_NEW)
                }
            }
            devServer.systemProperty("com.mojang.eula.agree", "true")
            devServer.systemProperty("papermake.watch", buildDir.canonicalPath)
            devServer.systemProperty("papermake.autoop", project.hasProperty("pmake.autoop") && project.property("pmake.autoop").toString().toBoolean())
            if (project.hasProperty("pmake.gamerules")) {
                devServer.systemProperty("papermake.gamerules", project.property("pmake.gamerules").toString())
            }
            if (project.hasProperty("pmake.newloader")) {
                devServer.systemProperty("papermake.newloader", project.property("pmake.newloader").toString().toBoolean())
            }
            devServer.classpath(server)
            devServer.standardInput = System.`in`
            devServer.workingDir = runDir
            devServer.args = args
            println("Starting server on port $port")
        }

        private fun installHook() {
            hook.parentFile.mkdirs()
            Files.copy(
                this.javaClass.getResourceAsStream("/META-INF/papermake/hook-${ReleaseData.version}.jar")!!,
                hook.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
            val fs = FileSystems.newFileSystem(hook.toPath(), null as ClassLoader?)
            val properties = Properties()
            properties["papermake.version"] = ReleaseData.version
            properties["papermake.hook.started"] = System.currentTimeMillis().toString()
            properties["project.group"] = project.group
            properties["project.name"] = project.name
            properties["project.version"] = project.version
            properties["jdk.version"] = System.getProperty("java.version")
            properties["gradle.version"] = project.gradle.gradleVersion
            properties["os.name"] = System.getProperty("os.name")
            properties["os.version"] = System.getProperty("os.version")
            properties["os.arch"] = System.getProperty("os.arch")
            val writer = StringWriter()
            properties.store(writer, null)
            fs.getPath("/papermake.properties").writeText(writer.toString())
            fs.close()
        }

        private fun getServer(): File {
            if (project.hasProperty("pmake.server")) {
                val server = project.property("pmake.server").toString()
                val file = runDir.resolve(server)
                if (!file.exists()) throw IllegalArgumentException("Server file \"${file.canonicalPath}\" does not exist")
                return file
            }
            return installPaper()
        }

        private fun installPaper(): File {
            var artifact: Pair<String, String>? = null
            val isMojmap = project.hasProperty("pmake.mojmap") && project.property("pmake.mojmap").toString().toBoolean()
            val type = if (isMojmap) "paper-mojmap" else "paper"
            if (project.hasProperty("pmake.version")) {
                version = project.property("pmake.version").toString()
                if (serversDir.resolve("$type/$version.jar").exists()) {
                    return serversDir.resolve("$type/$version.jar")
                }
                artifact = getPaperArtifact(version, mojmap = isMojmap) ?: throw Exception("No paper build found for version $version")
            } else {
                try {
                    val paper =
                        Yok.get("https://api.papermc.io/v2/projects/paper").body.json["versions"].list!!.map { it.string!! }
                    for (i in paper.lastIndex downTo 0) {
                        version = paper[i]
                        if (serversDir.resolve("$type/$version.jar").exists()) {
                            return serversDir.resolve("$type/$version.jar")
                        }
                        artifact = getPaperArtifact(version, mojmap = isMojmap) ?: continue
                        break
                    }
                    if (artifact == null) {
                        throw Exception("No version found")
                    }
                } catch (e: Exception) {
                    println("Failed to fetch latest Paper version ($e)")
                    serversDir.listFiles()?.last()?.let { return it }
                    println("Failed to find cached Paper installation")
                    throw Exception("Can't get Minecraft server, you can specify it manually via -Ppmake.server=\"/path/to/server.jar\"")
                }
            }
            serversDir.resolve(type).mkdirs()
            val file = serversDir.resolve("$type/$version.jar")
            println("Downloading Paper server $version")
            Files.copy(Yok.get(artifact.first).body.stream, file.toPath())
            if (!project.hasProperty("pmake.noverify") || !project.property("pmake.noverify").toString().toBoolean()) {
                println("Verifying server checksum")
                val hash = getFileSHA256(file)
                if (hash != artifact.second) {
                    file.delete()
                    throw Exception("Failed to download Paper server, checksum mismatch. Try running this task again or add -Ppmake.noverify=true to ignore hash checks")
                }
            }
            return file
        }

        private fun getPaperArtifact(version: String, mojmap: Boolean = false): Pair<String, String>? {
            try {
                val builds =
                    Yok.get("https://api.papermc.io/v2/projects/paper/versions/$version/builds").body.json["builds"].list!!
                if (builds.isEmpty()) return null
                val latest = builds.last()
                val buildNumber: Int = latest["build"].int!!
                val downloadArtifact = if (mojmap) "mojang-mappings" else "application"
                val artifactName = latest["downloads"][downloadArtifact]["name"].string!!
                val artifactHash = latest["downloads"][downloadArtifact]["sha256"].string!!
                return Pair(
                    "https://api.papermc.io/v2/projects/paper/versions/$version/builds/$buildNumber/downloads/$artifactName",
                    artifactHash
                )
            } catch (e: Exception) {
                return null
            }
        }
    }
}
