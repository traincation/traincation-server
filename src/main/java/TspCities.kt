import com.google.ortools.constraintsolver.*
import java.util.logging.Logger

object TspCities {
    init {
        System.loadLibrary("jniortools")
    }

    private val logger = Logger.getLogger(TspCities::class.java.name)

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val data = DataModel()

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

        printSolution(routing, manager, solution)
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

    internal class DataModel {
        val distanceMatrix = arrayOf(
            longArrayOf(0, 2451, 713, 1018, 1631, 1374, 2408, 213, 2571, 875, 1420, 2145, 1972),
            longArrayOf(2451, 0, 1745, 1524, 831, 1240, 959, 2596, 403, 1589, 1374, 357, 579),
            longArrayOf(713, 1745, 0, 355, 920, 803, 1737, 851, 1858, 262, 940, 1453, 1260),
            longArrayOf(1018, 1524, 355, 0, 700, 862, 1395, 1123, 1584, 466, 1056, 1280, 987),
            longArrayOf(1631, 831, 920, 700, 0, 663, 1021, 1769, 949, 796, 879, 586, 371),
            longArrayOf(1374, 1240, 803, 862, 663, 0, 1681, 1551, 1765, 547, 225, 887, 999),
            longArrayOf(2408, 959, 1737, 1395, 1021, 1681, 0, 2493, 678, 1724, 1891, 1114, 701),
            longArrayOf(213, 2596, 851, 1123, 1769, 1551, 2493, 0, 2699, 1038, 1605, 2300, 2099),
            longArrayOf(2571, 403, 1858, 1584, 949, 1765, 678, 2699, 0, 1744, 1645, 653, 600),
            longArrayOf(875, 1589, 262, 466, 796, 547, 1724, 1038, 1744, 0, 679, 1272, 1162),
            longArrayOf(1420, 1374, 940, 1056, 879, 225, 1891, 1605, 1645, 679, 0, 1017, 1200),
            longArrayOf(2145, 357, 1453, 1280, 586, 887, 1114, 2300, 653, 1272, 1017, 0, 504),
            longArrayOf(1972, 579, 1260, 987, 371, 999, 701, 2099, 600, 1162, 1200, 504, 0)
        )
        val vehicleNumber = 1
        val depot = 0
    }
}