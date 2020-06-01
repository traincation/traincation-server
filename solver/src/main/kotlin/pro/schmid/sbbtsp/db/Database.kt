package pro.schmid.sbbtsp.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
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
            // Heroku Postgres free allows 20 connections. Keep one to connect for debugging
            maximumPoolSize = 19
            validate()
        }

        val dataSource = HikariDataSource(config)

        Database.connect(dataSource)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.drop(Connections, Stations)
            SchemaUtils.create(Connections, Stations)
        }
    }

    suspend fun getConnection(from: String, to: String): Connection? = dbquery {
        Connection.find {
            Connections.fromStation eq from and (Connections.toStation eq to)
        }.firstOrNull()
    }

    suspend fun createConnection(from: String, to: String, min: Int, median: Int): Connection = dbquery {
        Connection.new {
            fromStationId = EntityID(from, Stations)
            toStationId = EntityID(to, Stations)
            minDuration = min
            medianDuration = median
            lastDownload = Instant.now().epochSecond
        }
    }

    suspend fun getExistingStations(stationsIds: List<String>): List<Station> = dbquery {
        Station.find { Stations.id inList stationsIds }.toList()
    }

    suspend fun createStation(
        apiId: String,
        name: String,
        latitude: Double,
        longitude: Double,
        type: String?
    ): Station = dbquery {
        Station.new(apiId) {
            this.name = name
            this.latitude = latitude
            this.longitude = longitude
            this.type = type
        }
    }
}

private suspend fun <T> dbquery(statement: suspend Transaction.() -> T): T =
    suspendedTransactionAsync(Dispatchers.IO, statement = statement).await()
