package pro.schmid.sbbtsp

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.schmid.sbbtsp.repositories.ConnectionsRepository
import pro.schmid.sbbtsp.repositories.Station
import pro.schmid.sbbtsp.solver.Leg
import pro.schmid.sbbtsp.solver.Solver

class Client {

    private val repository = ConnectionsRepository()
    private val solver = Solver()

    suspend fun findStations(stationsIds: List<String>): List<Station> {
        return repository.fetchStations(stationsIds)
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

    suspend fun search(term: String): List<Station> {
        return repository.searchStation(term)
    }
}

suspend fun main() {

    val client = Client()

    //doSolve(client)
    search(client)
}

private suspend fun doSolve(client: Client) {
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

    val stations = client.findStations(stationsIds)
    val stationById = stations.associateBy { it.id }
    val stationsPrint = stationById.map { "${it.key}: ${it.value.name}" }
    println(stationsPrint)

    val route = client.solve(stationsIds)
    val result = buildString {
        route.forEach {
            val fromApiId = stationsIds[it.from]
            val toApiId = stationsIds[it.to]
            val from = stationById[fromApiId]!!
            val to = stationById[toApiId]!!
            appendln("From ${from.name} to ${to.name}: ${it.durationMinutes} minutes")
        }
        val totalTime = route.map { it.durationMinutes }.sum()
        appendln("Total time: $totalTime minutes")
    }
    println(result)
}

private suspend fun search(client: Client) {
    val stationsYverdon = client.search("Yverdon")
    stationsYverdon.forEach { println(it) }

    val stationsLausanne = client.search("Lausanne")
    stationsLausanne.forEach { println(it) }
}