package pro.schmid.sbbtsp.db

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable

object Connections : IntIdTable() {
    val fromStation = reference("fromStation", Stations)
    val toStation = reference("toStation", Stations)
    val minDuration = integer("minDuration")
    val medianDuration = integer("medianDuration")
    val lastDownload = long("lastDownload")
    val stationsList = varchar("stationsList", 1500)

    init {
        uniqueIndex(fromStation, toStation)
    }
}

object Stations : IdTable<String>() {
    override val id = varchar("id", 9).entityId()
    val name = varchar("name", 100)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val type = varchar("type", 100).nullable()

    override val primaryKey = PrimaryKey(id)
}
