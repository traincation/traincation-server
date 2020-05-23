package pro.schmid.sbbtsp

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.schmid.sbbtsp.repositories.ConnectionsRepository
import pro.schmid.sbbtsp.solver.Leg
import pro.schmid.sbbtsp.solver.Solver

class Client {

    private val repository = ConnectionsRepository()
    private val solver = Solver()

    suspend fun findStations(stations: List<String>): List<String> {
        return stations.mapNotNull { stationId ->
            repository.fetchLocations(stationId).firstOrNull()
        }
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

private data class Station(
    val name: String,
    val id: String
)

suspend fun main() {
    val inputStations = listOf(
        Station("Yverdon", "8504200"),
        Station("Stoosbahn", "8577453"),
        Station("Zernez", "8509262"),
        Station("Grindelwald", "8505226"),
        Station("Bâle", "8500010"),
        Station("Kandersteg", "8507475"),
        Station("Zermatt", "8501689"),
        Station("Lucerne", "8505000"),
        Station("Genève", "8501008"),
        Station("St-Gallen", "8506302"),
        Station("Zurich", "8503000"),
        Station("Lausanne", "8501120")
    )
    val stationsIds = inputStations.map { it.id }

    val client = Client()

    val stations = client.findStations(stationsIds)
    println(stations)

    val route = client.solve(stationsIds)
    val result = buildString {
        route.forEach {
            val from = inputStations[it.from]
            val to = inputStations[it.to]
            appendln("From ${from.name} to ${to.name}: ${it.durationMinutes} minutes")
        }
        val totalTime = route.map { it.durationMinutes }.sum()
        appendln("Total time: $totalTime minutes")
    }
    println(result)
}