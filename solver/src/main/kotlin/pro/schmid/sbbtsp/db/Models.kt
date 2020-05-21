package pro.schmid.sbbtsp.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

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
