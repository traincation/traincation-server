package pro.schmid.sbbtsp.solver

import com.williamfiset.algorithms.graphtheory.TspDynamicProgrammingIterative

class Solver {
    fun solve(data: Array<DoubleArray>): List<Leg> {
        val tspSolver = TspDynamicProgrammingIterative(data)
        val route = tspSolver.tour

        return createLegs(data, route)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createLegs(data: Array<DoubleArray>, route: List<Int>): List<Leg> = buildList {
        for (index in 0 until route.size - 1) {
            val from = route[index]
            val to = route[index + 1]
            val distance = data[from][to]
            add(Leg(from, to, distance.toLong()))
        }
    }
}

data class Leg(
    val from: Int,
    val to: Int,
    val durationMinutes: Long
)
