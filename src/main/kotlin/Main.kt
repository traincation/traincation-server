package pro.schmid.sbbtsp

import kotlinx.coroutines.*
import pro.schmid.sbbtsp.db.Connection
import pro.schmid.sbbtsp.db.Database
import pro.schmid.sbbtsp.transportapi.downloadConnections

suspend fun main() {
    val lausanne = "8501120"
    val geneve = "8501008"
    val yverdon = "8504200"
    val basel = "8500010"
    val lugano = "8505300"
    val lucerne = "8505000"

    val allPoints = listOf(lausanne, geneve, yverdon, basel, lugano, lucerne)

    val allConnections = coroutineScope {
        val allDeferred = mutableMapOf<Pair<String, String>, Deferred<Connection>>()
        for (from in allPoints) {
            for (to in allPoints) {
                if (from == to) continue
                allDeferred[Pair(from, to)] = async { fetch(from, to) }
            }
        }
        allDeferred.mapValues { it.value.await() }
    }

    allConnections.forEach {
        println("For ${it.key}: ${it.value.minDuration} and ${it.value.medianDuration}")
    }
}

suspend fun fetch(from: String, to: String): Connection = withContext(Dispatchers.IO) {
    println("($from, $to): Fetching...")
    Database.get(from, to)?.let {
        println("($from, $to): Found from DB")
        return@withContext it
    }

    println("($from, $to): Downloading...")
    val firstConnection = downloadConnections(from, to)
    val fromNetwork = Database.create(
        firstConnection.fromId,
        firstConnection.toId,
        firstConnection.minDuration,
        firstConnection.medianDuration
    )
    println("($from, $to): Downloaded")
    return@withContext fromNetwork
}