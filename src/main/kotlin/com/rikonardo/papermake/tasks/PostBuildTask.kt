package com.rikonardo.papermake.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import kotlin.random.Random

open class PostBuildTask : DefaultTask() {
    private val dir = project.buildDir.resolve("papermake")
    private val buildDir = dir.resolve("build")
    private val trigger = buildDir.resolve("reload.list")
    private val plugins = buildDir.resolve("artifacts")

    init {
        description = "This task is executed after the build, you don't need to run it manually."
    }

    @TaskAction
    fun execute() {
        val jar = project.tasks.findByName("jar")?.outputs?.files?.files
        val shadow = project.tasks.findByName("shadowJar")?.outputs?.files?.files

        val artifacts =
            if (shadow != null && shadow.isNotEmpty()) shadow else (if (jar != null && jar.isNotEmpty()) jar else null)
                ?: throw IllegalStateException("No artifacts found")

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
