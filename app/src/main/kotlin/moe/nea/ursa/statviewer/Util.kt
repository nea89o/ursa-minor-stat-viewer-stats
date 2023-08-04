package moe.nea.ursa.statviewer

import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import java.io.File
import java.sql.DriverManager

object Util {
    val constant = LoggerFactory.getLogger("Constant")
    fun getToken() = System.getenv("TOKEN")
    fun getDatabaseFile() = File("data/database.db").absoluteFile
    fun getConnection() = DriverManager.getConnection("jdbc:sqlite:${getDatabaseFile()}").also {
        constant.info("Connecting to database at ${getDatabaseFile()}")
    }

    fun getRedis() = Jedis(System.getenv("REDIS_HOST"), (System.getenv("REDIS_PORT").toInt()))
}