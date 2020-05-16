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


    val response: ConnectionsResponse = client.get("http://transport.opendata.ch/v1/connections?from=Lausanne&to=Gen%C3%A8ve&date=2020-07-01&time=07:00")
    println(response)

    client.close()
}
