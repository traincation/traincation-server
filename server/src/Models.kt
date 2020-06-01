package pro.schmid.sbbtsp.server

import kotlinx.serialization.Serializable

@Serializable
data class SolverResult(
    val legs: List<Leg>,
    val stations: List<Station>
)

@Serializable
data class SearchResult(
    val stations: List<Station>
)

@Serializable
data class Leg(
    val from: String,
    val to: String,
    val durationMinutes: Long
)

@Serializable
data class Station(
    val apiId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String?
)

@Serializable
data class SolverRequest(
    val stationsIds: List<String>
)

@Serializable
data class SearchRequest(
    val searchTerm: String
)
