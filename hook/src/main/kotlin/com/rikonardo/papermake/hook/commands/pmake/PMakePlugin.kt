package com.rikonardo.papermake.hook.commands.pmake

import com.rikonardo.papermake.hook.HookManager
import com.rikonardo.papermake.hook.commands.SubCommand
import com.rikonardo.papermake.hook.plugin
import com.rikonardo.papermake.hook.utils.PluginUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.io.File

object PMakePlugin : SubCommand {
    override val usage = listOf(
        "load <plugin jar name>" to "Load external plugin",
        "unload <plugin name>" to "Unload external plugin",
        "enable <plugin name>" to "Enable plugin",
        "disable <plugin name>" to "Disable plugin",
        "reload <plugin name>" to "Reload plugin",
    )

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        val commands = listOf("load", "unload", "enable", "disable", "reload")
        return if (args.isEmpty()) commands
        else if (args.size == 1) commands.filter { it.startsWith(args[0]) }
        else if (args.size == 2) {
            return when (args[0]) {
                "load" -> {
                    File("plugins").listFiles()
                        ?.map { it.name }
                        ?.filter {
                            it.endsWith(".jar") &&
                            it != "_papermake_hook.jar" &&
                            !it.startsWith("_papermake_hooked_")
                        }?.filter { it.startsWith(args[1]) }
                        ?: emptyList()
                }
                "unload" -> {
                    Bukkit.getPluginManager().plugins
                        .filter { it != plugin && it !in HookManager.loaded }
                        .map { it.name }
                        .filter { it.startsWith(args[1]) }
                }
                "enable" -> {
                    Bukkit.getPluginManager().plugins
                        .filter { it != plugin && !it.isEnabled }
                        .map { it.name }
                        .filter { it.startsWith(args[1]) }
                }
                "disable", "reload" -> {
                    Bukkit.getPluginManager().plugins
                        .filter { it != plugin && it.isEnabled }
                        .map { it.name }
                        .filter { it.startsWith(args[1]) }
                }
                else -> emptyList()
            }
        } else emptyList()
    }

    override fun onCommand(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("§b[PaperMake]§r §cInvalid syntax§r")
            return
        }
        val secondArg = args.slice(1 until args.size).joinToString(" ")
        when (args[0]) {
            "load" -> {
                try {
                    val file = File("plugins").listFiles()?.find { it.name == secondArg }
                    if (file == null) {
                        sender.sendMessage("§b[PaperMake]§r §cPlugin not found§r")
                        return
                    }
                    val descr = plugin.pluginLoader.getPluginDescription(file)
                    if (Bukkit.getPluginManager().plugins.any { it.name == descr.name }) {
                        sender.sendMessage("§b[PaperMake]§r §cPlugin already loaded§r")
                        return
                    }
                    val p = PluginUtil.load(file)
                    if (p == null) {
                        sender.sendMessage("§b[PaperMake]§r §cFailed to load plugin§r")
                        return
                    }
                    sender.sendMessage("§b[PaperMake]§r §aPlugin §f${p.name}§a loaded§r")
                } catch (e: Exception) {
                    e.printStackTrace()
                    sender.sendMessage("§b[PaperMake]§r §cFailed to load plugin§r")
                    return
                }
            }
            "unload" -> {
                try {
                    val p = Bukkit.getPluginManager().getPlugin(secondArg)
                    if (p == null) {
                        sender.sendMessage("§b[PaperMake]§r §cPlugin not found§r")
                        return
                    }
                    if (p == plugin) {
                        sender.sendMessage("§b[PaperMake]§r §cCannot unload myself§r")
                        return
                    }
                    if (p in HookManager.loaded) {
                        sender.sendMessage("§b[PaperMake]§r §cCannot unload developed plugin, use /pmake reload§r")
                        return
                    }
                    if (!PluginUtil.unload(p)) {
                        sender.sendMessage("§b[PaperMake]§r §cFailed to unload plugin§r")
                        return
                    }
                    sender.sendMessage("§b[PaperMake]§r §aPlugin §f${p.name}§a unloaded§r")
                } catch (e: Exception) {
                    e.printStackTrace()
                    sender.sendMessage("§b[PaperMake]§r §cFailed to unload plugin§r")
                    return
                }
            }
            "enable" -> {
                val p = Bukkit.getPluginManager().getPlugin(secondArg)
                if (p == null) {
                    sender.sendMessage("§b[PaperMake]§r §cPlugin not found§r")
                    return
                }
                if (p.isEnabled) {
                    sender.sendMessage("§b[PaperMake]§r §cPlugin already enabled§r")
                    return
                }
                Bukkit.getPluginManager().enablePlugin(p)
                sender.sendMessage("§b[PaperMake]§r §aPlugin §f${p.name}§a enabled§r")
            }
            "disable" -> {
                val p = Bukkit.getPluginManager().getPlugin(secondArg)
                if (p == null) {
                    sender.sendMessage("§b[PaperMake]§r §cPlugin not found§r")
                    return
                }
                if (!p.isEnabled) {
                    sender.sendMessage("§b[PaperMake]§r §cPlugin already disabled§r")
                    return
                }
                if (p == plugin) {
                    sender.sendMessage("§b[PaperMake]§r §cCannot disable myself§r")
                    return
                }
                Bukkit.getPluginManager().disablePlugin(p)
                sender.sendMessage("§b[PaperMake]§r §aPlugin §f${p.name}§a disabled§r")
            }
            "reload" -> {
                val p = Bukkit.getPluginManager().getPlugin(secondArg)
                if (p == null) {
                    sender.sendMessage("§b[PaperMake]§r §cPlugin not found§r")
                    return
                }
                if (!p.isEnabled) {
                    sender.sendMessage("§b[PaperMake]§r §cPlugin not enabled§r")
                    return
                }
                if (p == plugin) {
                    sender.sendMessage("§b[PaperMake]§r §cCannot reload myself§r")
                    return
                }
                Bukkit.getPluginManager().disablePlugin(p)
                Bukkit.getPluginManager().enablePlugin(p)
                sender.sendMessage("§b[PaperMake]§r §aPlugin §f${p.name}§a reloaded§r")
            }
        }
    }
}
