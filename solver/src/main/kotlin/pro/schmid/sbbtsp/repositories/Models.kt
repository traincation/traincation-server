package pro.schmid.sbbtsp.repositories

class Connection(
    val fromStation: String,
    val toStation: String,
    val minDuration: Int,
    val medianDuration: Int
)

fun pro.schmid.sbbtsp.db.Connection.toRepoModel() = Connection(
    this.fromStationId.value,
    this.toStationId.value,
    this.minDuration,
    this.medianDuration
)

data class Station(
    val apiId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String?
)

fun pro.schmid.sbbtsp.db.Station.toRepoModel() = Station(
    this.id.value,
    this.name,
    this.latitude,
    this.longitude,
    this.type
)
