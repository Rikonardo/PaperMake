package com.rikonardo.papermake

import java.util.*

object ReleaseData {
    val version: String

    init {
        val p = Properties()
        p.load(javaClass.getResourceAsStream("/META-INF/papermake/build.properties"))
        version = p.getProperty("version")
    }
}
