import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get

suspend fun main() {
    println("OK")

    val client = HttpClient(CIO)

    val content: String = client.get("https://en.wikipedia.org/wiki/Main_Page")
    println(content)

    client.close()
}