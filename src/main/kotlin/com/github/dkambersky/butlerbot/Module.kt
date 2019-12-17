package com.github.dkambersky.butlerbot

import com.uchuhimo.konf.toValue
import discord4j.core.`object`.entity.Guild


/**
 * Main listener class.
 * This would probably look nicer with the annotation approach,
 * but hey, I like interfaces.
 */
abstract class Module(private val name: String, val guild: Guild? = null) : Command {
    fun name() = name


    open fun defaults() = mapOf<String, String>()

    inline fun <reified A> get(vararg keys: String) = db
            .at(name()).at(guild!!.id.asString()).at(keys.joinToString(".")).toValue<A>()

    inline fun <reified A> conf(vararg keys: String, ignoreGuild: Boolean = false): A {
        println("GETTING ${keys.joinToString()} $ignoreGuild")
        return if (ignoreGuild) config.at(name()).at(keys.joinToString(".")).toValue()
        else config.at(name()).at(guild!!.id.asString()).at(keys.joinToString(".")).toValue()
    }

}