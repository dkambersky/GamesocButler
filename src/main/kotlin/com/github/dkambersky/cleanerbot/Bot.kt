package com.github.dkambersky.cleanerbot

import com.github.dkambersky.cleanerbot.modules.GameRoleModule
import com.github.dkambersky.cleanerbot.modules.InitModule
import com.github.dkambersky.cleanerbot.modules.JanitorModule
import com.github.dkambersky.cleanerbot.modules.Module
import com.sun.org.apache.xml.internal.security.Init
import org.reflections.Reflections
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * CleanerBot - cleans up after Pokecord's spam
 *
 * Base taken from murderbot
 */
lateinit var client: IDiscordClient

/* For timed tasks */
val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()


fun main(args: Array<String>) {
    login()

    if (!::client.isInitialized)
        return

    val enabledModules = getConfBranch("enabled-modules")
            ?.toList() ?: listOf<String>()

    println("EnabledModules: $enabledModules")



    Reflections("com.github.dkambersky.cleanerbot")
            /* Find all modules */
            .getSubTypesOf(Module::class.java)

            /* Map them to their declared name */
            .map { it.getDeclaredField("name") .get(String) to it }

            /* Filter by the ones enabled in config */
            .filter { enabledModules.contains(it.first) }

            /* Finally, register them */
            .forEach {
                client.dispatcher.registerListener(it.second.constructors.first().newInstance())
            }




    enabledModules.forEach { client.dispatcher.registerListener(it) }

}


fun login() {

    val builder = ClientBuilder()

    val token = get("api-token") as String
    if (token == "") {
        throw Exception("Please specify an API token in config.yml!")
    }

    builder.withToken(token)

    try {
        println("Logging in with token: ${builder.token}")
        client = builder.login()
    } catch (e: Exception) {
        println("Error occurred while logging in!")
        e.printStackTrace()
    }


}

fun sendMsg(channel: IChannel, message: String): IMessage {
    return channel.sendMessage((message))
}

fun sendMsg(channel: IChannel, message: String, timeout: Long): IMessage {
    val msg = sendMsg(channel, message)
    executor.schedule({ msg.delete() }, timeout, TimeUnit.MILLISECONDS)

    return msg

}