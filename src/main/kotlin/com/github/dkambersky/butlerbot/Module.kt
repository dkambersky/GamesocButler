package com.github.dkambersky.butlerbot

import com.github.dkambersky.butlerbot.modules.GameRoleModule
import com.uchuhimo.konf.UnsetValueException
import com.uchuhimo.konf.toValue
import discord4j.core.`object`.entity.Guild


/**
 * Main listener class.
 * This would probably look nicer with the annotation approach,
 * but hey, I like interfaces.
 */
abstract class Module(private val name: String, val guild: Guild) : Command {
    fun name() = name
    fun db() = db.at(name()).apply {
        addSpec(GameRoleModule.Companion)
    }


    open fun defaults() = mapOf<String, String>()

    inline fun <reified T> get(vararg keys: String) = db
            .at(name()).at(guild.id.asString()).at(keys.joinToString(".")).toValue<T>()

    inline fun <reified T : Any?> conf(
            vararg keys: String,
            ignoreGuild: Boolean = false
    ): T? = try {
        if (ignoreGuild) config.at(name()).at(keys.joinToString(".")).toValue()
        else config.at(name()).at(guild.id.asString()).at(keys.joinToString(".")).toValue()
    } catch (e: UnsetValueException) {
        null
    }


    inline fun <reified T : Any?> db(
            vararg keys: String,
            ignoreGuild: Boolean = false
    ): T? = try {
        if (ignoreGuild) db.at(name()).at(keys.joinToString(".")).toValue()
        else db.at(name()).at(guild.id.asString()).at(keys.joinToString(".")).toValue()
    } catch (e: UnsetValueException) {
        null
    }

    inline fun <reified T : Any> dbSave(
            content: T,
            vararg keys: String,
            ignoreGuild: Boolean = false
    ) {
        try {
            if (ignoreGuild) db.at(name()).at(keys.dropLast(1).joinToString("."))[keys.last()] = content
            else db.at(name()).at(guild.id.asString()).at(keys.dropLast(1).joinToString("."))[keys.last()] = content
        } catch (e: UnsetValueException) {
            e.printStackTrace()
        }
    }


}
