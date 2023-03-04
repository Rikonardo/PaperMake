package com.rikonardo.papermake.hook.utils

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.PluginCommand
import org.bukkit.command.SimpleCommandMap
import org.bukkit.event.Event
import org.bukkit.plugin.*
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.net.URLClassLoader
import java.util.*

/*
 * Based on: https://github.com/ryan-clancy/PlugMan/blob/master/src/main/java/com/rylinaux/plugman/util/PluginUtil.java
 * ---
 * Copyright (C) 2010 - 2014 PlugMan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 */
@Suppress("UNCHECKED_CAST", "DuplicatedCode")
object PluginUtilOld: PluginUtil {
    override fun unload(plugin: Plugin): Boolean {
        val name: String = plugin.name
        val pluginManager: PluginManager? = Bukkit.getPluginManager()
        var commandMap: SimpleCommandMap? = null
        var plugins: MutableList<Plugin?>? = null
        var names: MutableMap<String?, Plugin?>? = null
        var commands: MutableMap<String?, Command>? = null
        var listeners: Map<Event?, SortedSet<RegisteredListener?>?>? = null
        var reloadlisteners = true
        if (pluginManager != null) {
            pluginManager.disablePlugin(plugin)
            try {
                val pluginsField: Field = Bukkit.getPluginManager().javaClass.getDeclaredField("plugins")
                pluginsField.isAccessible = true
                plugins = pluginsField.get(pluginManager) as MutableList<Plugin?>?
                val lookupNamesField: Field = Bukkit.getPluginManager().javaClass.getDeclaredField("lookupNames")
                lookupNamesField.isAccessible = true
                names = lookupNamesField.get(pluginManager) as MutableMap<String?, Plugin?>?
                try {
                    val listenersField: Field = Bukkit.getPluginManager().javaClass.getDeclaredField("listeners")
                    listenersField.isAccessible = true
                    listeners = listenersField.get(pluginManager) as Map<Event?, SortedSet<RegisteredListener?>?>?
                } catch (e: Exception) {
                    reloadlisteners = false
                }
                val commandMapField: Field = Bukkit.getPluginManager().javaClass.getDeclaredField("commandMap")
                commandMapField.isAccessible = true
                commandMap = commandMapField.get(pluginManager) as SimpleCommandMap?
                val knownCommandsField: Field = SimpleCommandMap::class.java.getDeclaredField("knownCommands")
                knownCommandsField.isAccessible = true
                commands = knownCommandsField.get(commandMap) as MutableMap<String?, Command>?
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        pluginManager!!.disablePlugin(plugin)
        if (plugins != null && plugins.contains(plugin)) plugins.remove(plugin)
        if (names != null && names.containsKey(name)) names.remove(name)
        if (listeners != null && reloadlisteners) {
            for (set in listeners.values) {
                val it: MutableIterator<RegisteredListener> = set!!.iterator() as MutableIterator<RegisteredListener>
                while (it.hasNext()) {
                    val value = it.next()
                    if (value.plugin === plugin) {
                        it.remove()
                    }
                }
            }
        }
        if (commandMap != null) {
            val it: MutableIterator<Map.Entry<String?, Command>> = commands!!.entries.iterator()
            while (it.hasNext()) {
                val (_, value) = it.next()
                if (value is PluginCommand) {
                    val c = value
                    if (c.plugin === plugin) {
                        c.unregister(commandMap)
                        it.remove()
                    }
                }
            }
        }

        // Attempt to close the classloader to unlock any handles on the plugin's jar file.
        val cl: ClassLoader = plugin.javaClass.classLoader
        if (cl is URLClassLoader) {
            try {
                val pluginField: Field = cl.javaClass.getDeclaredField("plugin")
                pluginField.isAccessible = true
                pluginField.set(cl, null)
                val pluginInitField: Field = cl.javaClass.getDeclaredField("pluginInit")
                pluginInitField.isAccessible = true
                pluginInitField.set(cl, null)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            try {
                cl.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }

        // Will not work on processes started with the -XX:+DisableExplicitGC flag, but lets try it anyway.
        // This tries to get around the issue where Windows refuses to unlock jar files that were previously loaded into the JVM.
        System.gc()
        return true
    }

    override fun load(file: File): Plugin? {
        if (!file.isFile) return null
        val target = try {
            Bukkit.getPluginManager().loadPlugin(file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }!!
        target.onLoad()
        Bukkit.getPluginManager().enablePlugin(target)
        return target
    }
}
