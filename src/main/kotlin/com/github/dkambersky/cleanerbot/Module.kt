package com.github.dkambersky.cleanerbot

import sx.blah.discord.api.events.Event
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType


/**
 * Main listener class.
 * This would probably look nicer with the annotation approach,
 * but hey, I like interfaces.
 */
abstract class Module(private val name: String) : IListener<Event> {
    fun name() = name

    override fun handle(e: Event?) {
        /* Wait for ReadyEvent */
        if (!ready && e !is ReadyEvent) return
        process(e ?: return)
    }

    open fun process(e: Event) {}
    open fun defaults() = mapOf<String, String>()
}


/**
 * Inititialization module - required for any interactions with Discord's API
 */
class InitModule : Module("init") {
    override fun process(e: Event) {
        if (e !is ReadyEvent)
            return

        ready = true
        client.changePresence(StatusType.ONLINE, ActivityType.PLAYING, "whack-a-mole w/ Pokécord")
    }
}