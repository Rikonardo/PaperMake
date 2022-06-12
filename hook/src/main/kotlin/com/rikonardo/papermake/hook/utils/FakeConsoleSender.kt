package com.rikonardo.papermake.hook.utils

import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.conversations.Conversation
import org.bukkit.conversations.ConversationAbandonedEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin

class FakeConsoleSender(
    private val sender: ConsoleCommandSender, private val player: CommandSender
) : ConsoleCommandSender {
    override fun isOp() = sender.isOp

    override fun setOp(value: Boolean) {
        sender.isOp = value
    }

    override fun isPermissionSet(name: String?) = sender.isPermissionSet(name)

    override fun isPermissionSet(perm: Permission?) = sender.isPermissionSet(perm)

    override fun hasPermission(name: String?) = sender.hasPermission(name)

    override fun hasPermission(perm: Permission?) = sender.hasPermission(perm)

    override fun addAttachment(plugin: Plugin?, name: String?, value: Boolean): PermissionAttachment {
        return sender.addAttachment(plugin, name, value)
    }

    override fun addAttachment(plugin: Plugin?): PermissionAttachment {
        return sender.addAttachment(plugin)
    }

    override fun addAttachment(plugin: Plugin?, name: String?, value: Boolean, ticks: Int): PermissionAttachment {
        return sender.addAttachment(plugin, name, value, ticks)
    }

    override fun addAttachment(plugin: Plugin?, ticks: Int): PermissionAttachment {
        return sender.addAttachment(plugin, ticks)
    }

    override fun removeAttachment(attachment: PermissionAttachment?) {
        sender.removeAttachment(attachment)
    }

    override fun recalculatePermissions() {
        sender.recalculatePermissions()
    }

    override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> = sender.effectivePermissions

    override fun sendMessage(message: String?) {
        player.sendMessage(message)
    }

    override fun sendMessage(messages: Array<out String>?) {
        player.sendMessage(messages)
    }

    override fun getServer(): Server {
        return sender.server
    }

    override fun getName(): String {
        return sender.name
    }

    override fun spigot(): CommandSender.Spigot {
        return sender.spigot()
    }

    override fun isConversing(): Boolean {
        return sender.isConversing
    }

    override fun acceptConversationInput(input: String?) {
        sender.acceptConversationInput(input)
    }

    override fun beginConversation(conversation: Conversation?): Boolean {
        return sender.beginConversation(conversation)
    }

    override fun abandonConversation(conversation: Conversation?) {
        sender.abandonConversation(conversation)
    }

    override fun abandonConversation(conversation: Conversation?, details: ConversationAbandonedEvent?) {
        sender.abandonConversation(conversation, details)
    }

    override fun sendRawMessage(message: String?) {
        player.sendMessage(message)
    }
}
