package com.rikonardo.papermake.hook.utils

import org.bukkit.plugin.Plugin
import java.io.File

interface PluginUtil {
    fun unload(plugin: Plugin): Boolean
    fun load(file: File): Plugin?
}