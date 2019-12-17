package com.github.dkambersky.butlerbot.util

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

/* Blocking extension functions bc no reactive stuff today */
fun Mono<Guild>.getUsersByRole(role: Role): List<Member> {
    return (block() ?: return listOf()).members
            .filter { it.roles.any { it.id == role.id }.block() ?: false }
            .collectList().block() ?: listOf()

}

fun MessageChannel.sendMessage(message: String) = createMessage(message).block()
fun Member.hasRole(role: Role) = roles.any { it.id == role.id }.block() ?: false
fun Member.hasRole(role: Snowflake) = roles.any { it.id == role }.block() ?: false
fun Member.addRole(role: Role) = addRole(role.id).block()
fun Member.removeRole(role: Role) = removeRole(role.id).block()
fun setUserNickname(member: Member, name: String) =
        member.edit { it.setNickname(name) }.block()

fun MessageCreateEvent.messageBack(message: String) {
    /* TODO autodelete */
    this.message.channel.block()?.createMessage(message)?.block()
}

fun Mono<MessageChannel>.sendMessage(message: String) = block()?.sendMessage(message)

var Guild.longID: Long
    get() = id.asLong()
    set(value) {}