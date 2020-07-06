package pro.schmid.sbbtsp.transportapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

class TransportApi {

    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            val json = Json {
                ignoreUnknownKeys = true
            }
            serializer = KotlinxSerializer(json)
        }
        engine {
            requestTimeout = 30000
        }
    }

    suspend fun downloadConnections(from: String, to: String): List<Connection> {
        val response: ConnectionsResponse = client.get("http://transport.opendata.ch/v1/connections") {
            url {
                parameters["from"] = from
                parameters["to"] = to
                parameters["date"] = "2020-07-01"
                parameters["time"] = "07:00"
                parameters["limit"] = "16"
            }
        }

        return response.connections
    }

    suspend fun downloadLocations(query: String): List<Station> {
        val response: LocationsResponse = client.get("http://transport.opendata.ch/v1/locations") {
            url {
                parameters["query"] = query
                parameters["type"] = "station"
            }
        }

        return response.stations
    }
}
