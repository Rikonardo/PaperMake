package com.rikonardo.papermake.hook.commands

import com.rikonardo.papermake.hook.commands.pmake.PMakeConsole
import com.rikonardo.papermake.hook.commands.pmake.PMakeInfo
import com.rikonardo.papermake.hook.commands.pmake.PMakePlugin
import com.rikonardo.papermake.hook.commands.pmake.PMakeReload
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object PMakeCommand : CommandExecutor, TabCompleter {
    private val subcommands = mapOf(
        "info" to PMakeInfo,
        "reload" to PMakeReload,
        "console" to PMakeConsole,
        "plugin" to PMakePlugin
    )

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String> {
        return if (args.isEmpty()) subcommands.keys.toList()
        else if (args.size == 1) subcommands.keys.toList().filter { it.startsWith(args[0]) }
        else {
            if (subcommands.containsKey(args.first())) subcommands[args.first()]!!.onTabComplete(
                sender, args.drop(1).toTypedArray()
            )
            else emptyList()
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, alias: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("§d----- §b[PaperMake] §d-----§r\n" + subcommands.entries.map { cmd ->
                cmd.value.usage.map {
                    "§b/pmake §a${cmd.key}${
                        if (it.first != null) " " + it.first!!
                            .replace("<", "§2<")
                            .replace("[", "§2[")
                            .replace(">", ">§a")
                            .replace("]", "]§a")
                        else ""
                    }§7: §f${it.second}§r"
                }
            }.flatten().joinToString("\n"))
        } else {
            if (subcommands.containsKey(args.first())) {
                subcommands[args.first()]!!.onCommand(sender, args.drop(1).toTypedArray())
            } else {
                sender.sendMessage("§b[PaperMake]§r §cUnknown subcommand§r: ${args.first()}")
            }
        }
        return true
    }
}
