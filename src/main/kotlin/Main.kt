package pro.schmid.sbbtsp

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.schmid.sbbtsp.repositories.ConnectionsRepository
import pro.schmid.sbbtsp.solver.DataModel
import pro.schmid.sbbtsp.solver.Solver

suspend fun main() {
    val yverdon = "8504200"
    val lausanne = "8501120"
    val lugano = "8505300"
    val geneve = "8501008"
    val basel = "8500010"
    val lucerne = "8505000"

    val allPoints = listOf(yverdon, lausanne, lugano, geneve, basel, lucerne)
    val stationsArray = allPoints.toTypedArray()

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
                    val fromId = stationsArray[from]
                    val toId = stationsArray[to]
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

    println(route)
}
