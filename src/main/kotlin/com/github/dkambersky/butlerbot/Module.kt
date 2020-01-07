package com.github.dkambersky.butlerbot

import discord4j.core.`object`.entity.Guild


/**
 * Main listener class.
 * This would probably look nicer with the annotation approach,
 * but hey, I like interfaces.
 */
abstract class Module(private val name: String, val guild: Guild) : Command {
    fun name() = name



    open fun defaults() = mapOf<String, String>()


    inline fun <reified T : Any?> db(vararg keys: String): T = keys as T
    inline fun <reified T : Any?> conf(vararg keys: String): T = keys as T


}

