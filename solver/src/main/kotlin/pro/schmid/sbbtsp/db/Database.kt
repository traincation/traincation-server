package pro.schmid.sbbtsp.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import pro.schmid.sbbtsp.db.Connections.fromStation
import pro.schmid.sbbtsp.db.Connections.medianDuration
import pro.schmid.sbbtsp.db.Connections.minDuration
import pro.schmid.sbbtsp.db.Connections.stationsList
import pro.schmid.sbbtsp.db.Connections.toStation
import pro.schmid.sbbtsp.db.Stations.latitude
import pro.schmid.sbbtsp.db.Stations.longitude
import pro.schmid.sbbtsp.db.Stations.name
import pro.schmid.sbbtsp.db.Stations.type
import pro.schmid.sbbtsp.repositories.Connection
import pro.schmid.sbbtsp.repositories.Station
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
            SchemaUtils.create(Connections, Stations)
        }
    }

    suspend fun getConnection(from: String, to: String): Connection? = dbquery {
        val row = Connections.select {
            fromStation eq from and (toStation eq to)
        }.firstOrNull()

        return@dbquery row?.let {
            Connection(
                it[fromStation].value,
                it[toStation].value,
                it[minDuration],
                it[medianDuration],
                row[stationsList].split(",")
            )
        }
    }

    suspend fun createConnection(from: String, to: String, min: Int, median: Int, stationsList: List<String>) =
        dbquery {
            val row = Connections.insertIgnore {
                it[fromStation] = EntityID(from, Stations)
                it[toStation] = EntityID(to, Stations)
                it[minDuration] = min
                it[medianDuration] = median
                it[lastDownload] = Instant.now().epochSecond
                it[this.stationsList] = stationsList.joinToString(",")
            }
            return@dbquery Connection(
                row[fromStation].value,
                row[toStation].value,
                row[minDuration],
                row[medianDuration],
                row[Connections.stationsList].split(",")
            )
        }

    suspend fun getExistingStations(stationsIds: List<String>): List<Station> = dbquery {
        Stations
            .select { Stations.id inList stationsIds }
            .map {
                Station(
                    it[Stations.id].value,
                    it[name],
                    it[latitude],
                    it[longitude],
                    it[type]
                )
            }
    }

    suspend fun createStation(
        apiId: String,
        name: String,
        latitude: Double,
        longitude: Double,
        type: String?
    ): Station = dbquery {
        val row = Stations.insertIgnore {
            it[Stations.id] = EntityID(apiId, Stations)
            it[Stations.name] = name
            it[Stations.latitude] = latitude
            it[Stations.longitude] = longitude
            it[Stations.type] = type
        }
        return@dbquery Station(
            row[Stations.id].value,
            row[Stations.name],
            row[Stations.latitude],
            row[Stations.longitude],
            row[Stations.type]
        )
    }
}

private suspend fun <T> dbquery(statement: suspend Transaction.() -> T): T =
    suspendedTransactionAsync(Dispatchers.IO, statement = statement).await()
