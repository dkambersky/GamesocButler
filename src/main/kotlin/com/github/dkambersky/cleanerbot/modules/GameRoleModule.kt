package com.github.dkambersky.cleanerbot.modules

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.dkambersky.cleanerbot.Module
import com.github.dkambersky.cleanerbot.client
import com.github.dkambersky.cleanerbot.getConfBranch
import com.github.dkambersky.cleanerbot.setConfBranch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sx.blah.discord.api.events.Event
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.PermissionUtils
import sx.blah.discord.util.RoleBuilder
import java.awt.Color
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

    private val greetingEnabled: Boolean = getConfBranch("game-role", "greeting-enabled")?.textValue()?.toBoolean()
            ?: true
    private val autoAssignRole: Long? = getConfBranch("game-role", "auto-assigned-role-id")?.textValue()?.toLongOrNull()
    private val rolePrefix: String? = getConfBranch("game-role", "prefix")?.textValue()
    private val roleSuffix = getConfBranch("game-role", "suffix")?.textValue()
    private val enableMentions = getConfBranch("game-role", "enableMentions")?.textValue()?.toBoolean() ?: false
    private val rolesManaged: MutableList<String>? = getConfBranch("game-role", "roles-managed")?.map { it.textValue() }?.toMutableList()
    private val BOT_CHANNEL = getConfBranch("game-role", "bot-channel")?.textValue()?.toLongOrNull()
            ?: GAMESOC_BOT_CHANNEL
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
    private val dirtyByChannel = mutableMapOf<Long, MutableList<IMessage>>()

    /* Never, ever touch these roles */
    private val roleBlacklist = listOf(
            "Social Secretary | Matteo Kekboi",
            "President | Gurg",
            "Server Owner",
            "Treasurer | Moneybags",
            "Secretary | Massive Hyena",
            "LANMaster | The Sun God",
            "Server Admin | Not a furry"
    )


    private val memeResponses = mapOf(
            "Social Secretary | Matteo Kekboi" to "WHO THE HELL DO YOU THINK I AM?",
            "President | Gurg" to "You really should reconsider.",
            "Server Owner" to "https://www.youtube.com/watch?v=PsCKnxe8hGY",
            "Treasurer | Moneybags" to "Sorry, Mayonnaise is the only one allowed to touch the money",
            "Secretary | Massive Hyena" to "Last time I checked your profile picture wasn't a fucking Hyena. Beep. Boop.",
            "LANMaster | The Sun God" to "Saaaaaaraaaaaaaaaaaaaaaaa",
            "Server Admin | Not a furry" to "https://www.youtube.com/watch?v=R6wbTpBja9w"
    )

    override fun process(e: Event) {
        when (e) {
            is MessageReceivedEvent -> {
                process(e)
            }
            is UserJoinEvent -> {
                process(e)
            }
        }
    }


    private fun process(e: UserJoinEvent) {
        if (!greetingEnabled) {
            println("Greeting disabled. The conf? ${getConfBranch("game-role", "greeting-enabled")?.textValue()}")
            return
        }
        val channel = e.guild.getChannelByID(BOT_CHANNEL)
                ?: e.guild.getChannelsByName("bot_hell").firstOrNull()
                ?: e.guild.defaultChannel

        println("Default channel is ${e.guild.getChannelsByName("general").firstOrNull()}, bot channel is ${channel.name}")

        if (autoAssignRole != null)
            e.user.addRole(
                    e.guild.getRoleByID(autoAssignRole)
            )

        e.guild.getChannelsByName("general").firstOrNull()?.sendMessage(
                "Welcome, ${e.user.mention()}!\n" +
                        "If you'd like to play games with us, go to ${channel.mention()} and register yourself for games you're interested in.\n" +
                        "You can see available commands with `-help` (but please keep it out of ${e.guild.getChannelsByName("general").firstOrNull()?.mention()}).")
    }

    private fun process(e: MessageReceivedEvent) {
        /* Meme section */
        val text = e.message.content
        if (text == "!help") {
            e.messageBack("HELP IS NOT COMING...")
            return
        }

        if (commandPrefix != null &&
                !e.message.content.regionMatches(0, commandPrefix, 0, commandPrefix.length))
            return

        val author = e.message.author
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

        if (roleName == "CCC")
            roleName = "Chill Chat Clan"

        if (argsFull != "" &&
                memeResponses.keys.none { roleName == it } &&
                roleName != "Chill Chat Clan" &&
                !canManageRole(roleName) &&
                command != "addgame" &&
                command != "removegame"
        ) {
            if (knownCommands.any { it == command }) {
                e.messageBack("That isn't a game role.")
            }
            println("Returning boi")
            return
        }
        val role = e.guild.getRolesByName(roleName).firstOrNull()


        /* Gigantic permissions debug part */
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


        if (!canRole2) {
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
                if (role?.name == "Chill Chat Clan") {
                    if (e.author.hasRole(role)) {
                        e.messageBack("You're already chill!")
                        return
                    }

                    val newNick = (author.getNicknameForGuild(e.message.guild)
                            ?: author.getDisplayName(e.message.guild)).removePrefix("[CCC] ")

                    var failed = false
                    try {
                        e.guild.setUserNickname(author, "[CCC] $newNick")
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
                if (role?.name == "Chill Chat Clan") {
                    if (!e.author.hasRole(role)) {
                        e.messageBack("You're not chill in the first place!")
                        return
                    }

                    e.messageBack("You're no longer chill.")
                    author.removeRole(role)
                    val newNick = (author.getNicknameForGuild(e.message.guild)
                            ?: author.getDisplayName(e.message.guild)).removePrefix("[CCC] ")
                    try {
                        e.guild.setUserNickname(author, newNick)
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
                if (!e.author.canAdmin(e.guild)) {
                    e.messageBack("No can do boss.")
                    return
                }
                if (role != null) {
                    println("already exists: $role")
                    e.messageBack("That game role already exists!")
                    return
                }

                RoleBuilder(e.guild)
                        .setMentionable(true)
                        .setHoist(false)
                        .withColor(Color.ORANGE)
                        .withName(roleName)
                        .build()
                        .apply { e.guild.roles.add(this) }


                e.messageBack("Game added!")
                rolesManaged?.add(roleName)
                saveRoles()
            }
            "removegame" -> {
                if (!e.author.canAdmin(e.guild)) {
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
                val users = e.guild.getUsersByRole(role).map { if (enableMentions) it.mention() else it.getDisplayName(e.guild) }
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

    private fun IUser.canAdmin(guild: IGuild): Boolean {

        return moderatorRoleIds.any {
            val role: IRole? = guild.getRoleByID(it)
            role != null && this.hasRole(role)
        } || moderatorUserIds.any { this.longID == it }

    }


    private fun MessageReceivedEvent.messageBack(message: String) {
        enqueueForDeletion(this.channel.sendMessage(message))
    }

    private fun saveRoles() {

        val node = JsonNodeFactory.instance.arrayNode().apply {
            rolesManaged?.forEach { add(it) }
        }
        println("Saving roles w/ node $node")
        setConfBranch(node, "game-role", "roles-managed")
    }

    private fun enqueueForDeletion(msg: IMessage) {
        if (!DELETION_ENABLED)
            return

        GlobalScope.launch {
            delay(DELETE_TIMER)
            msg.delete()
        }
    }

    private fun canManageRole(roleName: String): Boolean {
        return true
//                .and(!roleBlacklist.any { it == roleName })
                .and(rolesManaged?.contains(roleName) ?: true)

    }


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


