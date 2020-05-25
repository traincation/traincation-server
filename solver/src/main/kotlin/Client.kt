package pro.schmid.sbbtsp

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.schmid.sbbtsp.db.Station
import pro.schmid.sbbtsp.repositories.ConnectionsRepository
import pro.schmid.sbbtsp.solver.Leg
import pro.schmid.sbbtsp.solver.Solver

class Client {

    private val repository = ConnectionsRepository()
    private val solver = Solver()

    suspend fun findStations(stationsIds: List<String>): Map<String, Station> {
        val allStations = repository.fetchStations(stationsIds)
        val stationById = allStations.associateBy { it.apiId }
        return stationById
    }

    suspend fun solve(stations: List<String>): List<Leg> {

        val allConnections = coroutineScope {
            val arrayOfArray = Array(stations.size) { DoubleArray(stations.size) { 0.0 } }

            for (from in arrayOfArray.indices) {
                for (to in arrayOfArray.indices) {
                    if (from == to) continue

                    launch {
                        val fromId = stations[from]
                        val toId = stations[to]
                        val connection = repository.fetchConnections(fromId, toId)
                        arrayOfArray[from][to] = connection.minDuration.toDouble()
                    }
                }
            }

            arrayOfArray
        }

        return solver.solve(allConnections)
    }
}

suspend fun main() {
    val stationsIds = listOf(
        "8504200", // Yverdon
        "8577453", // Stoosbahn
        "8509262", // Zernez
        "8505226", // Grindelwald
        "8500010", // Bâle
        "8507475", // Kandersteg
        "8501689", // Zermatt
        "8505000", // Lucerne
        "8501008", // Genève
        "8506302", // St-Gallen
        "8503000", // Zurich
        "8501120"  // Lausanne
    )

    val client = Client()

    val stations = client.findStations(stationsIds)
    val stationsPrint = stations.map { "${it.key}: ${it.value.name}" }
    println(stationsPrint)

    val route = client.solve(stationsIds)
    val result = buildString {
        route.forEach {
            val fromApiId = stationsIds[it.from]
            val toApiId = stationsIds[it.to]
            val from = stations[fromApiId]!!
            val to = stations[toApiId]!!
            appendln("From ${from.name} to ${to.name}: ${it.durationMinutes} minutes")
        }
        val totalTime = route.map { it.durationMinutes }.sum()
        appendln("Total time: $totalTime minutes")
    }
    println(result)
}


data class Station3(
    val apiId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String?
)
