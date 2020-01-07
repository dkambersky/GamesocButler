package com.github.dkambersky.butlerbot.modules

import kotlin.reflect.KClass

class ModuleUtils {
    companion object {
        /* Placeholder until I figure out a nicer way with reflection */
        fun asModule(name: String): KClass<*>? {
            return when (name) {
                "game-role" -> GameRoleModule::class
                "moo" -> MooModule::class
                "janitor" -> JanitorModule::class
                "pong" -> PongModule::class
                else -> null
            }
        }
    }
}