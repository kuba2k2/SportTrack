package pl.szczodrzynski.tracker.data.network.openmeteo

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

	@GET("forecast")
	suspend fun getCurrentWeather(
		@Query("latitude") latitude: Double,
		@Query("longitude") longitude: Double,
		@Query("current") current: String = "temperature_2m,relative_humidity_2m,precipitation,apparent_temperature,pressure_msl,wind_speed_10m,wind_direction_10m,weather_code",
		@Query("timezone") timezone: String = "auto",
		@Query("timeformat") timeformat: String = "unixtime",
	): WeatherResponse
}
