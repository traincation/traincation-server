package pro.schmid.sbbtsp.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object Connections : IntIdTable() {
    val fromStation = varchar("fromStation", 9)
    val toStation = varchar("toStation", 9)
    val minDuration = integer("minDuration")
    val medianDuration = integer("medianDuration")
    val lastDownload = long("lastDownload")

    init {
        index(true, fromStation, toStation)
    }
}

class Connection(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Connection>(Connections)

    var fromStation by Connections.fromStation
    var toStation by Connections.toStation
    var minDuration by Connections.minDuration
    var medianDuration by Connections.medianDuration
    var lastDownload by Connections.lastDownload

}

object Database {
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