package pro.schmid.sbbtsp.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.ds.PGPoolingDataSource
import java.net.URI
import java.time.Instant

class Database {
    init {
        val dbUri = URI(System.getenv("DATABASE_URL"))

        val split = dbUri.userInfo.split(":")
        val username: String = split[0]
        val password: String = split[1]


        val pool = PGPoolingDataSource()// PGConnectionPoolDataSource()
        pool.serverName = dbUri.host
        pool.portNumber = dbUri.port
        pool.databaseName = dbUri.path.substring(1)
        pool.user = username
        pool.password = password
        if (dbUri.host != "localhost") {
            pool.sslMode = "require"
        }

        Database.connect(pool)

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