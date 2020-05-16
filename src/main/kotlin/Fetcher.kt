import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get

suspend fun main() {
    println("OK")

    val client = HttpClient(CIO) {
        install(JsonFeature)
    }


    val response: User = client.get("http://www.mocky.io/v2/5ebfc92c32000076730c34f1")
    println(response)

    client.close()
}

data class User(val id: Int)