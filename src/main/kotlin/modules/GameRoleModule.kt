package modules

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

/**
  Role module skeleton
 */
class GameRoleModule: Module() {
    override val name = "Game Role"

    override fun handleMessageReceived(e: MessageReceivedEvent) {
        val author = e.message.author

        if(e.message.content =="assign"){
            e.message.channel.sendMessage("Hey ${e.message.author}, what roles do you want assigned?")


        }
    }
}