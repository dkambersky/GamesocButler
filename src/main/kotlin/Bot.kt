import io.get
import modules.InitModule
import modules.JanitorModule
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * CleanerBot - cleans up after Pokecord's spam
 *
 * Base taken from murderbot
 */
val client = login()

/* For timed tasks */
val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()


fun main(args: Array<String>) {
    client.dispatcher.registerListener(InitModule())
    client.dispatcher.registerListener(JanitorModule())

}


fun login(): IDiscordClient {

    val builder = ClientBuilder()

    val token = get("api-token") as String
    if (token == "") {
        throw Exception("Please specify an API token in config.yml!")
    }

    builder.withToken(token)

    try {
        println("Logging in with token: ${builder.token}")
        return builder.login()
    } catch (e: Exception) {
        println("Error occurred while logging in!")
        e.printStackTrace()

    }

    /* IDEA complains whether this piece of unreachable
        code is here or not. Oh well */
    return null!!
}

fun sendMsg(channel: IChannel, message: String): IMessage {
    return channel.sendMessage((message))
}

fun sendMsg(channel: IChannel, message: String, timeout: Long): IMessage {
    val msg = sendMsg(channel, message)
    executor.schedule({ msg.delete() }, timeout, TimeUnit.MILLISECONDS)

    return msg

}