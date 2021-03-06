package pro.schmid.sbbtsp.repositories

data class Connection(
    val fromStation: String,
    val toStation: String,
    val minDuration: Int,
    val medianDuration: Int,
    val journey: List<String>
)

data class Station(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String?
)
