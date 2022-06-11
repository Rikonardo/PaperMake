package com.rikonardo.papermake

import java.util.*

object ReleaseData {
    val version: String

    init {
        val p = Properties()
        p.load(PaperMakePlugin::class.java.getResourceAsStream("/META-INF/papermake/build.properties"))
        version = p.getProperty("version")
    }
}
