package moe.nea.ursa.statviewer

import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

object Scraper {

    var lastKeys = listOf<String>()
        private set
    val jedis = Util.getRedis()
    val connection = Util.getConnection()
    val logger = LoggerFactory.getLogger("Scraper")

    fun scrapeOnce() {
        logger.info("Starting scraping")
        val keys = jedis.keys("hypixel:accumulated:*")
        lastKeys = keys.toList()
        val timestamp = System.currentTimeMillis()
        for (key in keys) {
            val value = jedis.get(key)?.toLongOrNull() ?: continue
            val statement =
                connection.prepareStatement("INSERT INTO metrics (`key`, `value`, `timestamp`) VALUES (?,?,?);")
            statement.setString(1, key)
            statement.setLong(2, value)
            statement.setLong(3, timestamp)
            statement.execute()
        }
        logger.info("Finished scraping $keys")
    }

    fun startScraper() {
        thread(name = "Scrape-Thread") {
            scrapeLoop()
        }
    }

    fun scrapeLoop() {
        while (true) {
            try {
                scrapeOnce()
            } catch (e: Exception) {
                logger.error("Error during scraping", e)
            }
            val target = (System.currentTimeMillis() / 600000 + 1) * 600000
            do {
                val time = target - System.currentTimeMillis()
                if (time > 0) {
                    Thread.sleep(time)
                } else
                    break
            } while (true)
        }
    }

}