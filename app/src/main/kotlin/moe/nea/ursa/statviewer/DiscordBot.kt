package moe.nea.ursa.statviewer

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload
import java.time.Instant
import java.time.temporal.ChronoUnit

object DiscordBot : ListenerAdapter() {
    lateinit var jda: JDA
    override fun onReady(event: ReadyEvent) {
        jda.updateCommands()
            .addCommands(
                Commands.slash("metrics", "Show server metrics")
                    .addOption(OptionType.STRING, "key", "Which key you want to query", true, true)
            ).queue()
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (event.fullCommandName == "metrics" && event.focusedOption.name == "key") {
            event.replyChoiceStrings(Scraper.lastKeys).queue()
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.fullCommandName == "metrics") {
            val key = event.getOption("key")!!.asString
            event.deferReply().queue {
                val graphStart = Instant.now().minus(1, ChronoUnit.DAYS)
                val graph = GraphCreator.renderGraph(
                    GraphCreator.createGraphFromPoints(
                        "total $key requests over time",
                        GraphCreator.queryGraphPoints(key, graphStart)
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