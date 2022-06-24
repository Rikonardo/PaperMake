package com.rikonardo.papermake.hook.commands.pmake

import com.rikonardo.papermake.hook.commands.SubCommand
import com.rikonardo.papermake.hook.utils.RunData
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

object PMakeInfo: SubCommand {
    override val usage = listOf(
        null to "Environment information",
    )

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return listOf()
    }

    override fun onCommand(sender: CommandSender, args: Array<out String>) {
        val time = (System.currentTimeMillis() - RunData.papermakeHookStarted) / 1000
        val seconds = time % 60
        val minutes = (time / 60) % 60
        val hours = time / 3600
        sender.sendMessage("""
            §d----- §b[PaperMake] §d-----§r
            §fPaperMake v§e${RunData.papermakeVersion}§r
            §fRunning on JDK: §e${RunData.jdkVersion}§f, Gradle §e${RunData.gradleVersion}§r
            §fHost OS: §e${RunData.osName} (${RunData.osArch})§f, v§e${RunData.osVersion}§r
            §fServer: §e${Bukkit.getServer().name}§r
            §fServer version: §e${Bukkit.getServer().version}§r
            §fProject group: §e${RunData.projectGroup}§r
            §fProject name: §e${RunData.projectName}§r
            §fProject version: §e${RunData.projectVersion}§r
            §fRunning for ${"§e$hours §fhours §e$minutes §fminutes §e$seconds §fseconds"}§r
        """.trimIndent())
    }
}
