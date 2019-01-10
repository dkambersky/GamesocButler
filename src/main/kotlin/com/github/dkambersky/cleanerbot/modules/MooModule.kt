package com.github.dkambersky.cleanerbot.modules

import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.LongNode
import com.github.dkambersky.cleanerbot.*
import khttp.get
import sx.blah.discord.api.events.Event
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IUser
import java.util.*

/**
 * Quick little module mimicking moobot, to get a hang of the API
 *
 * Features:
 * + f
 * + respect
 * + top
 * + moo
 * + harambe w/ status
 * . fortune - probably not coming, not without a *nix system :(
 *     - currently using a public API ¯\_(ツ)_/¯ well, gluing a fortune and cowsay API together
 */
class MooModule : Module("moo") {
    private val mooPattern = Regex("m*o*")

    override fun process(e: Event) {
        if (e !is MessageReceivedEvent)
            return

        val (author, contents) = e.message.author to e.message.content.toLowerCase()
        val channel = e.message.channel

        /* Search for exact matches first */
        val msg =
                when (contents) {
                    "f" -> "${increaseRespect(author)} pays their respects."
                    "respect" -> "$author has ${getRespect(author)} respect."
                    "top" -> getTopRespects()
                    "moo" -> "```         (__)\r\n         (oo)\r\n   /------\\/\r\n  / |    ||\r\n *  /\\---/\\\r\n    ~~   ~~\r\n....\"Have you mooed today?\"...```"
                    "harambe?" -> harambeStatus()
                    "fortune" -> fortune()
                    else -> ""
                }

        if (msg != "") {
            sendMsg(channel, msg)
            return
        }

        /* No exact match found, search for substrings */
        when {
            contents.contains("harambe") ->
                sendMsg(e.message.channel, harambe())

            mooPattern.matches(contents) -> {
                sendMsg(channel, mooBack(contents))
            }
        }


    }

    private fun fortune() =
            try {
                val fortune = get("http://yerkee.com/api/fortune").jsonObject.getString("fortune")
                get("http://cowsay.morecode.org/say", params = mapOf("message" to fortune, "format" to "text"))
                        .text.let { "```$it```" }
            } catch (e: Exception) {
                "Our fortune teller is currently unavailable :("
            }


    private fun mooBack(contents: String) =
            "m".repeat(contents.count { it == 'm' } * 2) +
                    "o".repeat(contents.count { it == 'o' } * 2)

    private fun harambeStatus(): String {

        val streak = getConfBranch("harambe", "streak")?.asInt() ?: 0
        val last = getConfBranch("harambe", "last")?.longValue() ?: return "Harambe was never mentioned before!"


        /* Time, in hours, since Harambe was last mentioned  */
        val hours = (last - System.currentTimeMillis()) / 360000

        return if (streak >= 0)
            "Current streak: $streak days. Harambe was last mentioned $hours hours ago."
        else
            "Harambe is forgotten :( Last mention was $hours hours ago."
    }


    /* Harambe */
    private fun harambe(): String {

        val streak = getConfBranch("harambe", "streak")?.intValue() ?: 0

        /* Time, in hours, since Harambe was last mentioned  */
        val hours = (getConfBranch("harambe", "last")!!.longValue() - System.currentTimeMillis()) / 360000

        setConfBranch(LongNode(System.currentTimeMillis()), "harambe", "last")

        if (hours > 48) {
            setConfBranch(IntNode(0), "harambe", "streak")
            return "Days since Harambe was last mentioned: ${hours / 24} -> 0"
        }

        if (hours > 24) {
            setConfBranch(IntNode(streak + 1), "harambe", "streak")
            return "Current Harambe daily streak: $streak"
        }

        return ""

    }

    /* Respects */
    private fun increaseRespect(author: IUser): IUser {

        var respect = 1

        val branch = getConfBranch("respect", author.longID.toString())
        if (branch is IntNode) respect = branch.intValue() + 1

        setConfBranch(IntNode(respect), "respect", author.longID.toString())

        return author
    }

    private fun getRespect(author: IUser): Int = getConfBranch("respect", author.longID.toString())?.intValue() ?: 0

    private fun getTopRespects(): String {
        val respects = getConfBranch("respect")

        if (respects == null || !respects.fields().hasNext())
            return "Nobody pays any respects around here!"

        /* Oh the laziness */
        return "Here's a list:\n```" +
                respects.fields()
                        ?.asSequence()
                        ?.sortedWith(Comparator.comparingInt { it.value.intValue() })
                        ?.toList()
                        ?.reversed()
                        ?.fold("") { str, ele ->
                            "$str\n${client.getUserByID(ele.key.toLong()).name}: ${ele.value}"
                        } + "```"
    }
}