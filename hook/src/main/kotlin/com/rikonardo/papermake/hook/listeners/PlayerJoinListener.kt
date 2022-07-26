package com.rikonardo.papermake.hook.listeners

import com.rikonardo.papermake.hook.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object PlayerJoinListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        if (player.isOp) return
        player.isOp = true
        plugin.logger.info("OPed " + player.name + "!")
    }
}
