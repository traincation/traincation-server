package pro.schmid.sbbtsp.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable

object Connections : IntIdTable() {
    val fromStation = reference("fromStation", Stations)
    val toStation = reference("toStation", Stations)
    val minDuration = integer("minDuration")
    val medianDuration = integer("medianDuration")
    val lastDownload = long("lastDownload")

    init {
        index(true, fromStation, toStation)
    }
}

class Connection(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Connection>(Connections)

    //var fromStation by Station referencedOn Connections.fromStation
    //var toStation by Station referencedOn Connections.toStation
    
    var fromStationId by Connections.fromStation
    var toStationId by Connections.toStation
    var minDuration by Connections.minDuration
    var medianDuration by Connections.medianDuration
    var lastDownload by Connections.lastDownload

}

object Stations : IdTable<String>() {
    override val id = varchar("id", 9).uniqueIndex().entityId()
    val name = varchar("name", 100)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val type = varchar("type", 100).nullable()

    override val primaryKey = PrimaryKey(id)
}

class Station(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, Station>(Stations)

    var name by Stations.name
    var latitude by Stations.latitude
    var longitude by Stations.longitude
    var type by Stations.type
}
