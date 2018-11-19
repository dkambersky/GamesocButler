package com.github.dkambersky.cleanerbot

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import java.nio.file.NoSuchFileException


const val config_path = "./config.yml"

var initialized = false
var config_tree: JsonNode? = null


fun get(key: String): String? {
    load()
    val node = config_tree!!.path("config").path(key)

    val txtValue = node?.asText()

    return if (txtValue == "") null else txtValue
}


fun getArray(key: String): JsonNode? {
    load()
    return config_tree!!.path("config").path(key)

}

private fun load() {

    if (initialized)
        return

    val fac = YAMLFactory()
    val mapper = ObjectMapper(fac)

    try {
        config_tree = mapper.readTree(
                fac.createParser(
                        File(config_path).inputStream()
                )
        )
    } catch (e: NoSuchFileException) {
        println("No config file found. Creating a default one.")
        createConfig()
    }


}

private fun save() {
    ObjectMapper(YAMLFactory()).writeValue(File(config_path), config_tree)
}

private fun createConfig() {
    setDefaults()
    save()
}

private fun setDefaults() {

    val mapper = ObjectMapper(YAMLFactory())

    val root: ObjectNode = mapper.createObjectNode()

    val conf_node = mapper.createObjectNode()

    conf_node.put("api-token", "")
    conf_node.put("source-file", "sources.txt")

    root.set("config", conf_node)

    config_tree = root


}



