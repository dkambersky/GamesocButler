package com.github.dkambersky.butlerbot.modules

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.dkambersky.butlerbot.Module
import com.github.dkambersky.butlerbot.db
import com.github.dkambersky.butlerbot.setConfBranch
import com.github.dkambersky.butlerbot.util.*
import discord4j.core.`object`.entity.GuildMessageChannel
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.guild.MemberJoinEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import reactor.core.publisher.Mono
import java.util.*

/**
 * Game Role module
 * TODOs:
 *  [x] assign and remove game roles on demand
 *  [x] ensure we only touch the right roles
 *  [x] autogreet w/ game list
 *  [x] permissions system
 *  Stretch goals
 *  [ ] primitive event scheduler?
 *  [x] memes
 */


class GameRoleModule : Module("game-role") {
    override fun process(e: Event): Mono<Void> {
        when (e) {
            is MessageCreateEvent -> process(e)
            is MemberJoinEvent -> process(e)
        }

        return Mono.empty()
    }

    private val GAMESOC_BOT_CHANNEL = 266274174450794496L // 484444732399812620L

    private val moderatorRoleIds = listOf(
            /* GS committee */
            252162169658015756,
            /* D Overlord*/
            484788867191537666,
            /* GS mods*/
            163649141438808064
    )

    private val moderatorUserIds = listOf(
            /* Davefin */
            108979387755401216
    )

    private val greetingEnabled = db("game-role", "greeting-enabled") ?: false
    private val autoAssignRole = db<Long?>("game-role", "auto-assigned-role-id")
    private val rolePrefix: String? = db("game-role", "prefix")
    private val roleSuffix = db<String>("game-role", "suffix")
    private val enableMentions = db("game-role", "enableMentions") ?: false
    private val rolesManaged: MutableList<String>? = db<MutableList<String?>>("game-role", "roles-managed")
            ?.toMutableList().mapNotNull { it.toString() }.toMutableList()
    private val BOT_CHANNEL = db("game-role", "bot-channel") ?: GAMESOC_BOT_CHANNEL
    private val DELETION_ENABLED = false

    /* The whole permission logic needs _major_ cleanup lol */
    val knownCommands = listOf(
            "help",
            "join",
            "leave",
            "addgame",
            "removegame",
            "list",
            "listgames"
    )


    /* Gamesoc server specific constants */
    private val commandPrefix: String = "-"
    private val scheduleTimer = Timer()

    /* Time in miliseconds to wait before deleting dialogue */
    val DELETE_TIMER = 10000L


    private val memeResponses = mapOf(
            "Social Secretary | Matteo Kekboi" to "WHO THE HELL DO YOU THINK I AM?",
            "President | Gurg" to "You really should reconsider.",
            "Server Owner" to "https://www.youtube.com/watch?v=PsCKnxe8hGY",
            "Treasurer | Moneybags" to "Sorry, Mayonnaise is the only one allowed to touch the money",
            "Secretary | Massive Hyena" to "Last time I checked your profile picture wasn't a fucking Hyena. Beep. Boop.",
            "LANMaster | The Sun God" to "Saaaaaaraaaaaaaaaaaaaaaaa",
            "Server Admin | Not a furry" to "https://www.youtube.com/watch?v=R6wbTpBja9w"
    )


    private fun process(e: MemberJoinEvent) {
        if (!greetingEnabled) {
            println("Greeting disabled. The conf? ${db<Boolean?>("game-role", "greeting-enabled")}")
            return
        }

        val guild = e.guild.block() ?: return
        val user = e.member ?: return

        val botChannel = guild.channels.filter {
            it.id.asLong() == BOT_CHANNEL
                    || it.name.startsWith("bot")
        }.blockFirst()

        val welcomeChannel = guild.channels.filter {
            it.name.startsWith("general")
        }.blockFirst() as GuildMessageChannel

        if (autoAssignRole != null)
            user.addRole(Snowflake.of(autoAssignRole)).block()

        welcomeChannel.sendMessage(
                "Welcome, ${user.mention}!\n" +
                        "If you'd like to play games with us, go to ${botChannel?.mention} and register yourself for games you're interested in.\n" +
                        "You can see available commands with `-help` (but please keep it out of ${welcomeChannel.mention}).")
    }

    private fun process(e: MessageCreateEvent) {
        /* Meme section */
        val text = e.message.content.orElse("")
        if (text == "!help") {
            e.messageBack("HELP IS NOT COMING...")
            return
        }

        if (commandPrefix != null &&
                !text.regionMatches(0, commandPrefix, 0, commandPrefix.length))
            return

        val author = e.message.author.get().asMember(e.guild.block()?.id ?: return).block() ?: return
//        val tokens = e.message.content.removePrefix(commandPrefix).sub
        val command = text.removePrefix(commandPrefix).substringBefore(" ").toLowerCase()
        val argsFull = text.substringAfter(" ", "")

        /* Handle both affixed and non-affixed identifiers */
        var roleName = if (
                (rolePrefix != null && argsFull.regionMatches(0, rolePrefix, 0, rolePrefix.length)) &&
                (roleSuffix != null && argsFull.regionMatches(0, roleSuffix, 0, roleSuffix.length))
        )
            argsFull
        else
//            "$rolePrefix${tokens[1]}$roleSuffix"
            argsFull


        println("Wroking with role [$roleName]")
        if (roleName == "CCC")
            roleName = "Chill Chat Clan"

        if (argsFull != "" &&
                memeResponses.keys.none { roleName == it } &&
                roleName != "Chill Chat Clan" &&
                !canManageRole(roleName) &&
                command != "addgame" &&
                command != "removegame"
        ) {
            if (knownCommands.none { it == command }) {
                e.messageBack("That isn't a game role.")
            }
            println("Returning boi")
            return
        }

        val role = e.guild.block()!!.roles.filter { it.name == roleName }.blockFirst()
        /*


        *//* Gigantic permissions debug part *//*
        val canNick = PermissionUtils.hasHierarchicalPermissions(
                e.guild,
                client.ourUser,
                e.author,
                Permissions.CHANGE_NICKNAME
        )
        val canRole = PermissionUtils.hasHierarchicalPermissions(
                e.guild,
                client.ourUser,
                e.author,
                Permissions.MANAGE_ROLES
        )

        val isHigher = PermissionUtils.isUserHigher(
                e.guild,
                client.ourUser,
                e.author
        )

        val canRole2 = role == null || PermissionUtils.isUserHigher(
                e.guild,
                client.ourUser,
                listOf(role)
        )

        fine("Can manage nicknames? $canNick; Is higher? $isHigher; Can manage role? $canRole; Is higher in roles? $canRole2 ")
*/

        /* TODO role hierarchy stuff */
        if (false) {
            e.messageBack("I'm not allowed to manage that role.")
            return
        }

        when (command) {
            "help" -> {
                e.messageBack(
                        "Available commands:\n" +
                                "join\nleave\naddgame\nremovegame\nlistgames\nlist <game>"
                )
            }
            "join" -> {
                if (role.name == "Chill Chat Clan") {
                    if (e.member.get().roles.any { it.id == role.id }.block() == true) {
                        e.messageBack("You're already chill!")
                        return
                    }

                    val newNick = (author.nickname.orElse(author.displayName))
                            .removePrefix("[CCC] ")


                    var failed = false
                    try {
                        setUserNickname(author, "[CCC] $newNick")
                    } catch (e: Exception) {
                        println("Permissions bad?")
                        failed = true
                        e.printStackTrace()
                    }
                    e.messageBack("Welcome to the ruling class. ${if (failed) "Change the nick yourself, I'm not allowed to." else ""}")
                    author.addRole(role)

                    enqueueForDeletion(e.message)
                    return
                }

                memeResponses.filter { it.key == argsFull }.entries.firstOrNull()?.value?.apply {
                    e.messageBack(this)
                    return
                }
                if (role != null) {
                    if (!author.hasRole(role)) {
                        author.addRole(role)
                        e.messageBack("You've been enlisted!")
                    } else {
                        e.messageBack("You're already registered!")
                    }
                } else
                    e.messageBack("That game role doesn't exist :(")
            }
            "leave" -> {
                if (role.name == "Chill Chat Clan") {
                    if (!author.hasRole(role)) {
                        e.messageBack("You're not chill in the first place!")
                        return
                    }

                    e.messageBack("You're no longer chill.")
                    author.removeRole(role)
                    val newNick = (author.nickname.orElse(author.displayName))
                            .removePrefix("[CCC] ")
                    try {
                        setUserNickname(author, newNick)
                    } catch (e: Exception) {
                        println("Permissions bad?")
                        e.printStackTrace()
                    }

                    enqueueForDeletion(e.message)
                    return
                }


                if (role != null) {
                    if (author.hasRole(role)) {
                        author.removeRole(role)
                        e.messageBack("You've been removed.")
                    } else {
                        e.messageBack("Cheeky.")
                    }

                } else
                    e.messageBack("That game role doesn't exist :(")

            }
            "addgame" -> {
                if (author.canAdmin()) {
                    e.messageBack("No can do boss.")
                    return
                }
                if (role != null) {
                    println("already exists: $role")
                    e.messageBack("That game role already exists!")
                    return
                }

//
//                Role(e.guildId,)
//                        .setMentionable(true)
//                        .setHoist(false)
//                        .withColor(Color.ORANGE)
//                        .withName(roleName)
//                        .build()
//                        .apply { e.guild.roles.add(this) }


                e.messageBack("Game added!")
                rolesManaged?.add(roleName)
                saveRoles()
            }
            "removegame" -> {
                if (author.canAdmin()) {
                    e.messageBack("No can do boss.")
                    return
                }

                if (role != null) {
                    role.delete()
                    rolesManaged?.remove(roleName)
                    e.messageBack("Game removed.")
                    saveRoles()
                } else {
                    e.messageBack("That's not a real role ¯\\_(ツ)_/¯")
                }

            }
            "list" -> {
                if (role == null) {
                    if (argsFull != "")
                        e.messageBack("That's not a real role ¯\\_(ツ)_/¯")
                    else
                        e.messageBack("Currently available games:\n${rolesManaged?.joinToString(", ")}")

                    return
                }
                val users = e.guild.getUsersByRole(role).map { if (enableMentions) it.mention else it.displayName }
                e.messageBack("People playing ${role.name}:\n${users.joinToString(", ")}")
            }
            "listgames" -> {
                e.messageBack("Currently available games:\n${rolesManaged?.joinToString(", ")}")
            }
            else -> {
//                e.messageBack("Unrecognized command :(")
                return
            }
        }
        enqueueForDeletion(e.message)

    }

    private fun Member.canAdmin(): Boolean {
        return moderatorRoleIds.any { modId ->
            guild.block()!!.roleIds.any {
                it.asLong() == modId
                        && hasRole(it)
            }
        } || moderatorUserIds.any { this.id.asLong() == it }
    }

    private fun saveRoles() {
        val node = JsonNodeFactory.instance.arrayNode().apply {
            rolesManaged?.forEach { add(it) }
        }
        println("Saving roles w/ node $node")
        setConfBranch(node, "game-role", "roles-managed")
    }

    private fun enqueueForDeletion(msg: Message) {
        if (!DELETION_ENABLED)
            return

        GlobalScope.launch {
            delay(DELETE_TIMER)
            msg.delete()
        }
    }

    private fun canManageRole(roleName: String): Boolean = rolesManaged?.contains(roleName) ?: false


    override fun defaults() = mapOf(
            "rolePrefix" to "",
            "roleSuffix" to "",
            "rolesManaged" to ""
    )

    init {
        /*rolePrefix = get("rolePrefix")*/
        println("Game Role module initializing. Data: $rolePrefix, $rolesManaged, $roleSuffix")
    }
}



