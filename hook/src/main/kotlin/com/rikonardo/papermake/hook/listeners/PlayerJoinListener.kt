package com.rikonardo.papermake.hook.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class PlayerJoinListener(private val plugin: JavaPlugin) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(e: PlayerJoinEvent) {
        e.player.isOp = true
        plugin.logger.info("OPed " + e.player.name + "!")
    }

}
