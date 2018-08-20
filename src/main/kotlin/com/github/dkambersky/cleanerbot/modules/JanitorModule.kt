package com.github.dkambersky.cleanerbot.modules


import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.RequestBuffer


/* ID of Pokécord's user acct to detect messages */
const val POKECORD_ID = 365975655608745985L

/* Time in miliseconds to wait before deleting Pokécord dialogue */
const val DELETE_TIMER = 5000L



class JanitorModule : Module() {
    override val name = "Janitor"

    private val dirtyByChannel = mutableMapOf<Long, MutableList<IMessage>>()

    override fun handleMessageReceived(e: MessageReceivedEvent) {
        val channelID = e.channel.longID
        val msg = e.message

        if (msg.fromPokecord()) {

            if (msg.embeds.isNotEmpty() && msg.embeds.first().title == "\u200C\u200CA wild pokémon has appeared!") {
                println("Main embed")
                dirtyByChannel.getOrPut(channelID) { mutableListOf() }.add(msg)
                return
            }

            if (msg.content.contains("Congratulations")) {
                println("Starting timer to delete messages")
                launch {
                    delay(DELETE_TIMER)
                    dirtyByChannel[channelID]?.forEach { RequestBuffer.request { it.delete()} }
                    dirtyByChannel.remove(channelID)
                    println("Deleted offending messages")
                }
                return
            }

        }

        if (isRelevantDialog(msg))
            dirtyByChannel[channelID]?.add(msg)

    }

    private fun isRelevantDialog(msg: IMessage): Boolean {
        return dirtyByChannel.containsKey(msg.channel.longID) &&
                (msg.content.startsWith("p!catch ")
                        || (msg.fromPokecord()) && msg.content.contains("wrong pokémon"))
    }

}

private fun IMessage.fromPokecord() = this.author.longID == POKECORD_ID
