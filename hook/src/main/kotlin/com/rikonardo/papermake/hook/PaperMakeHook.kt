package com.rikonardo.papermake.hook

import com.rikonardo.papermake.hook.commands.PMakeCommand
import com.rikonardo.papermake.hook.listeners.PlayerJoinListener
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

        if (System.getProperty("papermake.autoop", "false").toBoolean()) {
            logger.info("Auto-OP enabled, all players that join will be OPed!")
            Bukkit.getPluginManager().registerEvents(PlayerJoinListener, this)
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
