package pro.schmid.sbbtsp.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI
import java.time.Instant

class Database {
    init {
        val dbUri = URI(System.getenv("DATABASE_URL"))

        val split = dbUri.userInfo.split(":")
        val username: String = split[0]
        val password: String = split[1]

        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://${dbUri.host}:${dbUri.port}${dbUri.path}"
            this.username = username
            this.password = password
            maximumPoolSize = 20
            validate()
        }

        val dataSource = HikariDataSource(config)

        Database.connect(dataSource)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(Connections)
        }
    }

    suspend fun get(from: String, to: String): Connection? = dbquery {
        Connection.find {
            Connections.fromStation eq from and (Connections.toStation eq to)
        }.firstOrNull()
    }

    suspend fun create(from: String, to: String, min: Int, median: Int): Connection = dbquery {
        Connection.new {
            fromStation = from
            toStation = to
            minDuration = min
            medianDuration = median
            lastDownload = Instant.now().epochSecond
        }
    }
}

private suspend fun <T> dbquery(block: () -> T): T = suspendedTransactionAsync(Dispatchers.IO) {
    block()
}.await()
