package com.github.dkambersky.cleanerbot


/**
 * Main listener class.
 * This would probably look nicer with the annotation approach,
 * but hey, I like interfaces.
 */
abstract class Module(private val name: String) : Command {
    fun name() = name

    open fun defaults() = mapOf<String, String>()
}