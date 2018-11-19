package com.github.dkambersky.cleanerbot.modules


import com.github.dkambersky.cleanerbot.Module
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sx.blah.discord.api.events.Event
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.RequestBuffer


/* ID of Pokécord's user acct to detect messages */
const val POKECORD_ID = 365975655608745985L

/* Time in miliseconds to wait before deleting Pokécord dialogue */
const val DELETE_TIMER = 5000L

/* Whether to log every little thing */
const val FINE_LOGGING = false


class JanitorModule : Module("janitor") {

    private val dirtyByChannel = mutableMapOf<Long, MutableList<IMessage>>()

    override fun process(e: Event) {
        if (e !is MessageReceivedEvent)
            return

        val channelID = e.channel.longID
        val msg = e.message

        if (msg.fromPokecord()) {
            fine("Msg from pokecord! ${e.message.content}")
            if (msg.embeds.isNotEmpty() && msg.embeds.first().title == "\u200C\u200CA wild pokémon has appeared!") {
                dirtyByChannel.getOrPut(channelID) { mutableListOf() }.add(msg)
                return
            }
            if (msg.content.contains("Congratulations")) {


            GlobalScope.launch{
                    delay(DELETE_TIMER)
                    dirtyByChannel[channelID]?.forEach {
                        fine("Trying to delete msg with text [$it]")
                        RequestBuffer.request { it.delete() }
                    }
                    dirtyByChannel.remove(channelID)
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
fun fine(msg: String) {
    if (FINE_LOGGING)
        println(msg)
}
