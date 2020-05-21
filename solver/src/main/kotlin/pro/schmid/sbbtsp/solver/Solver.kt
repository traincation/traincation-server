package pro.schmid.sbbtsp.solver

import com.google.ortools.constraintsolver.*

class Solver {
    init {
        System.loadLibrary("jniortools")
    }

    fun solve(data: DataModel): List<Leg> {
        val manager = RoutingIndexManager(data.distanceMatrix.size, data.vehicleNumber, data.depot)
        val routing = RoutingModel(manager)

        val transitCallbackIndex =
            routing.registerTransitCallback { fromIndex: Long, toIndex: Long ->
                // Convert from routing variable Index to user NodeIndex.
                val fromNode = manager.indexToNode(fromIndex)
                val toNode = manager.indexToNode(toIndex)
                data.distanceMatrix[fromNode][toNode]
            }

        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex)

        val searchParameters =
            main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .build()

        val solution = routing.solveWithParameters(searchParameters)

        return createLegs(routing, manager, solution)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createLegs(
        routing: RoutingModel, manager: RoutingIndexManager, solution: Assignment
    ): List<Leg> = buildList {
        var index = routing.start(0)
        while (!routing.isEnd(index)) {
            val fromId = manager.indexToNode(index)
            val previousIndex = index
            index = solution.value(routing.nextVar(index))
            val toId = manager.indexToNode(index)
            val distance = routing.getArcCostForVehicle(previousIndex, index, 0)
            add(Leg(fromId, toId, distance))
        }
    }
}

data class DataModel(
    val distanceMatrix: Array<LongArray>,
    val vehicleNumber: Int = 1,
    val depot: Int = 0
)

data class Leg(
    val from: Int,
    val to: Int,
    val durationMinutes: Long
)
