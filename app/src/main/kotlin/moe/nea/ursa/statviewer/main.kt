package moe.nea.ursa.statviewer

import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Main")
    logger.info("Running with ${args.toList()}.")
    File("data").mkdirs()
    logger.info("Does database exist: ${Util.getDatabaseFile().exists()}")
    if (!Util.getDatabaseFile().exists()) {
        logger.warn("Database not found. Creating new database")
        val init = Util.getResource("/init.sql")
        if (init == null) {
            logger.error("Could not read init.sql")
            exitProcess(1)
        }
        val sql = init.readAllBytes().decodeToString()
        Util.getConnection().use {
            it.createStatement()
                .execute(sql)
        }
        logger.info("Database initialized")
    }
    Scraper.startScraper()
    DiscordBot.start()
}