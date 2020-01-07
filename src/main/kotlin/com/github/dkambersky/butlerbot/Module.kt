package com.github.dkambersky.butlerbot

import com.uchuhimo.konf.toValue
import discord4j.core.`object`.entity.Guild


/**
 * Main listener class.
 * This would probably look nicer with the annotation approach,
 * but hey, I like interfaces.
 */
abstract class Module(private val name: String, val guild: Guild) : Command {
    fun name() = name


    open fun defaults() = mapOf<String, String>()

    inline fun <reified T> get(vararg keys: String) = db
            .at(name()).at(guild.id.asString()).at(keys.joinToString(".")).toValue<T>()

    inline fun <reified T> conf(vararg keys: String, ignoreGuild: Boolean = false): T {
        println("GETTING ${keys.joinToString()} $ignoreGuild")
        println("Arguments: ${name()} || ${guild.id.asString()} || ${keys.joinToString(".")}")
        return if (ignoreGuild) config.at(name()).at(keys.joinToString(".")).toValue()
        else config.at(name()).at(guild.id.asString()).at(keys.joinToString(".")).toValue()
    }

}