package com.github.dkambersky.butlerbot

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.yaml
import com.uchuhimo.konf.toValue


object ConfAA : ConfigSpec("config") {
    val apiToken by optional<String?>(null)
    val dbPath by optional("database_yml")
    val sourceFile by optional("sources_txt")
//    val enabledModules by required<List<String>>()
}


val db = Config().from.yaml.watchFile("database.yml")
val config = Config().from.yaml.watchFile("config.yml")

inline fun <reified A> config(vararg keys: String) = config.at("config").at(keys.joinToString(".")).toValue<A>()

fun setConfBranch(value: Any, vararg keys: String) = config.set(keys[0], value)