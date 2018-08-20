package com.github.dkambersky.cleanerbot.modules

import com.github.dkambersky.cleanerbot.ready
import sx.blah.discord.api.events.Event
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent


/**
 * Main listener class.
 * This would probably look nicer with the annotation approach,
 *  but hey, I like interfaces.
 */
abstract class Module : IListener<Event> {
    companion object {
        @JvmField
        val name: String = ""
    }


    override fun handle(e: Event?) {

        /* Wait for ReadyEvent */
        if (e is ReadyEvent) handleReady()
        if (!ready) return


        /* Handle correct event type */
        when (e) {
            is MessageReceivedEvent -> handleMessageReceived(e)
            /* here be any more events we wanna handle */
        }
    }


    open fun handleMessageReceived(e: MessageReceivedEvent) {

    }

    open fun handleReady() {
        ready = true
    }


    protected fun messageBack(message: String) {

    }
}