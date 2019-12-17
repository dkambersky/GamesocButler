package com.github.dkambersky.butlerbot.modules

import com.github.dkambersky.butlerbot.Module
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import reactor.core.publisher.Mono


/* ID of Pokécord's user acct to detect messages */
const val POKECORD_ID = 365975655608745985L

/* Time in miliseconds to wait before deleting Pokécord dialogue */
const val DELETE_TIMER = 5000L

/* Whether to log every little thing */
const val FINE_LOGGING = true


class JanitorModule : Module("janitor") {
    override fun process(e: Event): Mono<Void> {
        if (e is MessageCreateEvent)
            process(e)

        return Mono.empty()
    }

    private val dirtyByChannel = mutableMapOf<Long, MutableList<Message>>()

    fun process(e: MessageCreateEvent) {
        println("WHOO")
        fine("Processing MessageCreate - janitor")
        val channelID = e.message.channelId.asLong()
        val msg = e.message

        if (msg.fromPokecord()) {
            fine("Msg from pokecord! ${e.message.content}")
            if (msg.embeds.isNotEmpty() && msg.embeds.first().title.get() == "\u200C\u200CA wild pokémon has appeared!") {
                dirtyByChannel.getOrPut(channelID) { mutableListOf() }.add(msg)
                return
            }
            if (msg.content.orElse("").contains("Congratulations")) {
                GlobalScope.launch {
                    delay(DELETE_TIMER)
                    dirtyByChannel[channelID]?.forEach {
                        fine("Trying to delete msg with text [$it]")
                        it.delete()
                    }
                    dirtyByChannel.remove(channelID)
                }
                return
            }
        }

        if (isRelevantDialog(msg))
            dirtyByChannel[channelID]?.add(msg)

    }

    private fun isRelevantDialog(msg: Message): Boolean {
        return dirtyByChannel.containsKey(msg.channelId.asLong()) &&
                (msg.content.orElse("").startsWith("p!catch ")
                        || (msg.fromPokecord()) && msg.content.orElse("").contains("wrong pokémon"))
    }

}

private fun Message.fromPokecord() = this.author.get().id.asLong() == POKECORD_ID
fun fine(msg: String) {
    if (FINE_LOGGING)
        println(msg)
}
