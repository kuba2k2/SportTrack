package pl.szczodrzynski.tracker.data.entity.serializer

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

inline fun <reified T> T.toMap(): Map<String, Any?> {
	return jsonObjectToMap(Json.encodeToJsonElement(this).jsonObject)
}

inline fun <reified T> Map<String, Any>.fromStringMap(): T =
	Json.decodeFromJsonElement<T>(
		JsonObject(this.mapValues {
			when (it.value) {
				is String -> JsonPrimitive(it.value as String)
				is List<*> -> JsonArray((it.value as List<*>).map { JsonPrimitive(it as String) })
				else -> JsonNull
			}
		})
	)

fun jsonObjectToMap(element: JsonObject): Map<String, Any?> {
	return element.entries.associate {
		it.key to extractValue(it.value)
	}
}

private fun extractValue(element: JsonElement): Any? {
	return when (element) {
		is JsonNull -> null
		is JsonPrimitive -> element.content
		is JsonArray -> element.map { extractValue(it) }
		is JsonObject -> jsonObjectToMap(element)
	}
}
