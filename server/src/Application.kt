package pro.schmid.sbbtsp.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.cio.EngineMain
import org.slf4j.event.Level
import pro.schmid.sbbtsp.Client

fun main(args: Array<String>): Unit = EngineMain.main(args)

@OptIn(ExperimentalStdlibApi::class)
@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations) {
    }

    install(ContentNegotiation) {
        json()
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

            post("/solver") {

                val request = call.receive<SolverRequest>()
                val requestStationsIds = request.stationsIds
                val route = client.solve(requestStationsIds)

                val allStationsIds = buildSet {
                    addAll(requestStationsIds)
                    val stationsListIds = route.flatMap { it.stationsList }
                    addAll(stationsListIds)
                }.toList()
                val requestedStations = client.findStations(allStationsIds)

                val stations = requestedStations.map { Station(it.id, it.name, it.latitude, it.longitude, it.type) }
                val legs = route.map { leg ->
                    Leg(
                        requestStationsIds[leg.from],
                        requestStationsIds[leg.to],
                        leg.durationMinutes,
                        leg.stationsList
                    )
                }
                val result = SolverResult(legs, stations)

                call.respond(result)
            }

            post("/search") {
                val request = call.receive<SearchRequest>()
                val term = request.searchTerm
                val repoStations = client.search(term)
                val stations = repoStations.map { Station(it.id, it.name, it.latitude, it.longitude, it.type) }
                val result = SearchResult(stations)

                call.respond(result)
            }
        }
    }
}
