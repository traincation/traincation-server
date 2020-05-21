package pro.schmid.sbbtsp.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI
import java.time.Instant

class Database {
    init {
        val dbUri = URI(System.getenv("DATABASE_URL"))
        
        val split = dbUri.userInfo.split(":")
        val username: String = split[0]
        val password: String = split[1]
        val ssl = if (dbUri.host != "localhost") "?sslmode=require" else ""
        val dbUrl = "jdbc:postgresql://${dbUri.host}:${dbUri.port}${dbUri.path}$ssl"

        Database.connect(
            url = dbUrl,
            driver = "org.postgresql.Driver",
            user = username,
            password = password
        )

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(Connections)
        }
    }

    fun get(from: String, to: String): Connection? {
        return transaction {
            Connection.find {
                Connections.fromStation eq from and (Connections.toStation eq to)
            }.firstOrNull()
        }
    }

    fun create(from: String, to: String, min: Int, median: Int): Connection {
        return transaction {
            Connection.new {
                fromStation = from
                toStation = to
                minDuration = min
                medianDuration = median
                lastDownload = Instant.now().epochSecond
            }
        }
    }
}