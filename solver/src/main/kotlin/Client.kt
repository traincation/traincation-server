package pro.schmid.sbbtsp

import TspDynamicProgrammingIterative
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.schmid.sbbtsp.repositories.ConnectionsRepository
import pro.schmid.sbbtsp.solver.DataModel
import pro.schmid.sbbtsp.solver.Leg
import pro.schmid.sbbtsp.solver.Solver
import kotlin.system.measureNanoTime

class Client {

    private val repository = ConnectionsRepository()
    private val solverOrTools = Solver()

    suspend fun solve(stations: List<String>): List<Leg> {

        val allConnections = coroutineScope {
            val arrayOfArray = Array(stations.size) { LongArray(stations.size) { 0 } }

            for (from in arrayOfArray.indices) {
                for (to in arrayOfArray.indices) {
                    if (from == to) continue

                    launch {
                        val fromId = stations[from]
                        val toId = stations[to]
                        val connection = repository.fetch(fromId, toId)
                        arrayOfArray[from][to] = connection.minDuration.toLong()
                    }
                }
            }

            arrayOfArray
        }


        val data = DataModel(allConnections)
        var routeOrTools: List<Leg> = listOf()
        val timeOrTools = measureNanoTime {
            routeOrTools = solverOrTools.solve(data)
        }
        val totalOrTools = routeOrTools.map { it.durationMinutes }.sum()

        println(routeOrTools.map { it.from })
        println(totalOrTools)
        println(timeOrTools)

        val doubleConnections = allConnections.map { it.map { it.toDouble() }.toDoubleArray() }.toTypedArray()

        val solverJava = TspDynamicProgrammingIterative(doubleConnections)
        val timeJava = measureNanoTime {
            solverJava.solve()
        }
        val tourJava = solverJava.tour
        val totalJava = solverJava.tourCost

        println(tourJava)
        println(totalJava)
        println(timeJava)

        return routeOrTools
    }
}

private data class Station(
    val name: String,
    val id: String
)

suspend fun main() {
    val stations = listOf(
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

    val client = Client()
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    client.solve(stations.map { it.id })
    val route = client.solve(stations.map { it.id })
    val result = buildString {
        route.forEach {
            val from = stations[it.from]
            val to = stations[it.to]
            appendln("From ${from.name} to ${to.name}: ${it.durationMinutes} minutes")
        }
        val totalTime = route.map { it.durationMinutes }.sum()
        appendln("Total time: $totalTime minutes")
    }
    println(result)
}