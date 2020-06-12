package pro.schmid.sbbtsp.solver

import com.williamfiset.algorithms.graphtheory.TspDynamicProgrammingIterative
import pro.schmid.sbbtsp.repositories.Connection

class Solver {
    fun solve(connections: Array<Array<Connection?>>): List<Leg> {
        val data = connections.map { it.map { it?.minDuration?.toDouble() ?: 0.0 }.toDoubleArray() }.toTypedArray()
        val tspSolver = TspDynamicProgrammingIterative(data)
        val route = tspSolver.tour

        return createLegs(connections, route)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createLegs(data: Array<Array<Connection?>>, route: List<Int>): List<Leg> = buildList {
        for (index in 0 until route.size - 1) {
            val from = route[index]
            val to = route[index + 1]
            val connection = data[from][to]!!
            add(Leg(from, to, connection.minDuration.toLong(), connection.journey))
        }
    }
}

data class Leg(
    val from: Int,
    val to: Int,
    val durationMinutes: Long,
    val stationsList: List<String>
)
