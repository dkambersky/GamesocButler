package com.github.dkambersky.butlerbot.modules

import com.github.dkambersky.butlerbot.Module
import discord4j.core.`object`.entity.Guild
import discord4j.core.event.domain.Event
import reactor.core.publisher.Mono

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
class MooModule(guild: Guild? = null) : Module("moo", guild) {
    override fun process(e: Event): Mono<Void> {
//        if (e is MessageCreateEvent)
//            process(e)
        return Mono.empty()
    }
/*
    private val mooPattern = Regex("m+oo+")

    fun process(e: MessageCreateEvent) {

        val (author, contents) = (e.message.author.get()
                .asMember(e.guildId.get()).block() ?: return) to
                e.message.content.orElse("").toLowerCase()

        val channel = e.message.channel

        *//* Search for exact matches first *//*
        val msg =
                when (contents) {
                    "f" -> "${increaseRespect(author)} pays their respects."
                    "blep" -> "${increaseRespect(author)} pays their respects."
                    "respect" -> "${author.mention} has ${getRespect(author)} respects."
                    "top" -> getTopRespects()
                    "moo" -> "```         (__)\r\n         (oo)\r\n   /------\\/\r\n  / |    ||\r\n *  /\\---/\\\r\n    ~~   ~~\r\n....\"Have you mooed today?\"...```"
                    "harambe?" -> harambeStatus(e.message.guild.block())
                    "fortune" -> fortune()
                    else -> ""
                }

        if (msg != "") {
            println("Sending message with content [$msg]")
            channel.sendMessage(msg)
            return
        }

        *//* No exact match found, search for substrings *//*
        when {
            contents.contains("harambe") ->
                e.message.channel.sendMessage(harambe(e.guild.block()))

            mooPattern.matches(contents) -> {
                if (e.message.authorAsMember.block()?.id == e.client.selfId.get())
                    return

                    println("MOO MATCHES $contents")
                channel.sendMessage(mooBack(contents))
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

    private fun harambeStatus(guild: Guild): String {
    val serverNode = conf<Long>("moo", "harambe", guild.longID.toString())
            val streak = serverNode?.get("streak").toInt() ?: 0
            val last = serverNode?.get("last")?.longValue() ?: return "Harambe was never mentioned before!"


            *//* Time, in hours, since Harambe was last mentioned  *//*
            val hours = (System.currentTimeMillis() - last) / 360000

            return if (streak >= 0)
                "Current streak: $streak days. Harambe was last mentioned $hours hours ago."
            else
                "Harambe is forgotten :( Last mention was $hours hours ago."

    }


    *//* Harambe *//*
    private fun harambe(guild: Guild): String {

        val serverNode = conf<Long>("moo", "harambe", guild.id.asLong().toString())
        val streak = serverNode?.get("streak")?.asInt() ?: 0
        val last = serverNode?.get("last")?.longValue() ?: return "Harambe was never mentioned before!"


        *//* Time, in hours, since Harambe was last mentioned  *//*
        val hours = (System.currentTimeMillis() - last) / 360000


        if (conf("moo", "harambe", guild.longID.toString()) == null) {
            *//* Initialize new server *//*
            val obj = JsonNodeFactory.instance.objectNode().apply {
                put("streak", 0)
                put("max", 0)
                put("last", System.currentTimeMillis())
                put("number", 0)

            }
            setConfBranch(obj, "moo", "harambe", guild.longID.toString())
        }

        setConfBranch(LongNode(System.currentTimeMillis()), "moo", "harambe", guild.longID.toString(), "last")

        if (hours > 48) {
            setConfBranch(IntNode(0), "moo", "harambe", guild.longID.toString(), "streak")
            return "Days since Harambe was last mentioned: ${hours / 24} -> 0"
        }

        if (hours > 24) {
            setConfBranch(IntNode(streak + 1), "moo", "harambe", guild.longID.toString(), "streak")
            return "Current Harambe daily streak: $streak"
        }

        return ""

    }

    *//* Respects *//*
    private fun increaseRespect(author: Member): String {
        var respect = 1

        val branch = conf("moo", "respect", author.id.asLong().toString())
        if (branch is IntNode) respect = branch.intValue() + 1

        setConfBranch(IntNode(respect), "moo", "respect", author.id.asLong().toString())

        return author.nicknameMention
    }

    private fun getRespect(author: Member): Int = conf("moo", "respect", author.id.asLong().toString())?.intValue()
            ?: 0

    private fun getTopRespects(): String {
        val respects = conf<List<Pair<Long, Int>>>("moo", "respect")

        if (respects == null || !respects.fields().hasNext())
            return "Nobody pays any respects around here!"

        *//* Oh the laziness *//*
        return "Give people a leaderboard, and they will try to climb it.\n```" +
                respects.fields()
                        ?.asSequence()
                        ?.sortedWith(Comparator.comparingInt { it.value.intValue() })
                        ?.toList()
                        ?.reversed()
                        ?.fold("") { str, ele ->
                            val user = client.getUserById(Snowflake.of(ele.key.toLong()))
                            if (user != null)
                                "$str\n${user.block().username}: ${ele.value} respects"
                            else
                                str
                        } + "```"
    }*/
}