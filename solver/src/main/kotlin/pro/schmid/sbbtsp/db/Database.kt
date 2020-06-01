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
            SchemaUtils.drop(Connections, Stations)
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
                it[medianDuration]
            )
        }
    }

    suspend fun createConnection(from: String, to: String, min: Int, median: Int) = dbquery {
        val row = Connections.insertIgnore {
            it[fromStation] = EntityID(from, Stations)
            it[toStation] = EntityID(to, Stations)
            it[minDuration] = min
            it[medianDuration] = median
            it[lastDownload] = Instant.now().epochSecond
        }
        return@dbquery row.let {
            pro.schmid.sbbtsp.repositories.Connection(
                it[pro.schmid.sbbtsp.db.Connections.fromStation].value,
                it[pro.schmid.sbbtsp.db.Connections.toStation].value,
                it[pro.schmid.sbbtsp.db.Connections.minDuration],
                it[pro.schmid.sbbtsp.db.Connections.medianDuration]
            )
        }
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
        return@dbquery row.let {
            Station(
                it[Stations.id].value,
                it[Stations.name],
                it[Stations.latitude],
                it[Stations.longitude],
                it[Stations.type]
            )
        }
    }
}

private suspend fun <T> dbquery(statement: suspend Transaction.() -> T): T =
    suspendedTransactionAsync(Dispatchers.IO, statement = statement).await()
