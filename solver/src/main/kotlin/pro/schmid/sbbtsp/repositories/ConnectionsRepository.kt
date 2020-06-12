package pro.schmid.sbbtsp.repositories

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import pro.schmid.sbbtsp.db.Database
import pro.schmid.sbbtsp.transportapi.TransportApi

private val regex = Regex("([0-9]{2})d([0-9]{2}):([0-9]{2}):([0-9]{2})")

class ConnectionsRepository(
    private val database: Database = Database(),
    private val api: TransportApi = TransportApi()
) {
    private val logger = LoggerFactory.getLogger("ConnectionsRepository");

    suspend fun fetchConnections(from: String, to: String): Connection = withContext(Dispatchers.IO) {
        logger.debug("($from, $to): Fetching...")
        database.getConnection(from, to)?.let {
            logger.debug("($from, $to): Found from DB")
            return@withContext it
        }

        logger.debug("($from, $to): Downloading...")
        val allConnections = api.downloadConnections(from, to)
        logger.debug("($from, $to): Downloaded")

        val allTimes = allConnections.mapNotNull {
            val journeyDuration = it.duration
            val result = regex.matchEntire(journeyDuration) ?: return@mapNotNull null

            val (sDays, sHours, sMinutes) = result.destructured

            val days = sDays.toInt()
            val hours = sHours.toInt()
            val minutes = sMinutes.toInt()

            val totalMinutes = days * 1440 + hours * 60 + minutes

            return@mapNotNull totalMinutes
        }.sorted()

        val firstConnectionSections = allConnections.first().sections
        val firstConnectionPassList = firstConnectionSections.map { section ->
            // If there is a journey, take it, otherwise create a one-stop from departure to arrival
            if (section.journey != null) {
                section.journey.passList.map { it.station }
            } else {
                listOf(section.departure.station, section.arrival.station)
            }
        }
        val firstSection = firstConnectionPassList.first()
        val remainingSections = firstConnectionPassList.drop(1)
        val remainingWithoutStart = remainingSections.map { it.drop(1) }
        val allStations = firstSection + remainingWithoutStart.flatten()
        val allStationsIds = allStations.mapNotNull { it.id }

        allStations.forEach {
            createStation(it)
        }
        
        val fromNetwork = database.createConnection(
            from,
            to,
            allTimes.first(),
            allTimes.median(),
            allStationsIds
        )
        return@withContext fromNetwork
    }

    suspend fun fetchStations(stationsIds: List<String>): List<Station> {

        val existingStations = database.getExistingStations(stationsIds)
        val existingIds = existingStations.map { it.id }
        val missingIds = stationsIds.subtract(existingIds)

        val allStationsJobs = coroutineScope {
            missingIds.map { stationId ->
                async {
                    api.downloadLocations(stationId)
                }
            }
        }

        val distinctStations = allStationsJobs
            .awaitAll()
            .flatten()
            .distinctBy { it.id }

        // Create all missing stations
        val newStations = distinctStations.mapNotNull { createStation(it) }

        return existingStations + newStations
    }

    suspend fun searchStation(term: String): List<Station> {
        val apiResult = api.downloadLocations(term)

        return apiResult.mapNotNull { createStation(it) }
    }

    private suspend fun createStation(apiStation: pro.schmid.sbbtsp.transportapi.Station): Station? {
        val stationId = apiStation.id ?: return null
        val x = apiStation.coordinate.x ?: return null
        val y = apiStation.coordinate.y ?: return null

        return database.createStation(
            stationId,
            apiStation.name,
            x,
            y,
            apiStation.icon
        )
    }
}

private fun <T : Comparable<T>> List<T>.median() = this.sorted().let { it[it.size / 2] }
