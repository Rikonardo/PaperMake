package com.rikonardo.papermake.hook.commands

import org.bukkit.command.CommandSender

interface SubCommand {
    val usage: List<Pair<String?, String>>
    fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String>
    fun onCommand(sender: CommandSender, args: Array<out String>)
}
