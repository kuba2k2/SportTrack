package pl.szczodrzynski.tracker.data.network.openmeteo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class WeatherResponse(
	val latitude: Double,
	val longitude: Double,
	val generationtimeMs: Double,
	val utcOffsetSeconds: Int,
	val timezone: String,
	val timezoneAbbreviation: String,
	val elevation: Float,
	val currentUnits: CurrentUnits,
	val current: CurrentWeather,
) {

	@Serializable
	data class CurrentUnits(
		val time: String,
		val interval: String,
		@SerialName("temperature_2m")
		val temperature2m: String,
		@SerialName("relative_humidity_2m")
		val relativeHumidity2m: String,
		val precipitation: String,
		val apparentTemperature: String,
		val pressureMsl: String,
		@SerialName("wind_speed_10m")
		val windSpeed10m: String,
		@SerialName("wind_direction_10m")
		val windDirection10m: String,
		val weatherCode: String,
	)

	@Serializable
	data class CurrentWeather(
		val time: Long,
		val interval: Int,
		@SerialName("temperature_2m")
		val temperature2m: Float,
		@SerialName("relative_humidity_2m")
		val relativeHumidity2m: Int,
		val precipitation: Float,
		val apparentTemperature: Float,
		val pressureMsl: Float,
		@SerialName("wind_speed_10m")
		val windSpeed10m: Float,
		@SerialName("wind_direction_10m")
		val windDirection10m: Int,
		val weatherCode: Int,
	) {
		fun windDirectionString(): String {
			val directions = listOf(
				"N", "NNE", "NE", "ENE",
				"E", "ESE", "SE", "SSE",
				"S", "SSW", "SW", "WSW",
				"W", "WNW", "NW", "NNW"
			)
			val index = ((windDirection10m % 360) / 22.5).roundToInt() % 16
			return directions[index]
		}

		fun weatherCodeString() = when (weatherCode) {
			0 -> "Bezchmurnie"
			1 -> "Przeważnie bezchmurnie"
			2 -> "Częściowo pochmurno"
			3 -> "Zachmurzenie całkowite"
			45 -> "Mgła"
			48 -> "Osadzająca się mgła szronowa"
			51 -> "Lekka mżawka"
			53 -> "Umiarkowana mżawka"
			55 -> "Silna mżawka"
			56 -> "Lekka marznąca mżawka"
			57 -> "Silna marznąca mżawka"
			61 -> "Lekki deszcz"
			63 -> "Umiarkowany deszcz"
			65 -> "Silny deszcz"
			66 -> "Lekki marznący deszcz"
			67 -> "Silny marznący deszcz"
			71 -> "Lekki śnieg"
			73 -> "Umiarkowany śnieg"
			75 -> "Silny śnieg"
			77 -> "Ziarna lodowe"
			80 -> "Przelotne opady deszczu"
			81 -> "Umiarkowane przelotne opady deszczu"
			82 -> "Silne przelotne opady deszczu"
			85 -> "Przelotne opady śniegu"
			86 -> "Silne przelotne opady śniegu"
			95 -> "Burza (możliwy deszcz)"
			96 -> "Burza z lekkim gradem"
			99 -> "Burza z silnym gradem"
			else -> "Nieznany kod pogodowy"
		}
	}
}
