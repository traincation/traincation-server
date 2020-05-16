import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

suspend fun main() {
    println("OK")

    val client = HttpClient(CIO) {
        install(JsonFeature) {
            val json = Json {
                ignoreUnknownKeys = true
            }
            serializer = KotlinxSerializer(json)
        }
    }


    val response: Stationboard = client.get("http://transport.opendata.ch/v1/stationboard?station=Aarau&limit=10")
    println(response)

    client.close()
}
