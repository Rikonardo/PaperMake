package com.rikonardo.papermake.hook

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.EventListener

class PaperMakeHook : JavaPlugin(), EventListener {
    val loaded = mutableSetOf<Plugin>()
    val previousLoad = mutableSetOf<File>()
    var watcher: FileWatcher? = null
    var watchDir: File? = null

    override fun onEnable() {
        val watch = System.getProperty("papermake.watch")
        if (watch == null || !File(watch).exists()) {
            logger.warning("Property papermake.watch is not set or watch target does not exist, disabling")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        watchDir = File(watch)
        Bukkit.getScheduler().runTaskLater(this, {
            File("plugins").listFiles()?.forEach { file ->
                if (file.name.startsWith("_papermake_hooked_")) {
                    try {
                        val descr = pluginLoader.getPluginDescription(file)
                        Bukkit.getPluginManager().getPlugin(descr.name)?.let {
                            loaded.add(it)
                        }
                    } catch (_: Exception) {
                        // ignore
                    }
                    previousLoad.add(file)
                }
            }
            var reloadLock = false
            watcher = FileWatcher(watchDir!!.resolve("reload.list")) {
                if (!reloadLock) Bukkit.getScheduler().runTask(this) {
                    reload()
                    reloadLock = false
                }
                reloadLock = true
            }
            watcher?.start()
            logger.info("Hook loaded")
        }, 1)
    }

    override fun onDisable() {
        logger.info("Hook unloaded")
        unload()
        watcher?.stop?.set(true)
    }

    private fun unload() {
        loaded.forEach {
            try {
                PluginUtil.unload(it)
                broadcast("${it.name} unloaded", true)
            } catch (e: Exception) {
                e.printStackTrace()
                broadcast("Plugin ${it.name} failed to unload", false)
            }
        }
        loaded.clear()
    }

    private fun load() {
        var fails = 0
        previousLoad.forEach { if (!it.delete()) it.deleteOnExit() }
        previousLoad.clear()
        watchDir!!.resolve("reload.list").readLines().map { File(it) }.forEach {
            previousLoad.add(it)
            try {
                val plugin = PluginUtil.load(it)
                if (plugin != null) {
                    loaded.add(plugin)
                    broadcast("${plugin.name} loaded", true)
                } else fails++
            } catch (e: Exception) {
                e.printStackTrace()
                fails++
            }
        }
        if (fails > 0) broadcast(
            "$fails plugin${if (fails > 1) "s" else ""} failed to load, check console for details",
            false
        )
    }

    private fun reload() {
        broadcast("Reloading...", true)
        unload()
        load()
        broadcast("Plugin${if (loaded.size > 1) "s" else ""} reloaded!", true)
    }

    private fun broadcast(text: String, ok: Boolean) {
        if (ok)
            logger.info(text)
        else
            logger.warning(text)
        Bukkit.getOnlinePlayers().forEach { it.sendMessage("§b§l[PaperMake]§r " + (if (ok) "§a" else "§e") + text) }
    }
}
