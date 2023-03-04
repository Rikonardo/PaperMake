package com.rikonardo.papermake.hook

import com.rikonardo.papermake.hook.utils.FileWatcher
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import java.io.File
import java.nio.file.FileSystems
import kotlin.io.path.inputStream

object HookManager {
    val loaded = mutableSetOf<Plugin>()
    private val previousLoad = mutableSetOf<File>()
    private lateinit var watchDir: File
    lateinit var plugin: Plugin
    lateinit var watcher: FileWatcher

    fun setup(plugin: PaperMakeHook, watchDir: File) {
        this.plugin = plugin
        this.watchDir = watchDir
        Bukkit.getScheduler().runTaskLater(plugin, {
            File("plugins").listFiles()?.forEach { file ->
                if (file.name.startsWith("_papermake_hooked_")) {
                    try {
                        val fs = FileSystems.newFileSystem(file.toPath(), null as ClassLoader?)
                        val descr = PluginDescriptionFile(fs.getPath("plugin.yml").inputStream())
                        fs.close()
                        Bukkit.getPluginManager().getPlugin(descr.name)?.let {
                            loaded.add(it)
                        }
                    } catch (ignored: Exception) { }
                    previousLoad.add(file)
                }
            }
            var reloadLock = false
            watcher = FileWatcher(watchDir.resolve("reload.list")) {
                if (!reloadLock) Bukkit.getScheduler().runTask(plugin) {
                    reload(previous = false)
                    reloadLock = false
                }
                reloadLock = true
            }
            watcher.start()
            plugin.logger.info("Loaded PaperMake hook")
        }, 1)
    }

    fun unload() {
        loaded.forEach {
            try {
                pluginUtil.unload(it)
                broadcast("${it.name} unloaded", true)
            } catch (e: Exception) {
                e.printStackTrace()
                broadcast("Plugin ${it.name} failed to unload", false)
            }
        }
        loaded.clear()
    }

    fun load(previous: Boolean = false) {
        var fails = 0
        if (!previous) {
            previousLoad.forEach { if (!it.delete()) it.deleteOnExit() }
            previousLoad.clear()
        }
        (if (previous) previousLoad else watchDir.resolve("reload.list").readLines().map { File(it) }).forEach {
            if (!previous) previousLoad.add(it)
            try {
                val plugin = pluginUtil.load(it)
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

    fun reload(previous: Boolean = false) {
        if (previous) broadcast("Reloading without rebuilding...", true)
        else broadcast("Reloading...", true)
        unload()
        load(previous)
        broadcast("Plugin${if (loaded.size > 1) "s" else ""} reloaded!", true)
    }

    private fun broadcast(text: String, ok: Boolean) {
        if (ok)
            plugin.logger.info(text)
        else
            plugin.logger.warning(text)
        Bukkit.getOnlinePlayers().forEach { it.sendMessage("§b[PaperMake]§r " + (if (ok) "§a" else "§e") + text) }
    }
}
