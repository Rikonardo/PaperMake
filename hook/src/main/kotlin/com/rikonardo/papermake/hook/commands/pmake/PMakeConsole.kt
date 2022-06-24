package com.rikonardo.papermake.hook.commands.pmake

import com.rikonardo.papermake.hook.commands.SubCommand
import com.rikonardo.papermake.hook.utils.FakeConsoleSender
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap

import org.bukkit.plugin.SimplePluginManager
import java.lang.reflect.Field


object PMakeConsole: SubCommand {
    override val usage = listOf(
        "<command>" to "Execute command as console",
    )

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size <= 1)
            return getKnownCommands().keys.toList().filter { it.startsWith(args[0]) }
        val command = Bukkit.getServer().getPluginCommand(args[0]) ?:
        getKnownCommands().entries.find { it.key == args[0] }?.value ?: return emptyList()
        return try {
            command.tabComplete(Bukkit.getConsoleSender(), args[0], args.drop(1).toTypedArray())
        } catch (e: Exception) {
            // Using player CommandSender because of a bug with console sender
            command.tabComplete(sender, args[0], args.drop(1).toTypedArray())
        }
    }

    override fun onCommand(sender: CommandSender, args: Array<out String>) {
        val fakeConsole = FakeConsoleSender(Bukkit.getConsoleSender(), sender)
        Bukkit.dispatchCommand(fakeConsole, args.joinToString(" "))
    }

    @Suppress("UNCHECKED_CAST")
    private fun getKnownCommands(): Map<String, Command> {
        val spm = Bukkit.getPluginManager() as SimplePluginManager
        return try {
            val commandMap: Field = SimplePluginManager::class.java.getDeclaredField("commandMap")
            val knownCommands: Field = SimpleCommandMap::class.java.getDeclaredField("knownCommands")
            commandMap.isAccessible = true
            knownCommands.isAccessible = true
            knownCommands.get(commandMap.get(spm)) as Map<String, Command>
        } catch (e: Exception) {
            mapOf()
        }
    }
}
