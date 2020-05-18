package pro.schmid.sbbtsp.solver

import com.google.ortools.constraintsolver.*
import java.util.logging.Logger

class Solver {
    init {
        System.loadLibrary("jniortools")
    }

    private val logger = Logger.getLogger(Solver::class.java.name)

    fun solve(data: DataModel) {
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

        printSolution(
            routing,
            manager,
            solution
        )
    }

    private fun printSolution(
        routing: RoutingModel, manager: RoutingIndexManager, solution: Assignment
    ) {
        // Solution cost.
        logger.info("Objective: " + solution.objectiveValue() + "miles")
        // Inspect solution.
        logger.info("Route:")
        var routeDistance: Long = 0
        var route = ""
        var index = routing.start(0)
        while (!routing.isEnd(index)) {
            route += manager.indexToNode(index).toString() + " -> "
            val previousIndex = index
            index = solution.value(routing.nextVar(index))
            routeDistance += routing.getArcCostForVehicle(previousIndex, index, 0)
        }
        route += manager.indexToNode(routing.end(0))
        logger.info(route)
        logger.info("Route distance: " + routeDistance + "miles")
    }
}

data class DataModel(
    val distanceMatrix: Array<LongArray>,
    val vehicleNumber: Int = 1,
    val depot: Int = 0
)
