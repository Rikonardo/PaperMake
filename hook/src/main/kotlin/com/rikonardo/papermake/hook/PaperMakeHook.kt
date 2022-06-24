package com.rikonardo.papermake.hook

import com.rikonardo.papermake.hook.commands.PMakeCommand
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

lateinit var plugin: PaperMakeHook

class PaperMakeHook : JavaPlugin() {
    init {
        plugin = this
    }

    override fun onEnable() {
        val watch = System.getProperty("papermake.watch")
        if (watch == null || !File(watch).exists()) {
            logger.warning("Property papermake.watch is not set or watch target does not exist, disabling")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        getCommand("pmake")!!.apply {
            executor = PMakeCommand
            tabCompleter = PMakeCommand
        }
        HookManager.setup(this, File(watch))
    }

    override fun onDisable() {
        HookManager.unload()
        HookManager.watcher.stop.set(true)
        logger.info("All hooked plugins unloaded, disabling hook")
    }
}
