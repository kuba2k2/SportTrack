package pl.szczodrzynski.tracker.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.szczodrzynski.tracker.data.db.AppDb
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

	@Provides
	@Singleton
	fun provideAppDb(app: Application) = Room.databaseBuilder(
		context = app,
		klass = AppDb::class.java,
		name = "app.db",
	).build()
}
