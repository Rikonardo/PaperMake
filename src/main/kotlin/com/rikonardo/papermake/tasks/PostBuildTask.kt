package com.rikonardo.papermake.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.random.Random

open class PostBuildTask : DefaultTask() {

    // The tasks to try to get artifacts from, in order.
    private val artifactTasks = arrayOf("reobfJar", "shadowJar", "jar")

    private val dir = project.buildDir.resolve("papermake")
    private val buildDir = dir.resolve("build")
    private val trigger = buildDir.resolve("reload.list")
    private val plugins = buildDir.resolve("artifacts")

    init {
        description = "This task is executed after the build, you don't need to run it manually."
    }

    @TaskAction
    fun execute() {
        var artifacts = setOf<File>()
        for (task in artifactTasks) {
            val files = project.tasks.findByName(task)?.outputs?.files?.files
            if (files.isNullOrEmpty())
                continue
            artifacts = files
            break
        }
        if (artifacts.isEmpty()) {
            throw IllegalStateException("No artifacts found")
        }

        if (plugins.exists()) plugins.deleteRecursively()
        plugins.mkdirs()
        val stored = artifacts.map {
            val tempName = randomString(32) + ".jar"
            val file = plugins.resolve(tempName)
            it.copyTo(file)
        }
        trigger.writeBytes(stored.joinToString("\n") { it.canonicalPath }.encodeToByteArray())
    }

    private fun randomString(length: Int): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
}
