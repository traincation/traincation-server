package pro.schmid.sbbtsp.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object Connections : IntIdTable() {
    val fromStation = varchar("fromStation", 9)
    val toStation = varchar("toStation", 9)
    val minDuration = integer("minDuration")
    val medianDuration = integer("medianDuration")

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

    fun create(from: String, to: String, min: Int, median: Int) {
        transaction {
            Connection.new {
                fromStation = from
                toStation = to
                minDuration = min
                medianDuration = median
            }
        }
    }
}