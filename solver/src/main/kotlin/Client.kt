package pro.schmid.sbbtsp

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.schmid.sbbtsp.repositories.ConnectionsRepository
import pro.schmid.sbbtsp.solver.DataModel
import pro.schmid.sbbtsp.solver.Solver

data class Station(
    val name: String,
    val id: String
)

class Client {

    private val repository = ConnectionsRepository()

    suspend fun solve(stations: List<Station>): String {

        val allConnections = coroutineScope {
            val arrayOfArray = Array(stations.size) { LongArray(stations.size) { 0 } }

            for (from in arrayOfArray.indices) {
                for (to in arrayOfArray.indices) {
                    if (from == to) continue

                    launch {
                        val fromId = stations[from].id
                        val toId = stations[to].id
                        val connection = repository.fetch(fromId, toId)
                        arrayOfArray[from][to] = connection.minDuration.toLong()
                    }
                }
            }

            arrayOfArray
        }

        val data = DataModel(allConnections)
        val solver = Solver()
        val route = solver.solve(data)

        return buildString {
            route.forEach {
                val from = stations[it.from]
                val to = stations[it.to]
                appendln("From ${from.name} to ${to.name}: ${it.distance} minutes")
            }
            val totalTime = route.map { it.distance }.sum()
            appendln("Total time: $totalTime minutes")
        }
    }
}

suspend fun main() {
    val allPoints = listOf(
        Station("Yverdon", "8504200"),
        Station("Stoosbahn", "8577453"),
        Station("Zernez", "8509262"),
        Station("Grindelwald", "8505226"),
        Station("Bâle", "8500010"),
        Station("Kandersteg", "8507475"),
        Station("Zermatt", "8501689"),
        Station("Lucerne", "8505000")
    )

    val client = Client()
    val result = client.solve(allPoints)
    println(result)
}