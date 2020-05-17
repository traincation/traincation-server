package pro.schmid.sbbtsp.transportapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

private val regex = Regex("([0-9]{2})d([0-9]{2}):([0-9]{2}):([0-9]{2})")

suspend fun downloadConnections(from: String, to: String): ConnectionResult {

    val client = HttpClient(CIO) {
        install(JsonFeature) {
            val json = Json {
                ignoreUnknownKeys = true
            }
            serializer = KotlinxSerializer(json)
        }
    }

    val response: ConnectionsResponse = client.get("http://transport.opendata.ch/v1/connections") {
        url {
            parameters["from"] = from
            parameters["to"] = to
            parameters["date"] = "2020-07-01"
            parameters["time"] = "07:00"
            parameters["limit"] = "10"
        }
    }
    println(response)

    val allTimes = response.connections
        .mapNotNull {
            val journeyDuration = it.duration
            val result = regex.matchEntire(journeyDuration) ?: return@mapNotNull null

            val (sDays, sHours, sMinutes) = result.destructured

            val days = sDays.toInt()
            val hours = sHours.toInt()
            val minutes = sMinutes.toInt()

            val totalMinutes = days * 1440 + hours * 60 + minutes

            return@mapNotNull totalMinutes
        }
        .sorted()

    client.close()

    return ConnectionResult(
        response.from.id,
        response.to.id,
        allTimes.first(),
        allTimes.median()
    )
}

data class ConnectionResult(
    val fromId: String,
    val toId: String,
    val minDuration: Int,
    val medianDuration: Int
)

private fun <T : Comparable<T>> List<T>.median() = this.sorted().let { it[it.size / 2] }
