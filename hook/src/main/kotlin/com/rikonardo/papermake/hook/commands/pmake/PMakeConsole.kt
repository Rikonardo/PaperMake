package com.rikonardo.papermake.hook.commands.pmake

import com.rikonardo.papermake.hook.commands.SubCommand
import com.rikonardo.papermake.hook.utils.FakeConsoleSender
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

object PMakeConsole: SubCommand {
    override val usage = listOf(
        "<command>" to "Execute command as console",
    )

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return listOf()
    }

    override fun onCommand(sender: CommandSender, args: Array<out String>) {
        val fakeConsole = FakeConsoleSender(Bukkit.getConsoleSender(), sender)
        Bukkit.dispatchCommand(fakeConsole, args.joinToString(" "))
    }
}
