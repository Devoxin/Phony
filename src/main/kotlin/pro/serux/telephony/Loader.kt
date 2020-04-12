package pro.serux.telephony

import me.devoxin.flight.api.CommandClient
import me.devoxin.flight.api.CommandClientBuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import pro.serux.telephony.audio.DCAReader
import pro.serux.telephony.audio.OggOpusReader
import pro.serux.telephony.listeners.EmptyVcListener
import pro.serux.telephony.listeners.FlightEventAdapter
import pro.serux.telephony.parsers.memberorempty.Member
import pro.serux.telephony.parsers.memberorempty.MemberOrEmpty
import kotlin.system.exitProcess

object Loader {

    val bootTime = System.currentTimeMillis()

    var isDebug = false
        private set

    lateinit var shardManager: ShardManager
    lateinit var commandClient: CommandClient

    @ExperimentalStdlibApi
    @JvmStatic
    fun main(args: Array<String>) {
        isDebug = args.any { it == "--debug" }
        val token = Config["token"]
        val prefix = if (isDebug) "))" else ")"

        DCAReader.loadDcaFile("tone.dca")

        commandClient = CommandClientBuilder()
            .setPrefixes(prefix)
            .registerDefaultParsers()
            .addCustomParser(java.lang.Long::class.java, LongParser())
            .addCustomParser(Member::class.java, MemberOrEmpty())
            .addEventListeners(FlightEventAdapter())
            .configureDefaultHelpCommand { showParameterTypes = true }
            .setIgnoreBots(false)
            .build()

        shardManager = DefaultShardManagerBuilder().apply {
            addEventListeners(commandClient, EmptyVcListener())
            setActivity(Activity.watching("phones ring | )help"))
            setToken(token)
            setShardsTotal(-1)
        }.build()

        commandClient.registerCommands("pro.serux.telephony.commands")
    }

}