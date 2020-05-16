
import kotlinx.serialization.*

@Serializable
data class Stationboard (
    val station: Ation,
    val stationboard: List<StationboardElement>
)

@Serializable
data class Ation (
    val id: String,
    val name: String? = null,
    val coordinate: Coordinate
)

@Serializable
data class Coordinate (
    val type: Type,
    val x: Double? = null,
    val y: Double? = null
)

@Serializable(with = Type.Companion::class)
enum class Type(val value: String) {
    Wgs84("WGS84");

    companion object : KSerializer<Type> {
        override val descriptor: SerialDescriptor get() {
            return PrimitiveDescriptor("quicktype.Type", PrimitiveKind.STRING)
        }
        override fun deserialize(decoder: Decoder): Type = when (val value = decoder.decodeString()) {
            "WGS84" -> Wgs84
            else    -> throw IllegalArgumentException("Type could not parse: $value")
        }
        override fun serialize(encoder: Encoder, value: Type) {
            return encoder.encodeString(value.value)
        }
    }
}

@Serializable
data class StationboardElement (
    val stop: Stop,
    val name: String,
    val category: Category,
    val number: String,
    val operator: Operator,
    val to: String,
    val passList: List<PassList>
)

@Serializable(with = Category.Companion::class)
enum class Category(val value: String) {
    IR("IR"),
    Re("RE"),
    S("S");

    companion object : KSerializer<Category> {
        override val descriptor: SerialDescriptor get() {
            return PrimitiveDescriptor("quicktype.Category", PrimitiveKind.STRING)
        }
        override fun deserialize(decoder: Decoder): Category = when (val value = decoder.decodeString()) {
            "IR" -> IR
            "RE" -> Re
            "S"  -> S
            else -> throw IllegalArgumentException("Category could not parse: $value")
        }
        override fun serialize(encoder: Encoder, value: Category) {
            return encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Operator.Companion::class)
enum class Operator(val value: String) {
    AVAWsb("AVA-wsb"),
    Sbb("SBB");

    companion object : KSerializer<Operator> {
        override val descriptor: SerialDescriptor get() {
            return PrimitiveDescriptor("quicktype.Operator", PrimitiveKind.STRING)
        }
        override fun deserialize(decoder: Decoder): Operator = when (val value = decoder.decodeString()) {
            "AVA-wsb" -> AVAWsb
            "SBB"     -> Sbb
            else      -> throw IllegalArgumentException("Operator could not parse: $value")
        }
        override fun serialize(encoder: Encoder, value: Operator) {
            return encoder.encodeString(value.value)
        }
    }
}

@Serializable
data class PassList (
    val station: Ation,
    val arrival: String? = null,
    val arrivalTimestamp: Long? = null,
    val departure: String? = null,
    val departureTimestamp: Long? = null,
    val platform: String? = null,
    val prognosis: Prognosis,
    val location: Ation
)

@Serializable
data class Prognosis(
    val platform: String? = null
)

@Serializable
data class Stop (
    val station: Ation,
    val departure: String,
    val departureTimestamp: Long,
    val platform: String,
    val prognosis: Prognosis,
    val location: Ation
)
