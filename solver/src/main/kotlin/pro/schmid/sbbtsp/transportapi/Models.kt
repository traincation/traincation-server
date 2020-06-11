package pro.schmid.sbbtsp.transportapi

import kotlinx.serialization.*

@Serializable
data class ConnectionsResponse(
    val connections: List<Connection>
)

@Serializable
data class LocationsResponse(
    val stations: List<Station>
)

@Serializable
data class Connection(
    val from: Checkpoint,
    val to: Checkpoint,
    val duration: String
)

@Serializable
data class Checkpoint(
    val station: Station,
    val arrival: String? = null,
    val arrivalTimestamp: Long? = null,
    val departure: String? = null,
    val departureTimestamp: Long? = null
)

@Serializable
data class Station(
    val id: String? = null,
    val name: String,
    val coordinate: Coordinates,
    val icon: String? = null
)

@Serializable
data class Coordinates(
    val type: Type,
    val x: Double? = null,
    val y: Double? = null
)

@Serializable(with = Type.Companion::class)
enum class Type(val value: String) {
    Wgs84("WGS84");

    companion object : KSerializer<Type> {
        override val descriptor: SerialDescriptor
            get() {
                return PrimitiveDescriptor("quicktype.Type", PrimitiveKind.STRING)
            }

        override fun deserialize(decoder: Decoder): Type = when (val value = decoder.decodeString()) {
            "WGS84" -> Wgs84
            else -> throw IllegalArgumentException("Type could not parse: $value")
        }

        override fun serialize(encoder: Encoder, value: Type) {
            return encoder.encodeString(value.value)
        }
    }
}

@Serializable
data class Stations(
    val from: List<Station>,
    val to: List<Station>
)
