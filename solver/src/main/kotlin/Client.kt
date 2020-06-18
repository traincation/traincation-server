package pro.schmid.sbbtsp

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.schmid.sbbtsp.repositories.Connection
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
            val arrayOfArray = Array(stations.size) { Array<Connection?>(stations.size) { null } }

            for (from in arrayOfArray.indices) {
                for (to in arrayOfArray.indices) {
                    if (from == to) continue

                    launch {
                        val fromId = stations[from]
                        val toId = stations[to]
                        val connection = repository.fetchConnections(fromId, toId)
                        arrayOfArray[from][to] = connection
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

    doSolve(client)
    //search(client)
}

@OptIn(ExperimentalStdlibApi::class)
private suspend fun doSolve(client: Client) {
    val stationsIds = listOf(
        "8504200", // Yverdon
        //"8509262", // Zernez
        //"8505226", // Grindelwald
        //"8509000", // Chur
        //"8509253", // St Moritz
        //"8505165", // Andermatt
        //"8503000", // Zurich
        "8577737", // Gruyères, gare
        "8509778", // Il Fuorn P6
        "8507475" // Kandersteg
    )

    val stations = client.findStations(stationsIds)
    val stationsPrint = stations.map { "${it.id}: ${it.name}" }
    println(stationsPrint)

    val route = client.solve(stationsIds)

    val allStationsIds = buildSet {
        addAll(stationsIds)
        val stationsListIds = route.flatMap { it.stationsList }
        addAll(stationsListIds)
    }.toList()
    val stationById = client.findStations(allStationsIds).associateBy { it.id }

    val result = buildString {
        route.forEach { leg ->
            val fromApiId = stationsIds[leg.from]
            val toApiId = stationsIds[leg.to]
            val fromStation = stationById[fromApiId]!!
            val toStation = stationById[toApiId]!!
            val durationMinutes = leg.durationMinutes
            append("From ${fromStation.name} to ${toStation.name}: $durationMinutes minutes")

            append(" / ")

            val allStations = leg.stationsList.mapNotNull { stationById[it] }.map { it.name }.joinToString(", ")
            append(allStations)
            appendln()
        }
        val totalTime = route.map { it.durationMinutes }.sum()
        appendln("Total time: $totalTime minutes")
    }
    println(result)
}

private suspend fun search(client: Client) {
    val stationsYverdon = client.search("Vuadens")
    stationsYverdon.forEach { println(it) }
}