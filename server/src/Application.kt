package pro.schmid

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import org.slf4j.event.Level
import pro.schmid.sbbtsp.Client
import pro.schmid.sbbtsp.Station

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations) {
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ConditionalHeaders)

    val client = Client()

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        route("api/") {

            get("/solver") {

                val allPoints = listOf(
                    Station("Yverdon", "8504200"),
                    Station("Stoosbahn", "8577453"),
                    Station("Zernez", "8509262"),
                    Station("Grindelwald", "8505226"),
                    Station("Bâle", "8500010")
                )
                val result = client.solve(allPoints)

                call.respondText(result)
            }
        }
    }
}
