package pro.schmid.sbbtsp

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.schmid.sbbtsp.repositories.ConnectionsRepository
import pro.schmid.sbbtsp.solver.DataModel
import pro.schmid.sbbtsp.solver.Solver

suspend fun main() {
    val allPoints = listOf(
        Station("Yverdon", "8504200"),
        Station("Lausanne", "8501120"),
        Station("Lugano", "8505300"),
        Station("Genève", "8501008"),
        Station("Bâle", "8500010"),
        Station("Lucerne", "8505000")
    )

    val repository = ConnectionsRepository()

    val allConnections = coroutineScope {
        val arrayOfArray = Array(allPoints.size) { LongArray(allPoints.size) }

        for (from in arrayOfArray.indices) {
            for (to in arrayOfArray.indices) {
                launch {
                    if (from == to) {
                        arrayOfArray[from][to] = 0
                        return@launch
                    }
                    val fromId = allPoints[from].id
                    val toId = allPoints[to].id
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

    route.forEach {
        val from = allPoints[it.from]
        val to = allPoints[it.to]
        println("From ${from.name} to ${to.name}: ${it.distance} minutes")
    }
}

private data class Station(
    val name: String,
    val id: String
)