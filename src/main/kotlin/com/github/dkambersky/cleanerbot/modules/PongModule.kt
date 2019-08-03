package com.github.dkambersky.cleanerbot.modules

import com.github.dkambersky.cleanerbot.Module
import com.github.dkambersky.cleanerbot.util.messageBack
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono


/* Just a class to ascertain stuff is working correctly */
class PongModule : Module("pong") {
    override fun process(e: Event): Mono<Void> {
        if (e !is MessageCreateEvent)
            return Mono.empty()

        if (e.message.content.get() == "!ping")
             e.messageBack("Pong!")

        return Mono.empty()
    }
}