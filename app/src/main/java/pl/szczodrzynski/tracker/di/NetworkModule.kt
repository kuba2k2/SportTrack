package pl.szczodrzynski.tracker.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

	@Provides
	@Singleton
	fun provideOkHttpClient() = OkHttpClient.Builder()
		.addInterceptor(
			HttpLoggingInterceptor().also {
				it.setLevel(HttpLoggingInterceptor.Level.BASIC)
			}
		)
		.addNetworkInterceptor { chain ->
			chain.proceed(
				chain.request().newBuilder()
					.header("User-Agent", "Mozilla/5.0 SportTrackApp")
					.build()
			)
		}
		.build()

	@Provides
	@Singleton
	fun provideOkHttpCache(app: Application) = Cache(
		directory = File(app.cacheDir, "okhttp"),
		maxSize = 50L * 1024L * 1024L, // 50 MiB
	)
}
