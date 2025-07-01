package pl.szczodrzynski.tracker.data.entity.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime

object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {

	override val descriptor = PrimitiveSerialDescriptor("java.time.ZonedDateTime", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: ZonedDateTime) {
		val string = value.toString()
		encoder.encodeString(string)
	}

	override fun deserialize(decoder: Decoder): ZonedDateTime {
		val string = decoder.decodeString()
		return ZonedDateTime.parse(string)
	}
}
