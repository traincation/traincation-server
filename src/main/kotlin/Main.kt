package pro.schmid.sbbtsp

import pro.schmid.sbbtsp.db.Connection
import pro.schmid.sbbtsp.db.Database
import pro.schmid.sbbtsp.transportapi.downloadConnections

suspend fun main() {
    val from = "8501120"
    val to = "8501008"

    val fetched = fetch(from, to)
    println(fetched)
}

suspend fun fetch(from: String, to: String): Connection {
    val fromDb = Database.get(from, to)
    if (fromDb != null) return fromDb

    val firstConnection = downloadConnections(from, to)
    val fromNetwork = Database.create(
        firstConnection.fromId,
        firstConnection.toId,
        firstConnection.minDuration,
        firstConnection.medianDuration
    )
    return fromNetwork
}