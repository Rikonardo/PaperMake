package com.rikonardo.papermake.hook.utils

import java.util.*

object RunData {
    val papermakeVersion: String
    val papermakeHookStarted: Long
    val projectGroup: String
    val projectName: String
    val projectVersion: String
    val jdkVersion: String
    val gradleVersion: String
    val osName: String
    val osVersion: String
    val osArch: String

    init {
        val p = Properties()
        p.load(javaClass.getResourceAsStream("/papermake.properties"))
        papermakeVersion = p.getProperty("papermake.version")
        papermakeHookStarted = p.getProperty("papermake.hook.started").toLong()
        projectGroup = p.getProperty("project.group")
        projectName = p.getProperty("project.name")
        projectVersion = p.getProperty("project.version")
        jdkVersion = p.getProperty("jdk.version")
        gradleVersion = p.getProperty("gradle.version")
        osName = p.getProperty("os.name")
        osVersion = p.getProperty("os.version")
        osArch = p.getProperty("os.arch")
    }
}
