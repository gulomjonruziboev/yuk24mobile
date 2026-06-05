package uz.yuk24.app.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * `driverId` may be a populated driver object or a raw MongoDB ObjectId string.
 * Review and some order endpoints return the string form.
 */
object DriverIdSerializer : KSerializer<DriverInfoDto?> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DriverId")

    override fun deserialize(decoder: Decoder): DriverInfoDto? {
        val input = decoder as? JsonDecoder ?: return null
        return when (val element = input.decodeJsonElement()) {
            is JsonNull -> null
            is JsonPrimitive -> null
            is JsonObject -> input.json.decodeFromJsonElement(DriverInfoDto.serializer(), element)
            else -> null
        }
    }

    override fun serialize(encoder: Encoder, value: DriverInfoDto?) {
        val output = encoder as? JsonEncoder
            ?: throw IllegalStateException("DriverIdSerializer only supports JSON")
        if (value == null) {
            output.encodeNull()
        } else {
            output.encodeJsonElement(
                output.json.encodeToJsonElement(DriverInfoDto.serializer(), value)
            )
        }
    }
}
