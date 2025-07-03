package pl.szczodrzynski.tracker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.szczodrzynski.tracker.data.db.AppDb
import pl.szczodrzynski.tracker.manager.SyncManager
import pl.szczodrzynski.tracker.manager.TrackerManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {

	@Provides
	@Singleton
	fun provideTrackerManager(appDb: AppDb) = TrackerManager(appDb)

	@Provides
	@Singleton
	fun provideSyncManager(appDb: AppDb) = SyncManager(appDb)
}
