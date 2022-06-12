package com.rikonardo.papermake.hook.commands.pmake

import com.rikonardo.papermake.hook.HookManager
import com.rikonardo.papermake.hook.commands.SubCommand
import org.bukkit.command.CommandSender

object PMakeReload: SubCommand {
    override val usage = listOf(
        null to "Reload developed plugin${if (HookManager.loaded.size > 1) "s" else ""}",
    )

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return listOf()
    }

    override fun onCommand(sender: CommandSender, args: Array<out String>) {
        HookManager.reload(previous = true)
    }
}
