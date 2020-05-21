package pro.schmid.sbbtsp.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class Database {
    init {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "helloworld"
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