package pl.szczodrzynski.tracker.data.entity.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

object InstantSerializer : KSerializer<Instant> {

	override val descriptor = PrimitiveSerialDescriptor("java.time.Instant", PrimitiveKind.LONG)

	override fun serialize(encoder: Encoder, value: Instant) =
		encoder.encodeLong(value.toEpochMilli())

	override fun deserialize(decoder: Decoder) =
		Instant.ofEpochMilli(decoder.decodeLong())
}
