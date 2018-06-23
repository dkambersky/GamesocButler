package modules

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent


const val POKECORD_ID = 365975655608745985L

class JanitorModule : Module() {


    override val name = "Janitor"

    override fun handleMessageReceived(e: MessageReceivedEvent) {

        if (e.message.author.longID == POKECORD_ID) {
            e.message.channel.sendMessage("Detected Pokecord spam!")


        }
    }
}