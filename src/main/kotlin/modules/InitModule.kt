package modules

import client
import io.get
import sendMsg


/**
 * modules.Module which handles the initial triggers for the bot
 */
class InitModule : Module() {
    override val name = "init"

    override fun handleReady() {
        super.handleReady()
        client.changePlayingText("whack-a-mole w/ Pokécord")

        try {
            val channelID = get("master-channel-id") as Long
            sendMsg(client.getChannelByID(channelID), "MurderBot online!", 5000)
        } catch (e: Exception) {
            println("Please specify master-channel-id for the bot to work properly.")
        }


    }

}