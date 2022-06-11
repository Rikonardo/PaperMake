package com.rikonardo.papermake

import com.rikonardo.papermake.tasks.DevServerTask
import com.rikonardo.papermake.tasks.PostBuildTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class PaperMakePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val devServer = project.tasks.create("devServer", DevServerTask::class.java)
        val preDevServer = project.tasks.create("papermakePrepareDevServer", DevServerTask.PreDevServerTask::class.java, devServer)
        devServer.dependsOn(preDevServer)
        preDevServer.dependsOn("build")
        val postBuild = project.tasks.create("papermakeFinalizeBuild", PostBuildTask::class.java)
        project.tasks.findByPath("build")?.finalizedBy(postBuild)
        project.tasks.findByPath("shadowJar")?.finalizedBy(postBuild)
    }
}
