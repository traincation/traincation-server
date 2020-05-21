package pro.schmid.sbbtsp.server

import kotlinx.serialization.Serializable

@Serializable
data class Result(
    val legs: List<Leg>
)

@Serializable
data class Leg(
    val from: String,
    val to: String,
    val durationMinutes: Long
)
