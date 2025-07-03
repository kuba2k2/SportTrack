package pl.szczodrzynski.tracker.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
	).addMigrations(object : Migration(1, 2) {
		override fun migrate(db: SupportSQLiteDatabase) {
			db.execSQL(
				"""ALTER TABLE trainingRunSplit ADD COLUMN type TEXT NOT NULL DEFAULT "SPLIT";"""
			)
		}
	}).build()
}
