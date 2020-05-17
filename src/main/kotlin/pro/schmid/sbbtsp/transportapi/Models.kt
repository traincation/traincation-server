package pro.schmid.sbbtsp.transportapi

import kotlinx.serialization.*

@Serializable
data class ConnectionsResponse(
    val connections: List<Connection>,
    val from: Location,
    val to: Location,
    val stations: Stations
)

@Serializable
data class Connection(
    val from: Checkpoint,
    val to: Checkpoint,
    val duration: String,
    val transfers: Long,
    val products: List<String>,
    val sections: List<Section>
)

@Serializable
data class Checkpoint(
    val station: Location,
    val arrival: String? = null,
    val arrivalTimestamp: Long? = null,
    val departure: String? = null,
    val departureTimestamp: Long? = null,
    val delay: Int? = null,
    val platform: String? = null,
    val location: Location
)

@Serializable
data class Location(
    val id: String,
    val name: String,
    val coordinate: Coordinates
)

@Serializable
data class Coordinates(
    val type: Type,
    val x: Double,
    val y: Double
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
data class Section(
    val journey: Journey,
    val departure: Checkpoint,
    val arrival: Checkpoint
)

@Serializable
data class Journey(
    val name: String,
    val category: String,
    val number: String,
    val operator: String,
    val to: String,
    val passList: List<Checkpoint>
)

@Serializable
data class Stations(
    val from: List<Location>,
    val to: List<Location>
)
