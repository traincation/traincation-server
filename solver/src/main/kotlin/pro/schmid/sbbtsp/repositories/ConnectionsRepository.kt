package pro.schmid.sbbtsp.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import pro.schmid.sbbtsp.db.Connection
import pro.schmid.sbbtsp.db.Database
import pro.schmid.sbbtsp.transportapi.TransportApi

private val regex = Regex("([0-9]{2})d([0-9]{2}):([0-9]{2}):([0-9]{2})")

class ConnectionsRepository(
    private val database: Database = Database(),
    private val api: TransportApi = TransportApi()
) {
    private val logger = LoggerFactory.getLogger("chapters.introduction.HelloWorld2");

    suspend fun fetch(from: String, to: String): Connection = withContext(Dispatchers.IO) {
        logger.debug("($from, $to): Fetching...")
        database.get(from, to)?.let {
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

        val fromNetwork = database.create(
            from,
            to,
            allTimes.first(),
            allTimes.median()
        )
        return@withContext fromNetwork
    }
}

private fun <T : Comparable<T>> List<T>.median() = this.sorted().let { it[it.size / 2] }
