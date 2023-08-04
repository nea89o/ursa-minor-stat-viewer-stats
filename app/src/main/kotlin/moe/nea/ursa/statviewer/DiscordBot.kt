package moe.nea.ursa.statviewer

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.utils.FileUpload
import java.time.Instant
import java.time.temporal.ChronoUnit

object DiscordBot : ListenerAdapter() {
    lateinit var jda: JDA
    override fun onReady(event: ReadyEvent) {
        jda.updateCommands()
            .addCommands(
                Commands.slash("metrics", "Show server metrics")
                    .addSubcommands(
                        SubcommandData("total", "Total requests").addOption(
                            OptionType.STRING,
                            "key",
                            "Which key you want to query",
                            false,
                            true
                        ),
                        SubcommandData("delta", "Requests per minute").addOption(
                            OptionType.STRING,
                            "key",
                            "Which key you want to query",
                            false,
                            true
                        )
                    )
            ).queue()
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (event.name == "metrics" && event.focusedOption.name == "key") {
            event.replyChoiceStrings(Scraper.lastKeys).queue()
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == "metrics") {
            val key = event.getOption("key")?.asString
            val isDeltas = event.subcommandName == "delta"
            event.deferReply().queue {
                val graphStart = Instant.now().minus(1, ChronoUnit.DAYS)
                val keysToQuery = key?.split(",") ?: Scraper.lastKeys
                var graphPoints = keysToQuery.map { Pair(it, GraphCreator.queryGraphPoints(it, graphStart)) }
                if (isDeltas) {
                    graphPoints = graphPoints.map { Pair(it.first, GraphCreator.calculateGraphDeltas(it.second)) }
                }
                val graph = GraphCreator.renderGraph(
                    GraphCreator.createGraphFromPoints(
                        if (isDeltas) "$key requests per minute over time"
                        else ("total $key requests over time"),
                        graphPoints.toMap(),
                        if (isDeltas) "requests / minute"
                        else "requests"
                    )
                )
                it.sendFiles(FileUpload.fromData(graph, "graph.png")).queue()
            }
        }
    }

    fun start() {
        jda = JDABuilder.createLight(Util.getToken())
            .addEventListeners(DiscordBot)
            .build()
    }
}