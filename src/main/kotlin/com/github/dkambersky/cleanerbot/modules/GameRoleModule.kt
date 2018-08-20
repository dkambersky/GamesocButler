package com.github.dkambersky.cleanerbot.modules

import com.github.dkambersky.cleanerbot.getConfBranch
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RoleBuilder
import java.awt.Color
import java.util.*

/**
 * Game Role module
 * TODOs:
 *  [.] assign and remove game roles on demand
 *  [ ] ensure we only touch the right roles
 *  [ ] primitive event scheduler?
 *  [ ] autogreet w/ game list
 */
class GameRoleModule : Module() {
    override val name = "Game Role"
    private val rolePrefix: String? = getConfBranch("game_role", "prefix")?.textValue()
    private val roleSuffix = getConfBranch("game_role", "suffix")?.textValue()
    private val rolesSupported: List<String>? = getConfBranch("game_role", "games")?.map { it.textValue() }?.toMutableList()
    private val scheduleTimer = Timer()


    override fun handleMessageReceived(e: MessageReceivedEvent) {
        val author = e.message.author

        val tokens = e.message.content.split(" ")

        val roleName = if (
                (rolePrefix != null && tokens[2].regionMatches(0, rolePrefix, 0, rolePrefix.length)) &&
                (roleSuffix != null && tokens[2].regionMatches(0, roleSuffix, 0, roleSuffix.length))
        )
            tokens[2]
        else
            "$rolePrefix${tokens[2]}$roleSuffix"

        when (tokens[1].toLowerCase()) {
        /* Handle both affixed and non-affixed identifiers */
            "join"
            -> {
                if (canManageRole(tokens[2]))
                    author.addRole(e.guild.getRolesByName(tokens[2]).firstOrNull())
            }
            "part" -> {
                author.removeRole(e.guild.getRolesByName(tokens[2]).firstOrNull())
            }
            "addgame" -> {
                if (e.guild.getRolesByName(roleName).size > 0)
                    messageBack("Requested role already exists!")

                val role = RoleBuilder(e.guild)
                        .setMentionable(true)
                        .setHoist(false)
                        .withColor(Color.BLUE)
                        .withName(roleName)
                        .build()
                e.guild.roles.add(role)

            }
            "removegame" -> {
                if (canManageRole(roleName))
                    e.guild.getRolesByName(tokens[2]).firstOrNull()?.delete()
            }

        }


    }

    private fun canManageRole(roleName: String): Boolean {
        return rolesSupported?.contains(roleName) ?: true
    }

    init {
        println("Game Role module initializing. Data: $rolePrefix, $rolesSupported, $roleSuffix")
    }

}