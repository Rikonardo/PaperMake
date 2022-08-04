package com.rikonardo.papermake.tasks

import com.rikonardo.papermake.ReleaseData
import com.rikonardo.papermake.utils.freePort
import com.rikonardo.papermake.utils.getFileSHA256
import dev.virefire.yok.Yok
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileNotFoundException
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
            val properties = runDir.resolve("server.properties")
            if (!properties.exists()) {
                val internalProps = DevServerTask::class.java.classLoader.getResourceAsStream("templates/server.properties")
                    ?: throw FileNotFoundException("Failed to find server.properties template")
                Files.write(properties.toPath(), internalProps.readBytes(), StandardOpenOption.CREATE_NEW)
            } else {
                val p = Properties()
                p.load(properties.inputStream())
                if ((p["max-tick-time"]?.toString()?.toIntOrNull() ?: 0) > 0) {
                    println("WARNING: max-tick-time is set to non-zero value in server.properties, this may cause server to crash when using breakpoints")
                }
            }
            installHook()
            val args = mutableListOf<String>()
            if (!project.hasProperty("pmake.gui") || !project.property("pmake.gui").toString().toBoolean())
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
            devServer.systemProperty("com.mojang.eula.agree", "true")
            devServer.systemProperty("papermake.watch", buildDir.canonicalPath)
            devServer.systemProperty("papermake.autoop", project.hasProperty("pmake.autoop") && project.property("pmake.autoop").toString().toBoolean())
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
            var v = ""
            if (project.hasProperty("pmake.version")) {
                v = project.property("pmake.version").toString()
                if (serversDir.resolve("$type/$v.jar").exists()) {
                    return serversDir.resolve("$type/$v.jar")
                }
                artifact = getPaperArtifact(v, mojmap = isMojmap) ?: throw Exception("No paper build found for version $v")
            } else {
                try {
                    val paper =
                        Yok.get("https://api.papermc.io/v2/projects/paper").body.json["versions"].list!!.map { it.string!! }
                    for (i in paper.lastIndex downTo 0) {
                        v = paper[i]
                        if (serversDir.resolve("$type/$v.jar").exists()) {
                            return serversDir.resolve("$type/$v.jar")
                        }
                        artifact = getPaperArtifact(v, mojmap = isMojmap) ?: continue
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
            val file = serversDir.resolve("$type/${v}.jar")
            println("Downloading Paper server $v")
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
