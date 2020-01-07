package com.github.dkambersky.butlerbot.modules

import com.github.dkambersky.butlerbot.Module
import com.github.dkambersky.butlerbot.util.messageBack
import discord4j.core.`object`.entity.Guild
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono


/* Just a module to ascertain stuff is working correctly */
class PongModule(guild: Guild) : Module("pong", guild) {
    override fun process(e: Event): Mono<Void> {
        if (e !is MessageCreateEvent)
            return Mono.empty()

        if (e.message.content.orElse("") == "!ping")
             e.messageBack("Pong!")

        if (e.message.content.orElse("") == "!guild")
            e.messageBack("You're in ${guild.name}")

        return Mono.empty()
    }
}