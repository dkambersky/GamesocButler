package com.github.dkambersky.cleanerbot.modules

import com.github.dkambersky.cleanerbot.Module
import sx.blah.discord.api.events.Event
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent


/* Just a class to ascertain stuff is working correctly */
class PongModule : Module("pong") {
    override fun process(e: Event) {
        if (e !is MessageReceivedEvent)
            return
        if (e.message.content == "!ping")
            e.message.channel.sendMessage("Pong!")
    }
}