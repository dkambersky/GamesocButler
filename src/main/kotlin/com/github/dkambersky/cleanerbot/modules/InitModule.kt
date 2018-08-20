package com.github.dkambersky.cleanerbot.modules

import com.github.dkambersky.cleanerbot.client
import com.github.dkambersky.cleanerbot.get
import com.github.dkambersky.cleanerbot.sendMsg
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType


/**
 * com.github.dkambersky.cleanerbot.Module which handles the initial triggers for the bot
 */
class InitModule : Module() {

    override val name = "init"

    override fun handleReady() {
        super.handleReady()
        client.changePresence(StatusType.ONLINE, ActivityType.PLAYING, "whack-a-mole w/ Pokécord")

        try {
            val channelID = get("master-channel-id") as Long
            sendMsg(client.getChannelByID(channelID), "MurderBot online!", 5000)
        } catch (e: Exception) {
            println("Please specify master-channel-id for the bot to work properly.")
        }


    }

}