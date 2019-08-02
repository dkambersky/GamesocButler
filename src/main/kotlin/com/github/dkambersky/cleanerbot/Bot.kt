package com.github.dkambersky.cleanerbot

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import org.reflections.Reflections
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/* --- Globals --- */
/* Discord client for interfacing with the API */
lateinit var client: DiscordClient

/* Whether Discord API is ready */
var ready = false

/* Executor for timed tasks */
val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()


/* Bot for GameSoc; cleans up after Pokecord and manages game-related groups and happenings
 * TODO rework the unholy abomination that is the config code
 */
fun main(args: Array<String>) {
    login()


    if (!::client.isInitialized)
        return

    /* Oh Jackson */
    val enabledModules: Set<String> =
            getArray("enabled-modules")
                    ?.iterator()
                    ?.asSequence()
                    ?.map { it.textValue() }
                    ?.toMutableSet()
                    ?.apply { add("init") }
                    ?: throw Exception("Please enable at least one module.")


    Reflections("com.github.dkambersky.cleanerbot")
            /* Find all modules */
            .getSubTypesOf(Module::class.java)

            /* Map them to their declared name */
            .map {
                /* This is potentially wasteful as it briefly creates an instance of even the unused modules
                 * However, the other ways to deal with this are either
                 *  - adding annotations to the mix
                 *  - adding companion objects everywhere
                 *  Both of which are pretty ugly. Thank Java's reflection and Kotlin's 'static' weirdness.
                 *  The modules are pretty much free to instantiate so ¯\_(ツ)_/¯
                 */
                val instance = it.constructors.first().newInstance()
                val name = it.superclass.getMethod("name")
                        .invoke(instance) as String
                name to instance
            }

            /* Filter by the ones enabled in config */
            .filter { enabledModules.contains(it.first) }

            /* Finally, register them */
            .forEach {}




    client.eventDispatcher
            .on(ReadyEvent::class.java)
            .map { it.guilds.size }
            .flatMap {
                client.eventDispatcher.on(GuildCreateEvent::class.java)
                        .take(it.toLong()).last()
            }
            .subscribe {
                ready = true
            }

}

fun login() {
    val token = get("api-token")
            ?: throw Exception("Please specify an API token in config.yml!")

    client = DiscordClientBuilder(token)
            .build()

    client.login().block()
}


fun sendMsg(channel: MessageChannel, message: String): Message? {
    return if (message != "") channel.createMessage(message).block() else null
}

fun sendMsg(channel: MessageChannel, message: String, timeout: Long): Message? {
    val msg = sendMsg(channel, message)
    executor.schedule({ msg?.delete() }, timeout, TimeUnit.MILLISECONDS)
    return msg

}
