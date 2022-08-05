package com.rikonardo.papermake.hook.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.util.CachedServerIcon

class ServerListPingListener(private val icon: CachedServerIcon) : Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onPing(e: ServerListPingEvent) {
        e.setServerIcon(icon)
    }

}